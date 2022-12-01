package models.BatchActivityConnector;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;

public class BatchActivityConnectorConditionModel {
  public int conditionId;
  public int connectorId;
  public String fieldName;
  public FieldType fieldType;
  public CompareOperator compareOperator;
  public String compareValue;
  private ObjectMapper mapper;

  @JsonCreator
  public BatchActivityConnectorConditionModel(
      @JsonProperty("conditionId") int conditionId,
      @JsonProperty("connectorId") int connectorId,
      @JsonProperty("fieldName") String fieldName,
      @JsonProperty("fieldType") String fieldType,
      @JsonProperty("compareOperator") String compareOperator,
      @JsonProperty("compareValue") String compareValue) {
    this.conditionId = conditionId;
    this.connectorId = connectorId;
    this.fieldName = fieldName;
    this.fieldType = FieldType.from(fieldType);
    this.compareOperator = CompareOperator.from(compareOperator);
    this.compareValue = compareValue;
    this.mapper = new ObjectMapper();
  }

  public BatchActivityConnectorConditionModel(String json) {
    BatchActivityConnectorConditionModel object = null;
    try {
      object = mapper.readValue(json, BatchActivityConnectorConditionModel.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
    new BatchActivityConnectorConditionModel(
      object.conditionId,
      object.connectorId,
      object.fieldName,
      object.fieldType.value(),
      object.compareOperator.value(),
      object.compareValue);
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

  public enum CompareOperator {
    EQUALS("="),
    LARGER_THAN(">"),
    SMALLER_THAN("<");

    private String value;

    CompareOperator(String value) {
      this.value = value;
    }

    public static CompareOperator from(String value) {
      switch (value) {
        case "=":
          return EQUALS;
        case ">":
          return LARGER_THAN;
        case "<":
          return SMALLER_THAN;
        default:
          return EQUALS;
      }
    }

    public String value() {
      return value;
    }
  }

  public enum FieldType {
    INT("INT"),
    LONG("LONG"),
    STRING("STRING"),
    FLOAT("FLOAT"),
    DOUBLE("DOUBLE"),
    BOOLEAN("BOOLEAN");

    private final String value;

    FieldType(final String value) {
      this.value = value;
    }

    public static FieldType from(final String value) {
      switch (value) {
        case "INT":
          return INT;
        case "LONG":
          return LONG;
        case "STRING":
          return STRING;
        case "FLOAT":
          return FLOAT;
        case "DOUBLE":
          return DOUBLE;
        case "BOOLEAN":
          return BOOLEAN;
        default:
          return STRING;
      }
    }

    public String value() {
      return value;
    }
  }
}
