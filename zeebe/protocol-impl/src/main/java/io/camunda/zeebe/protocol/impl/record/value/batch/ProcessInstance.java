/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.protocol.impl.record.value.batch;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.camunda.zeebe.msgpack.UnpackedObject;
import io.camunda.zeebe.msgpack.property.DocumentProperty;
import io.camunda.zeebe.msgpack.property.IntegerProperty;
import io.camunda.zeebe.msgpack.property.LongProperty;
import io.camunda.zeebe.msgpack.property.StringProperty;
import io.camunda.zeebe.protocol.impl.encoding.MsgPackConverter;
import io.camunda.zeebe.protocol.record.value.BpmnElementType;
import io.camunda.zeebe.util.buffer.BufferUtil;
import java.util.Map;
import org.agrona.DirectBuffer;

public final class ProcessInstance extends UnpackedObject
    implements io.camunda.zeebe.protocol.record.value.batch.ProcessInstance {

  private final LongProperty processInstanceKeyProperty =
      new LongProperty("processInstanceKey", -1);
  private final LongProperty elementInstanceKeyProperty =
      new LongProperty("elementInstanceKey", -1);
  private final StringProperty bpmnProcessIdProperty = new StringProperty("bpmnProcessId", "");
  private final IntegerProperty processVersionProperty = new IntegerProperty("processVersion", -1);
  private final LongProperty processDefinitionKeyProperty =
      new LongProperty("processDefinitionKey", -1);
  private final StringProperty elementIdProperty = new StringProperty("elementId", "");
  private final StringProperty bpmnElementTypeProperty = new StringProperty("bpmnElementType", "");
  private final LongProperty flowScopeKeyProperty = new LongProperty("flowScopeKey", -1);
  private final DocumentProperty variablesProperty = new DocumentProperty("variables");

  public ProcessInstance() {
    declareProperty(processInstanceKeyProperty)
        .declareProperty(elementInstanceKeyProperty)
        .declareProperty(bpmnProcessIdProperty)
        .declareProperty(processVersionProperty)
        .declareProperty(processDefinitionKeyProperty)
        .declareProperty(elementIdProperty)
        .declareProperty(bpmnElementTypeProperty)
        .declareProperty(flowScopeKeyProperty)
        .declareProperty(variablesProperty);
  }

  @Override
  public long getProcessInstanceKey() {
    return processInstanceKeyProperty.getValue();
  }

  @Override
  public long getElementInstanceKey() {
    return elementInstanceKeyProperty.getValue();
  }

  @Override
  public String getBpmnProcessId() {
    return BufferUtil.bufferAsString(bpmnProcessIdProperty.getValue());
  }

  @Override
  public DirectBuffer getBpmnProcessIdBuffer() {
    return bpmnProcessIdProperty.getValue();
  }

  @Override
  public int getProcessVersion() {
    return processVersionProperty.getValue();
  }

  @Override
  public long getProcessDefinitionKey() {
    return processDefinitionKeyProperty.getValue();
  }

  @Override
  public String getElementId() {
    return BufferUtil.bufferAsString(elementIdProperty.getValue());
  }

  @Override
  public BpmnElementType getBpmnElementType() {
    return BpmnElementType.bpmnElementTypeFor(
        BufferUtil.bufferAsString(bpmnElementTypeProperty.getValue()));
  }

  @Override
  public String getBpmnElementTypeDescription() {
    return BufferUtil.bufferAsString(bpmnElementTypeProperty.getValue());
  }

  @Override
  public long getFlowScopeKey() {
    return flowScopeKeyProperty.getValue();
  }

  @Override
  public Map<String, Object> getVariables() {
    return MsgPackConverter.convertToMap(getVariablesBuffer());
  }

  @JsonIgnore
  @Override
  public DirectBuffer getVariablesBuffer() {
    return variablesProperty.getValue();
  }

  public ProcessInstance setProcessInstanceKey(long key) {
    processInstanceKeyProperty.setValue(key);
    return this;
  }

  public ProcessInstance setElementInstanceKey(long key) {
    elementInstanceKeyProperty.setValue(key);
    return this;
  }

  public ProcessInstance setBpmnProcessId(String id) {
    bpmnProcessIdProperty.setValue(id);
    return this;
  }

  public ProcessInstance setProcessVersion(int version) {
    processVersionProperty.setValue(version);
    return this;
  }

  public ProcessInstance setProcessDefinitionKey(long key) {
    processDefinitionKeyProperty.setValue(key);
    return this;
  }

  public ProcessInstance setElementId(String id) {
    elementIdProperty.setValue(id);
    return this;
  }

  public ProcessInstance setBpmnElementType(String type) {
    bpmnElementTypeProperty.setValue(type);
    return this;
  }

  public ProcessInstance setFlowScopeKey(long key) {
    flowScopeKeyProperty.setValue(key);
    return this;
  }

  public ProcessInstance setVariables(final DirectBuffer variables) {
    variablesProperty.setValue(variables);
    return this;
  }
}
