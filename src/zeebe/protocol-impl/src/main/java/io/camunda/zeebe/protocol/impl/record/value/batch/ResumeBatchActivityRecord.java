/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.protocol.impl.record.value.batch;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.camunda.zeebe.msgpack.property.ArrayProperty;
import io.camunda.zeebe.msgpack.property.BooleanProperty;
import io.camunda.zeebe.protocol.impl.record.UnifiedRecordValue;
import io.camunda.zeebe.protocol.record.value.ResumeBatchActivityRecordValue;
import java.util.ArrayList;
import java.util.List;
import org.agrona.DirectBuffer;

public final class ResumeBatchActivityRecord extends UnifiedRecordValue
    implements ResumeBatchActivityRecordValue {

  private final ArrayProperty<ProcessInstance> processInstancesProperty =
      new ArrayProperty<>("processInstances", new ProcessInstance());
  private final BooleanProperty isBatchExecutedProperty =
      new BooleanProperty("isBatchExecuted", false);

  public ResumeBatchActivityRecord() {
    declareProperty(processInstancesProperty).declareProperty(isBatchExecutedProperty);
  }

  @Override
  public List<io.camunda.zeebe.protocol.record.value.batch.ProcessInstance> getProcessInstances() {
    final List<io.camunda.zeebe.protocol.record.value.batch.ProcessInstance> list =
        new ArrayList<>();

    for (final ProcessInstance instance : processInstancesProperty) {
      final ProcessInstance copiedInstance = new ProcessInstance();
      copiedInstance.setProcessInstanceKey(instance.getProcessInstanceKey());
      copiedInstance.setElementInstanceKey(instance.getElementInstanceKey());
      copiedInstance.setBpmnProcessId(instance.getBpmnProcessId());
      copiedInstance.setProcessVersion(instance.getProcessVersion());
      copiedInstance.setProcessDefinitionKey(instance.getProcessDefinitionKey());
      copiedInstance.setElementId(instance.getElementId());
      copiedInstance.setBpmnElementType(instance.getBpmnElementTypeDescription());
      copiedInstance.setFlowScopeKey(instance.getFlowScopeKey());
      list.add(copiedInstance);
    }

    return list;
  }

  @Override
  public boolean getIsBatchExecuted() {
    return isBatchExecutedProperty.getValue();
  }

  public ResumeBatchActivityRecord addProcessInstance(
      final long processInstanceKey,
      final long elementInstanceKey,
      final String bpmnProcessId,
      final int processVersion,
      final long processDefinitionKey,
      final String elementId,
      final String bpmnElementType,
      final Long flowScopeKey,
      final DirectBuffer variables) {
    processInstancesProperty
        .add()
        .setProcessInstanceKey(processInstanceKey)
        .setElementInstanceKey(elementInstanceKey)
        .setBpmnProcessId(bpmnProcessId)
        .setProcessVersion(processVersion)
        .setProcessDefinitionKey(processDefinitionKey)
        .setElementId(elementId)
        .setBpmnElementType(bpmnElementType)
        .setFlowScopeKey(flowScopeKey)
        .setVariables(variables);
    return this;
  }

  public ResumeBatchActivityRecord setIsBatchExecuted(final boolean isBatchExecuted) {
    isBatchExecutedProperty.setValue(isBatchExecuted);
    return this;
  }

  @JsonIgnore
  @Override
  public boolean hasProcessInstances() {
    return !processInstancesProperty.isEmpty();
  }
}
