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

import io.camunda.batching.messaging.MessageProducer;
import io.camunda.batching.messaging.messages.ActivityDTO;
import io.camunda.batching.messaging.messages.ProcessDTO;
import io.camunda.batching.messaging.serialization.ProcessSerializer;
import io.camunda.zeebe.engine.processing.deployment.model.element.AbstractFlowElement;
import io.camunda.zeebe.engine.processing.deployment.model.element.ExecutableProcess;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessLogger {
  private Logger logger;

  public ProcessLogger() {
    logger = LoggerFactory.getLogger("io.camunda.zeebe.engine.adapter");
    logger.info("ProcessLogger()");
  }

  public void logProcesses(List<ExecutableProcess> processes) {
    for (ExecutableProcess process : processes) {
      logger.info("Process: " + bufferAsString(process.getId()));
      identifyType(process);
      doThings(process.getFlowElementList());
      produceMessage(process);
    }
  }

  private void produceMessage(ExecutableProcess process) {
    final ProcessSerializer serializer = new ProcessSerializer();
    final MessageProducer producer = new MessageProducer("kafka:9092", "test", serializer);
    final ProcessDTO message = new ProcessDTO("1", "test", new ArrayList<ActivityDTO>());
    producer.sendMessage(bufferAsString(process.getId()), message);
  }

  private void doThings(final Collection<AbstractFlowElement> flowElements) {
    logger.info("BpmnTransformer - recourse creation - doThings()");
    final List<AbstractFlowElement> flowElementList = new ArrayList(flowElements);

    for (AbstractFlowElement elem : flowElementList) {
      logger.info("Elem: " + bufferAsString(elem.getId()));
      identifyType(elem);
    }
  }

  private void identifyType(final AbstractFlowElement element) {
    final Optional<String> elementType = element.getElementType().getElementTypeName();
    if (elementType.isPresent()) {
      logger.info("type: " + elementType.get());
    } else {
      logger.info("type unkown...");
    }
  }
}
