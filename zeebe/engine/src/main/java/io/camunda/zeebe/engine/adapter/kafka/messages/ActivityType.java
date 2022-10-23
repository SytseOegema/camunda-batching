/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.engine.adapter.kafka.messages;

public enum ActivityType {
  // Default
  UNSPECIFIED("unspecified"),

  // Containers
  PROCESS("process"),
  SUB_PROCESS("subProcess"),
  EVENT_SUB_PROCESS("eventSubProcess"),

  // Events
  START_EVENT("startEvent"),
  INTERMEDIATE_CATCH_EVENT("intermediateCatchEvent"),
  INTERMEDIATE_THROW_EVENT("intermediateThrowEvent"),
  BOUNDARY_EVENT("boundaryEvent"),
  END_EVENT("endEvent"),

  // Tasks
  SERVICE_TASK("serviceTask"),
  RECEIVE_TASK("receiveTask"),
  USER_TASK("userTask"),
  MANUAL_TASK("manualTask"),

  // Gateways
  EXCLUSIVE_GATEWAY("exclusiveGateway"),
  PARALLEL_GATEWAY("parallelGateway"),
  EVENT_BASED_GATEWAY("eventBasedGateway"),
  INCLUSIVE_GATEWAY("inclusiveGateway"),

  // Other
  SEQUENCE_FLOW("sequenceFlow"),
  MULTI_INSTANCE_BODY("multiInstanceBody"),
  CALL_ACTIVITY("callActivity"),

  BUSINESS_RULE_TASK("businessRuleTask"),
  SCRIPT_TASK("scriptTask"),
  SEND_TASK("sendTask");

  private String elementTypeName;

  private ActivityType(String elementTypeName) {
    elementTypeName = elementTypeName;
  }

  public String getElementTypeName() {
    return elementTypeName;
  }
}
