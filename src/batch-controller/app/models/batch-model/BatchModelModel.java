package models.BatchModel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class BatchModelModel {
  public int batchModelId;
  public int maxBatchSize;
  public boolean executeParallel;
  public int activationThresholdCases;
  public int activationThresholdTime;
  public String batchExecutorURI;
  public List<String> groupBy;
  private ObjectMapper mapper;

  @JsonCreator
  public BatchModelModel(
      @JsonProperty("batchModelId") int batchModelId,
      @JsonProperty("maxBatchSize") int maxBatchSize,
      @JsonProperty("executeParallel") boolean executeParallel,
      @JsonProperty("activationThresholdCases") int activationThresholdCases,
      @JsonProperty("activationThresholdTime") int activationThresholdTime,
      @JsonProperty("batchExecutorURI") String batchExecutorURI,
      @JsonProperty("groupBy") List<String> groupBy) {
    this.batchModelId = batchModelId;
    this.maxBatchSize = maxBatchSize;
    this.executeParallel = executeParallel;
    this.activationThresholdCases = activationThresholdCases;
    this.activationThresholdTime = activationThresholdTime;
    this.batchExecutorURI = batchExecutorURI;
    this.groupBy = groupBy;
    this.mapper = new ObjectMapper();
  }

  public BatchModelModel(String json) {
    BatchModelModel object = null;
    try {
      object = mapper.readValue(json, BatchModelModel.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
    new BatchModelModel(
      object.batchModelId,
      object.maxBatchSize,
      object.executeParallel,
      object.activationThresholdCases,
      object.activationThresholdTime,
      object.batchExecutorURI,
      object.groupBy);
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
