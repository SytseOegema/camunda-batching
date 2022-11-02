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
package io.camunda.zeebe.engine.adapter;

import static io.camunda.zeebe.util.buffer.BufferUtil.bufferAsString;

import io.camunda.zeebe.engine.processing.bpmn.BpmnElementContextImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessInstanceLogger {
  private Logger logger;

  public ProcessInstanceLogger() {
    logger = LoggerFactory.getLogger("io.camunda.zeebe.engine.adapter");
    logger.info("ProcessInstanceLogger()");
  }

  public void logInstance(BpmnElementContextImpl context) {
    logger.info("ProcessInstanceLogger new instance");
    logger.info("getProcessInstanceKey: " + String.valueOf(context.getProcessInstanceKey()));
    logger.info("getProcessDefinitionKey: " + String.valueOf(context.getProcessDefinitionKey()));
    logger.info("getProcessVersion: " + String.valueOf(context.getProcessVersion()));

    logger.info("getBpmnProcessId: " + bufferAsString(context.getBpmnProcessId()));
    logger.info("getElementId: " + bufferAsString(context.getElementId()));

    logger.info(
        "getParentProcessInstanceKey: " + String.valueOf(context.getParentProcessInstanceKey()));
    logger.info(
        "getParentElementInstanceKey: " + String.valueOf(context.getParentElementInstanceKey()));
  }
}
