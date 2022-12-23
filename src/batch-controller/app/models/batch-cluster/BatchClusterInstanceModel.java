package models.BatchCluster;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

// Connection between process instance and batch cluster.
// Basically builds the list of process instances inside a cluster.
public class BatchClusterInstanceModel {
  public int batchClusterId;
  public int processInstanceId;
  public long processInstanceKey;
  private ObjectMapper mapper;

  @JsonCreator
  public BatchClusterInstanceModel(
      @JsonProperty("batchClusterId") int batchClusterId,
      @JsonProperty("processInstanceId") int processInstanceId,
      @JsonProperty("processInstanceKey") long processInstanceKey) {
    this.batchClusterId = batchClusterId;
    this.processInstanceId = processInstanceId;
    this.processInstanceKey = processInstanceKey;
    this.mapper = new ObjectMapper();
  }

  public BatchClusterInstanceModel(String json) {
    BatchClusterInstanceModel object = null;
    try {
      object = mapper.readValue(json, BatchClusterInstanceModel.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
    new BatchClusterInstanceModel(
      object.batchClusterId,
      object.processInstanceId,
      object.processInstanceKey);
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
