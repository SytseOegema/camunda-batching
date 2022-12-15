package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.batching.messaging.messages.ProcessInstanceDTO;

public class ProcessInstanceModel {
  public long processInstanceKey;
  public long elementInstanceKey;
  public String bpmnProcessId;
  public int processVersion;
  public long processDefinitionKey;
  public String elementId;
  public String elementType;
  public long flowScopeKey;
  public String variables;
  public String intent;
  private ObjectMapper mapper;

  @JsonCreator
  public ProcessInstanceModel(
      @JsonProperty("processInstanceKey") long processInstanceKey,
      @JsonProperty("elementInstanceKey") long elementInstanceKey,
      @JsonProperty("bpmnProcessId") String bpmnProcessId,
      @JsonProperty("processVersion") int processVersion,
      @JsonProperty("processDefinitionKey") long processDefinitionKey,
      @JsonProperty("elementId") String elementId,
      @JsonProperty("elementType") String elementType,
      @JsonProperty("flowScopeKey") long flowScopeKey,
      @JsonProperty("variables") String variables,
      @JsonProperty("intent") String intent) {
    this.processInstanceKey = processInstanceKey;
    this.elementInstanceKey = elementInstanceKey;
    this.bpmnProcessId = bpmnProcessId;
    this.processVersion = processVersion;
    this.processDefinitionKey = processDefinitionKey;
    this.elementId = elementId;
    this.elementType = elementType;
    this.flowScopeKey = flowScopeKey;
    this.variables = variables;
    this.intent = intent;
    this.mapper = new ObjectMapper();
  }

  public ProcessInstanceModel(String json) {
    ProcessInstanceModel object = null;
    try {
      object = mapper.readValue(json, ProcessInstanceModel.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
    new ProcessInstanceModel(
      object.processInstanceKey,
      object.elementInstanceKey,
      object.bpmnProcessId,
      object.processVersion,
      object.processDefinitionKey,
      object.elementId,
      object.elementType,
      object.flowScopeKey,
      object.variables,
      object.intent);
  }

  public ProcessInstanceModel(ProcessInstanceDTO dto) {
    this.processInstanceKey = dto.processInstanceKey;
    this.elementInstanceKey = dto.elementInstanceKey;
    this.bpmnProcessId = dto.bpmnProcessId;
    this.processVersion = dto.processVersion;
    this.processDefinitionKey = dto.processDefinitionKey;
    this.elementId = dto.elementId;
    this.elementType = dto.elementType.getElementTypeName();
    this.flowScopeKey = dto.flowScopeKey;
    this.variables = dto.variables;
    this.intent = dto.intent.getValue();
    this.mapper = new ObjectMapper();
  }

  public String toJson() {
    String retVal = "";

    try {
      retVal = mapper.writeValueAsString(this);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return retVal;
  }
}
