/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.streamprocessor;

import static io.camunda.zeebe.engine.processing.streamprocessor.TypedEventRegistry.TYPE_REGISTRY;

import io.camunda.zeebe.engine.api.PostCommitTask;
import io.camunda.zeebe.engine.api.ProcessingResponse;
import io.camunda.zeebe.engine.api.ProcessingResult;
import io.camunda.zeebe.engine.api.ProcessingResultBuilder;
import io.camunda.zeebe.engine.api.records.RecordBatch;
import io.camunda.zeebe.engine.api.records.RecordBatchEntry;
import io.camunda.zeebe.engine.api.records.RecordBatchSizePredicate;
import io.camunda.zeebe.msgpack.UnpackedObject;
import io.camunda.zeebe.protocol.impl.record.UnifiedRecordValue;
import io.camunda.zeebe.protocol.record.RecordType;
import io.camunda.zeebe.protocol.record.RecordValue;
import io.camunda.zeebe.protocol.record.RejectionType;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.Intent;
import io.camunda.zeebe.util.Either;
import io.camunda.zeebe.util.StringUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@code ProcessingResultBuilder} that buffers the processing results. After
 * being done with processing the build can be turned into a immutable {@link ProcessingResult},
 * which allows to process the result further.
 */
final class BufferedProcessingResultBuilder implements ProcessingResultBuilder {

  private final List<PostCommitTask> postCommitTasks = new ArrayList<>();

  private final RecordBatch mutableRecordBatch;
  private ProcessingResponseImpl processingResponse;

  BufferedProcessingResultBuilder(final RecordBatchSizePredicate predicate) {
    mutableRecordBatch = new RecordBatch(predicate);
  }

  @Override
  public Either<RuntimeException, ProcessingResultBuilder> appendRecordReturnEither(
      final long key,
      final RecordType type,
      final Intent intent,
      final RejectionType rejectionType,
      final String rejectionReason,
      final RecordValue value) {

    final ValueType valueType = TYPE_REGISTRY.get(value.getClass());
    if (valueType == null) {
      // usually happens when the record is not registered at the TypedStreamEnvironment
      throw new IllegalStateException("Missing value type mapping for record: " + value.getClass());
    }

    if (value instanceof UnifiedRecordValue unifiedRecordValue) {
      final var either =
          mutableRecordBatch.appendRecord(
              key, -1, type, intent, rejectionType, rejectionReason, valueType, unifiedRecordValue);
      if (either.isLeft()) {
        return Either.left(either.getLeft());
      }
    } else {
      throw new IllegalStateException(
          String.format(
              "The record value %s is not a UnifiedRecordValue",
              StringUtil.limitString(value.toString(), 1024)));
    }

    return Either.right(this);
  }

  @Override
  public ProcessingResultBuilder withResponse(
      final RecordType recordType,
      final long key,
      final Intent intent,
      final UnpackedObject value,
      final ValueType valueType,
      final RejectionType rejectionType,
      final String rejectionReason,
      final long requestId,
      final int requestStreamId) {
    final var entry =
        RecordBatchEntry.createEntry(
            key, -1, recordType, intent, rejectionType, rejectionReason, valueType, value);
    processingResponse = new ProcessingResponseImpl(entry, requestId, requestStreamId);
    return this;
  }

  @Override
  public ProcessingResultBuilder appendPostCommitTask(final PostCommitTask task) {
    final Logger logger = LoggerFactory.getLogger("ResumeBatchActivityProcessor");
    postCommitTasks.add(task);
    return this;
  }

  @Override
  public ProcessingResultBuilder resetPostCommitTasks() {
    final Logger logger = LoggerFactory.getLogger("ResumeBatchActivityProcessor");
    postCommitTasks.clear();
    return this;
  }

  @Override
  public ProcessingResult build() {
    return new BufferedResult(mutableRecordBatch, processingResponse, postCommitTasks);
  }

  @Override
  public boolean canWriteEventOfLength(final int eventLength) {
    return mutableRecordBatch.canAppendRecordOfLength(eventLength);
  }

  record ProcessingResponseImpl(RecordBatchEntry responseValue, long requestId, int requestStreamId)
      implements ProcessingResponse {}
}
