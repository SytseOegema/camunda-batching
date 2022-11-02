/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.protocol.impl.record.value.batch;

import static io.camunda.zeebe.util.buffer.BufferUtil.bufferAsString;

import io.camunda.zeebe.msgpack.property.StringProperty;
import io.camunda.zeebe.protocol.impl.record.UnifiedRecordValue;
import io.camunda.zeebe.protocol.record.value.ResumeBatchActivityRecordValue;
import org.agrona.DirectBuffer;

public final class ResumeBatchActivityRecord extends UnifiedRecordValue
    implements ResumeBatchActivityRecordValue {

  private final StringProperty bpmnProcessIdProperty = new StringProperty("bpmnProcessId", "");

  public ResumeBatchActivityRecord() {
    declareProperty(bpmnProcessIdProperty);
  }

  @Override
  public String getBpmnProcessId() {
    return bufferAsString(bpmnProcessIdProperty.getValue());
  }

  public ResumeBatchActivityRecord setBpmnProcessId(final String bpmnProcessId) {
    bpmnProcessIdProperty.setValue(bpmnProcessId);
    return this;
  }

  public ResumeBatchActivityRecord setBpmnProcessId(final DirectBuffer bpmnProcessId) {
    return setBpmnProcessId(bufferAsString(bpmnProcessId));
  }
}
