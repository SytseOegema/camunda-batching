package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProcessModel {
  public String id;
  public String name;
  private ObjectMapper mapper;


  @JsonCreator
  public ProcessModel(@JsonProperty("id") String id, @JsonProperty("name") String name) {
    this.id = id;
    this.name = name;
    this.mapper = new ObjectMapper();
  }

  public ProcessModel(String json) {
    ProcessModel object = null;
    try {
      object = mapper.readValue(json, ProcessModel.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
    new ProcessModel(object.id, object.name);
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
