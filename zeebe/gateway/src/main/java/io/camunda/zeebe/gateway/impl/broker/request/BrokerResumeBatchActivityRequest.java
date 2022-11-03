/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.gateway.impl.broker.request;

import io.camunda.zeebe.protocol.impl.record.value.batch.ResumeBatchActivityRecord;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.ResumeBatchActivityIntent;
import org.agrona.DirectBuffer;

public final class BrokerResumeBatchActivityRequest
    extends BrokerExecuteCommand<ResumeBatchActivityRecord> {

  private final ResumeBatchActivityRecord requestDto = new ResumeBatchActivityRecord();

  public BrokerResumeBatchActivityRequest(final String jobType) {
    super(ValueType.RESUME_BATCH_ACTIVITY, ResumeBatchActivityIntent.RESUME);
    requestDto.setBpmnProcessId(jobType);
  }

  @Override
  public String toString() {
    return "BrokerResumeBatchActivityRequest{" + "requestDto=" + requestDto + '}';
  }

  @Override
  public ResumeBatchActivityRecord getRequestWriter() {
    return requestDto;
  }

  @Override
  protected ResumeBatchActivityRecord toResponseDto(final DirectBuffer buffer) {
    final ResumeBatchActivityRecord responseDto = new ResumeBatchActivityRecord();
    responseDto.wrap(buffer);
    return responseDto;
  }
}
