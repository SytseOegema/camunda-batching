/*
 * Messaging implementation
 *
 * Created by Sytse Oegema
 */
package io.camunda.batching.messaging.messages;

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
    this.elementTypeName = elementTypeName;
  }

  public String getElementTypeName() {
    return elementTypeName;
  }
}
