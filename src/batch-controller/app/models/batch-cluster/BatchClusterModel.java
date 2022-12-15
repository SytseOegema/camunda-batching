package models.BatchCluster;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class BatchClusterModel {
  public int batchClusterId;
  public int batchModelId;
  public String createdAt;
  public List<BatchClusterInstanceModel> instances;
  private ObjectMapper mapper;

  @JsonCreator
  public BatchClusterModel(
      @JsonProperty("batchClusterId") int batchClusterId,
      @JsonProperty("batchModelId") int batchModelId,
      @JsonProperty("instances") List<BatchClusterInstanceModel> instances,
      @JsonProperty("createdAt") String createdAt) {
    this.batchClusterId = batchClusterId;
    this.batchModelId = batchModelId;
    this.createdAt = createdAt;
    this.instances = instances;
    this.mapper = new ObjectMapper();
  }

  public BatchClusterModel(String json) {
    BatchClusterModel object = null;
    try {
      object = mapper.readValue(json, BatchClusterModel.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
    new BatchClusterModel(
      object.batchClusterId,
      object.batchModelId,
      object.instances,
      object.createdAt);
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
