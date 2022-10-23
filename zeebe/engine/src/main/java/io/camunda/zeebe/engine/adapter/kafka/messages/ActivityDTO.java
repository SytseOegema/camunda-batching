/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
/*
 * Camunda extension used to implement batch procesing.
 *
 * Created by Sytse Oegema
 */
package io.camunda.zeebe.engine.adapter.kafka.messages;

public class ActivityDTO {
  public String id;
  public String name;
  public ActivityType activityType;

  public ActivityDTO(String id, String name, ActivityType activityType) {
    id = id;
    name = name;
    activityType = activityType;
  }
}
