/*
 * Messaging implementation
 *
 * Created by Sytse Oegema
 */
package io.camunda.batching.messaging.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProcessInstanceDTO {
  public long processInstanceKey;
  public String bpmnProcessId;
  public int processVersion;
  public long processDefinitionKey;
  public String elementId;
  public ActivityType elementType;
  public long flowScopeKey;
  public String variables;

  @JsonCreator
  public ProcessInstanceDTO(
      @JsonProperty("processInstanceKey") long processInstanceKey,
      @JsonProperty("bpmnProcessId") String bpmnProcessId,
      @JsonProperty("processVersion") int processVersion,
      @JsonProperty("processDefinitionKey") long processDefinitionKey,
      @JsonProperty("elementId") String elementId,
      @JsonProperty("elementType") ActivityType elementType,
      @JsonProperty("flowScopeKey") long flowScopeKey,
      @JsonProperty("variables") String variables
  ) {
    this.processInstanceKey = processInstanceKey;
    this.bpmnProcessId = bpmnProcessId;
    this.processVersion = processVersion;
    this.processDefinitionKey = processDefinitionKey;
    this.elementId = elementId;
    this.elementType = elementType;
    this.flowScopeKey = flowScopeKey;
    this.variables = variables;
  }
}
