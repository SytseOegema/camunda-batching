/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.engine.processing.batch;

import io.camunda.zeebe.engine.api.TypedRecord;
import io.camunda.zeebe.engine.processing.bpmn.behavior.BpmnBehaviors;
import io.camunda.zeebe.engine.processing.common.ElementActivationBehavior;
import io.camunda.zeebe.engine.processing.common.EventSubscriptionException;
import io.camunda.zeebe.engine.processing.streamprocessor.CommandProcessor;
import io.camunda.zeebe.engine.processing.streamprocessor.TypedRecordProcessor.ProcessingError;
import io.camunda.zeebe.engine.processing.streamprocessor.sideeffect.SideEffectQueue;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.TypedCommandWriter;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.TypedRejectionWriter;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.TypedResponseWriter;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.Writers;
import io.camunda.zeebe.engine.processing.variable.VariableBehavior;
import io.camunda.zeebe.engine.state.KeyGenerator;
import io.camunda.zeebe.engine.state.immutable.ProcessState;
import io.camunda.zeebe.protocol.impl.record.value.batch.ResumeBatchActivityRecord;
import io.camunda.zeebe.protocol.record.RejectionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResumeBatchActivityProcessor
    implements CommandProcessor<ResumeBatchActivityRecord> {

  // private final ResumeBatchActivityRecord newProcessInstance = new ProcessInstanceRecord();

  private final SideEffectQueue sideEffectQueue = new SideEffectQueue();

  private final ProcessState processState;
  private final VariableBehavior variableBehavior;

  private final KeyGenerator keyGenerator;
  private final TypedCommandWriter commandWriter;
  private final TypedRejectionWriter rejectionWriter;
  private final TypedResponseWriter responseWriter;

  private final ElementActivationBehavior elementActivationBehavior;

  public ResumeBatchActivityProcessor(
      final ProcessState processState,
      final KeyGenerator keyGenerator,
      final Writers writers,
      final BpmnBehaviors bpmnBehaviors) {
    this.processState = processState;
    variableBehavior = bpmnBehaviors.variableBehavior();
    this.keyGenerator = keyGenerator;
    commandWriter = writers.command();
    rejectionWriter = writers.rejection();
    responseWriter = writers.response();
    elementActivationBehavior = bpmnBehaviors.elementActivationBehavior();
  }

  @Override
  public boolean onCommand(
      final TypedRecord<ResumeBatchActivityRecord> command,
      final CommandControl<ResumeBatchActivityRecord> controller) {

    final Logger logger = LoggerFactory.getLogger("ResumeBatchActivityProcessor");
    logger.info("onCommand()");
    logger.info("well in onCommand van ResumeBatchActivityProcessor");

    sideEffectQueue.clear();
    logger.info("well in onCommand van ResumeBatchActivityProcessor");

    final ResumeBatchActivityRecord record = command.getValue();
    logger.info("well in onCommand van ResumeBatchActivityProcessor");

    controller.reject(RejectionType.INVALID_STATE, "goh reply");
    // controller.accept(ResumeBatchActivityIntent.RESUMED, record);
    logger.info("well in onCommand van ResumeBatchActivityProcessor");

    return true;

    // logger.info("1");
    // final ResumeBatchActivityRecord resumeEvent = command.getValue();
    // logger.info("2");
    // resumeEvent.setBpmnProcessId("joh dit werkt als een tiet!");
    // logger.info("3");
    //
    // final long key = keyGenerator.nextKey();
    // logger.info("4");
    // responseWriter.writeEventOnCommand(
    //     key, ResumeBatchActivityIntent.RESUMED, resumeEvent, command);
    //
    // stateWriter.appendFollowUpEvent(key, ResumeBatchActivityIntent.RESUMED, resumeEvent);
    //
    // logger.info("5");

  }

  @Override
  public ProcessingError tryHandleError(
      final TypedRecord<ResumeBatchActivityRecord> typedCommand, final Throwable error) {
    if (error instanceof EventSubscriptionException exception) {
      // This exception is only thrown for ProcessInstanceCreationRecord with start instructions
      rejectionWriter.appendRejection(
          typedCommand, RejectionType.INVALID_ARGUMENT, exception.getMessage());
      responseWriter.writeRejectionOnCommand(
          typedCommand, RejectionType.INVALID_ARGUMENT, exception.getMessage());
      return ProcessingError.EXPECTED_ERROR;
    }
    return ProcessingError.UNEXPECTED_ERROR;
  }
}
