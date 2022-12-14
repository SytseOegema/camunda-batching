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

  public BrokerResumeBatchActivityRequest() {
    super(ValueType.RESUME_BATCH_ACTIVITY, ResumeBatchActivityIntent.RESUME);
  }

  public BrokerResumeBatchActivityRequest setIsBatchExecuted(final boolean isBatchExecuted) {
    requestDto.setIsBatchExecuted(isBatchExecuted);
    return this;
  }

  public BrokerResumeBatchActivityRequest addProcessInstance(
      final long processInstanceKey,
      final long elementInstanceKey,
      final String bpmnProcessId,
      final int processVersion,
      final long processDefinitionKey,
      final String elementId,
      final String bpmnElementType,
      final Long flowScopeKey,
      final DirectBuffer variables) {
    // hier dit toepassen op requestDto;
    requestDto.addProcessInstance(
        processInstanceKey,
        elementInstanceKey,
        bpmnProcessId,
        processVersion,
        processDefinitionKey,
        elementId,
        bpmnElementType,
        flowScopeKey,
        variables);
    return this;
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
