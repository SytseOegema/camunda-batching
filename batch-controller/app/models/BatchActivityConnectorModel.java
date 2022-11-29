package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;

public class BatchActivityConnectorModel {
  public int connectorId;
  public Boolean active;
  public String validity;
  public List<BatchActivityConnectorConditionModel> conditions;
  public String activityId;
  public int batchModelId;
  private ObjectMapper mapper;

  @JsonCreator
  public BatchActivityConnectorModel(
      @JsonProperty("connectorId") int connectorId,
      @JsonProperty("active") boolean active,
      @JsonProperty("validity") String validity,
      @JsonProperty("conditions") List<BatchActivityConnectorConditionModel> conditions,
      @JsonProperty("activityId") String activityId,
      @JsonProperty("batchModelId") int batchModelId) {
    this.connectorId = connectorId;
    this.active = active;
    this.validity = validity;
    this.conditions = conditions;
    this.activityId = activityId;
    this.batchModelId = batchModelId;
    this.mapper = new ObjectMapper();
  }

  public BatchActivityConnectorModel(String json) {
    BatchActivityConnectorModel object = null;
    try {
      object = mapper.readValue(json, BatchActivityConnectorModel.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
    new BatchActivityConnectorModel(
      object.connectorId,
      object.active,
      object.validity,
      object.conditions,
      object.activityId,
      object.batchModelId);
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
