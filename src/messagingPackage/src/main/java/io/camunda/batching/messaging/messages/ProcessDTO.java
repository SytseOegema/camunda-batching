/*
 * Messaging implementation
 *
 * Created by Sytse Oegema
 */
package io.camunda.batching.messaging.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ProcessDTO {
  public String id;
  public String name;
  public List<ActivityDTO> activities;

  @JsonCreator
  public ProcessDTO(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("activities") List<ActivityDTO> activities) {
    this.id = id;
    this.name = name;
    this.activities = activities;
  }
}
