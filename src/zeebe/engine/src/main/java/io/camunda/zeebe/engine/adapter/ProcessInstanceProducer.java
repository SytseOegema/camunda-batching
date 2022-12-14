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
import io.camunda.batching.messaging.messages.ActivityType;
import io.camunda.batching.messaging.messages.ProcessInstanceDTO;
import io.camunda.batching.messaging.messages.ProcessInstanceIntent;
import io.camunda.batching.messaging.serialization.ProcessInstanceSerializer;
import io.camunda.zeebe.engine.processing.bpmn.BpmnElementContext;
import io.camunda.zeebe.protocol.record.value.BpmnElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessInstanceProducer {
  private Logger logger;
  private final String topic = "process_instances";
  private ProcessInstanceSerializer serializer = new ProcessInstanceSerializer();
  private MessageProducer producer = new MessageProducer("kafka-camunda:9092", topic, serializer);

  public ProcessInstanceProducer() {
    logger = LoggerFactory.getLogger("ProcessInstanceProducer");
  }

  public void produceMessage(
      BpmnElementContext context,
      String variables,
      final io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent intent) {
    // logInstance(context);
    final ProcessInstanceDTO message =
        new ProcessInstanceDTO(
            context.getProcessInstanceKey(),
            context.getElementInstanceKey(),
            bufferAsString(context.getBpmnProcessId()),
            context.getProcessVersion(),
            context.getProcessDefinitionKey(),
            bufferAsString(context.getElementId()),
            identifyType(context.getBpmnElementType()),
            context.getFlowScopeKey(),
            variables,
            identifyIntent(intent));

    producer.sendMessage(String.valueOf(context.getElementInstanceKey()), message);
  }

  private void logInstance(BpmnElementContext context) {
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

  private ActivityType identifyType(final BpmnElementType bpmnType) {
    ActivityType type = ActivityType.UNSPECIFIED;
    switch (bpmnType) {
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

  private ProcessInstanceIntent identifyIntent(
      final io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent bpmnIntent) {
    ProcessInstanceIntent intent = ProcessInstanceIntent.UNSPECIFIED;
    switch (bpmnIntent) {
      case ACTIVATE_ELEMENT:
        intent = ProcessInstanceIntent.READY_ELEMENT;
        break;
      case COMPLETE_ELEMENT:
        intent = ProcessInstanceIntent.COMPLETE_ELEMENT;
        break;
      case TERMINATE_ELEMENT:
        intent = ProcessInstanceIntent.TERMINATE_ELEMENT;
        break;
      case RESUME_ELEMENT:
        intent = ProcessInstanceIntent.RESUME_ELEMENT;
        break;
      default:
        intent = ProcessInstanceIntent.UNSPECIFIED;
        break;
    }

    return intent;
  }
}
