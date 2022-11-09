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
import io.camunda.batching.messaging.messages.ActivityType;
import io.camunda.batching.messaging.messages.ProcessDTO;
import io.camunda.batching.messaging.serialization.ProcessSerializer;
import io.camunda.zeebe.engine.processing.deployment.model.element.AbstractFlowElement;
import io.camunda.zeebe.engine.processing.deployment.model.element.ExecutableProcess;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessProducer {
  private Logger logger;
  private final String topic = "processes";
  private ProcessSerializer serializer = new ProcessSerializer();
  private MessageProducer producer = new MessageProducer("kafka:9092", topic, serializer);

  public ProcessProducer() {
    logger = LoggerFactory.getLogger("ProcessProducer");
    logger.info("ProcessProducer()");
  }

  public void produceMessages(List<ExecutableProcess> processes) {
    for (ExecutableProcess process : processes) {
      produceMessage(process);
    }
  }

  public void produceMessage(ExecutableProcess process) {
    final List<ActivityDTO> activities = new ArrayList<ActivityDTO>();
    for (AbstractFlowElement element : process.getFlowElementList()) {
      if (isTask(element)) {
        activities.add(
            new ActivityDTO(
                bufferAsString(element.getId()),
                bufferAsString(element.getId()),
                identifyType(element)));
      }
    }

    final ProcessDTO message =
        new ProcessDTO(
            bufferAsString(process.getId()), bufferAsString(process.getId()), activities);
    producer.sendMessage(bufferAsString(process.getId()), message);
  }

  private boolean isTask(AbstractFlowElement element) {
    final Optional<String> elementType = element.getElementType().getElementTypeName();
    if (elementType.isPresent()) {
      logger.info("type: " + elementType.get());
    } else {
      logger.info("type unkown...");
    }

    boolean isTask = false;
    switch (element.getElementType()) {
      case SERVICE_TASK:
      case RECEIVE_TASK:
      case USER_TASK:
      case MANUAL_TASK:
      case BUSINESS_RULE_TASK:
      case SCRIPT_TASK:
      case SEND_TASK:
        isTask = true;
        break;
      default:
        isTask = false;
        break;
    }
    return isTask;
  }

  private ActivityType identifyType(final AbstractFlowElement element) {
    ActivityType type = ActivityType.UNSPECIFIED;
    switch (element.getElementType()) {
      case SERVICE_TASK:
        type = ActivityType.SERVICE_TASK;
        break;
      case RECEIVE_TASK:
        type = ActivityType.RECEIVE_TASK;
        break;
      case USER_TASK:
        type = ActivityType.USER_TASK;
        break;
      case MANUAL_TASK:
        type = ActivityType.MANUAL_TASK;
        break;
      case BUSINESS_RULE_TASK:
        type = ActivityType.BUSINESS_RULE_TASK;
        break;
      case SCRIPT_TASK:
        type = ActivityType.SCRIPT_TASK;
        break;
      case SEND_TASK:
        type = ActivityType.SEND_TASK;
        break;
      default:
        type = ActivityType.UNSPECIFIED;
        break;
    }
    return type;
  }
}
