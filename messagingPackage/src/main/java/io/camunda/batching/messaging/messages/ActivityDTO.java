/*
 * Messaging implementation
 *
 * Created by Sytse Oegema
 */
package io.camunda.batching.messaging.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ActivityDTO {
  public String id;
  public String name;
  public ActivityType activityType;

  @JsonCreator
  public ActivityDTO(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("activityType") ActivityType activityType) {
    this.id = id;
    this.name = name;
    this.activityType = activityType;
  }
}
