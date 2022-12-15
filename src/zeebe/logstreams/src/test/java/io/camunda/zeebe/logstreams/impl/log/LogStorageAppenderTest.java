/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.logstreams.impl.log;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.camunda.zeebe.dispatcher.BlockPeek;
import io.camunda.zeebe.dispatcher.Dispatcher;
import io.camunda.zeebe.dispatcher.Dispatchers;
import io.camunda.zeebe.dispatcher.Subscription;
import io.camunda.zeebe.dispatcher.impl.log.DataFrameDescriptor;
import io.camunda.zeebe.logstreams.log.LogStreamBatchWriter;
import io.camunda.zeebe.logstreams.log.LogStreamReader;
import io.camunda.zeebe.logstreams.log.LoggedEvent;
import io.camunda.zeebe.logstreams.storage.LogStorage.AppendListener;
import io.camunda.zeebe.logstreams.util.ListLogStorage;
import io.camunda.zeebe.protocol.Protocol;
import io.camunda.zeebe.scheduler.ActorScheduler;
import io.camunda.zeebe.util.ByteValue;
import io.camunda.zeebe.util.buffer.BufferWriter;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.agrona.CloseHelper;
import org.agrona.MutableDirectBuffer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mockito;

@Execution(ExecutionMode.CONCURRENT)
final class LogStorageAppenderTest {
  private static final int MAX_FRAGMENT_SIZE = 1024;
  private static final int PARTITION_ID = 0;
  private static final long INITIAL_POSITION = 2L;
  private static final long WRONG_POSITION = 10L;

  private final ActorScheduler scheduler =
      ActorScheduler.newActorScheduler()
          .setCpuBoundActorThreadCount(1)
          .setIoBoundActorThreadCount(1)
          .build();

  private final ListLogStorage logStorage = spy(new ListLogStorage());

  private Dispatcher dispatcher;
  private Subscription subscription;
  private LogStorageAppender appender;
  private LogStreamBatchWriter writer;
  private LogStreamReader reader;

  @BeforeEach
  void beforeEach() {
    scheduler.start();

    dispatcher =
        Dispatchers.create("0")
            .actorSchedulingService(scheduler)
            .bufferSize((int) ByteValue.ofMegabytes(100 * MAX_FRAGMENT_SIZE))
            .maxFragmentLength(MAX_FRAGMENT_SIZE)
            .initialPosition(INITIAL_POSITION)
            .build();
    subscription = spy(dispatcher.openSubscription("log"));
    Mockito.reset(subscription, logStorage);

    appender =
        new LogStorageAppender(
            "appender", PARTITION_ID, logStorage, subscription, MAX_FRAGMENT_SIZE);
    writer = new LogStreamBatchWriterImpl(PARTITION_ID, dispatcher);
    reader = new LogStreamReaderImpl(logStorage.newReader());
  }

  @AfterEach
  public void tearDown() {
    CloseHelper.quietCloseAll(appender, dispatcher, scheduler);
  }

  @Test
  void shouldAppendSingleEvent() throws InterruptedException {
    // given
    final var value = new Value(1);
    final var latch = new CountDownLatch(1);

    // when
    final var position = writer.event().valueWriter(value).done().tryWrite();
    logStorage.setPositionListener(i -> latch.countDown());
    scheduler.submitActor(appender).join();

    // then
    assertThat(latch.await(5, TimeUnit.SECONDS)).as("value was written within 5 seconds").isTrue();
    assertThat(reader.seek(position)).isTrue();
    assertThat(reader.hasNext()).isTrue();
    assertThat(Value.of(reader.next())).isEqualTo(value);
  }

  @Test
  void shouldAppendMultipleEvents() throws InterruptedException {
    // given
    final var values = List.of(new Value(1), new Value(2));
    final var latch = new CountDownLatch(1);

    // when
    final var highestPosition =
        writer
            .event()
            .valueWriter(values.get(0))
            .done()
            .event()
            .valueWriter(values.get(1))
            .done()
            .tryWrite();
    final var lowestPosition = highestPosition - 1;
    logStorage.setPositionListener(i -> latch.countDown());
    scheduler.submitActor(appender).join();

    // then
    assertThat(latch.await(5, TimeUnit.SECONDS)).as("value was written within 5 seconds").isTrue();

    // make sure we read the correct lowest/highest positions
    verify(logStorage, timeout(1000).times(1))
        .append(
            eq(lowestPosition),
            eq(highestPosition),
            any(ByteBuffer.class),
            any(AppendListener.class));

    // ensure events were written properly
    assertThat(reader.seek(lowestPosition)).isTrue();
    for (final var value : values) {
      assertThat(reader.hasNext()).isTrue();
      assertThat(Value.of(reader.next())).isEqualTo(value);
    }
  }

  @Test
  void shouldFailActorWhenDetectingGapsInPositions() {
    // given
    final var value = new Value(1);

    // the stub has to be defined before we start the actor, as otherwise it is accessed by multiple
    // threads, which is undefined behavior in Mockito. See the FAQ,
    // https://github.com/mockito/mockito/wiki/FAQ#is-mockito-thread-safe. The FAQ doesn't mention
    // this case in particular, but you can test this yourself - if you stub it after, it will
    // sometimes receive the invocation pointing to a different method than peekBlock.
    when(subscription.peekBlock(any(BlockPeek.class), anyInt(), anyBoolean()))
        .thenAnswer(
            a -> {
              final var result = (int) a.callRealMethod();
              if (result <= 0) {
                return result;
              }

              final BlockPeek block = a.getArgument(0);
              final LoggedEventImpl event = new LoggedEventImpl();
              event.wrap(block.getBuffer(), 0);

              // corrupt second entry
              LogEntryDescriptor.setPosition(
                  block.getBuffer(),
                  DataFrameDescriptor.messageOffset(event.getLength()),
                  WRONG_POSITION);

              return result;
            });
    scheduler.submitActor(appender).join();

    // when
    writer.event().valueWriter(value).done().event().valueWriter(value).done().tryWrite();

    // then
    Awaitility.await("until the actor has failed")
        .atMost(Duration.ofSeconds(10))
        .until(appender::isActorClosed);
    reader.seekToFirstEvent();
    assertThat(reader.hasNext()).isFalse();
  }

  private record Value(int value) implements BufferWriter {
    private static Value of(final LoggedEvent event) {
      return new Value(event.getValueBuffer().getInt(event.getValueOffset(), Protocol.ENDIANNESS));
    }

    @Override
    public int getLength() {
      return Integer.BYTES;
    }

    @Override
    public void write(final MutableDirectBuffer buffer, final int offset) {
      buffer.putInt(offset, value, Protocol.ENDIANNESS);
    }
  }
}
