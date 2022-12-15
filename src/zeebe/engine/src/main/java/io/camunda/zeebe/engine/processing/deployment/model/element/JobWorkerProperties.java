/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.engine.processing.deployment.model.element;

import io.camunda.zeebe.el.Expression;
import io.camunda.zeebe.logstreams.impl.Loggers;
import java.util.Map;
import org.slf4j.Logger;

/**
 * The properties of an element that is based on a job and should be processed by a job worker. For
 * example, a service task.
 */
public class JobWorkerProperties {
  private static final Logger LOG = Loggers.PROCESSOR_LOGGER;
  private Expression type;
  private Expression retries;
  private Expression assignee;
  private Expression candidateGroups;
  private Map<String, String> taskHeaders = Map.of();

  public boolean getIsBatchTask() {
    for (Map.Entry<String, String> entry : taskHeaders.entrySet()) {
      LOG.info(entry.getKey() + "/" + entry.getValue());
    }

    if (taskHeaders.containsKey("batchTask")) {
      LOG.info("ohno er is geen BatchTask header");
      return Boolean.parseBoolean(taskHeaders.get("batchTask"));
    }

    return false;
  }

  public Expression getType() {
    return type;
  }

  public void setType(final Expression type) {
    this.type = type;
  }

  public Expression getRetries() {
    return retries;
  }

  public void setRetries(final Expression retries) {
    this.retries = retries;
  }

  public Expression getAssignee() {
    return assignee;
  }

  public void setAssignee(final Expression assignee) {
    this.assignee = assignee;
  }

  public Expression getCandidateGroups() {
    return candidateGroups;
  }

  public void setCandidateGroups(final Expression candidateGroups) {
    this.candidateGroups = candidateGroups;
  }

  public Map<String, String> getTaskHeaders() {
    return taskHeaders;
  }

  public void setTaskHeaders(final Map<String, String> taskHeaders) {
    this.taskHeaders = taskHeaders;
  }
}
