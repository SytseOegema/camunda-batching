/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.logstreams.impl.log;

import io.camunda.zeebe.logstreams.storage.LogStorage.AppendListener;
import io.prometheus.client.Histogram.Timer;

final class Listener implements AppendListener {
  private final LogStorageAppender appender;
  private final long highestPosition;
  private final Timer appendLatencyTimer;
  private final Timer commitLatencyTimer;

  Listener(
      final LogStorageAppender appender,
      final long highestPosition,
      final Timer startAppendLatencyTimer,
      final Timer startCommitLatencyTimer) {
    this.appender = appender;
    this.highestPosition = highestPosition;
    appendLatencyTimer = startAppendLatencyTimer;
    commitLatencyTimer = startCommitLatencyTimer;
  }

  @Override
  public void onWrite(final long address) {
    appender.notifyWritePosition(highestPosition, appendLatencyTimer);
  }

  @Override
  public void onWriteError(final Throwable error) {
    LogStorageAppender.LOG.error(
        "Failed to append block with last event position {}.", highestPosition, error);
    appender.runOnFailure(error);
  }

  @Override
  public void onCommit(final long address) {
    releaseBackPressure();
    appender.notifyCommitPosition(highestPosition, commitLatencyTimer);
  }

  @Override
  public void onCommitError(final long address, final Throwable error) {
    LogStorageAppender.LOG.error(
        "Failed to commit block with last event position {}.", highestPosition, error);
    releaseBackPressure();
    appender.runOnFailure(error);
  }

  private void releaseBackPressure() {
    appender.releaseBackPressure(highestPosition);
  }
}
