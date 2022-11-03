/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.engine.processing.batch;

import static io.camunda.zeebe.engine.state.instance.TimerInstance.NO_ELEMENT_INSTANCE;

import io.camunda.zeebe.engine.api.TypedRecord;
import io.camunda.zeebe.engine.processing.bpmn.behavior.BpmnBehaviors;
import io.camunda.zeebe.engine.processing.common.CatchEventBehavior;
import io.camunda.zeebe.engine.processing.common.ExpressionProcessor;
import io.camunda.zeebe.engine.processing.common.ExpressionProcessor.EvaluationException;
import io.camunda.zeebe.engine.processing.common.Failure;
import io.camunda.zeebe.engine.processing.deployment.distribute.DeploymentDistributionBehavior;
import io.camunda.zeebe.engine.processing.deployment.distribute.DeploymentDistributionCommandSender;
import io.camunda.zeebe.engine.processing.deployment.model.element.ExecutableCatchEventElement;
import io.camunda.zeebe.engine.processing.deployment.model.element.ExecutableStartEvent;
import io.camunda.zeebe.engine.processing.deployment.transform.DeploymentTransformer;
import io.camunda.zeebe.engine.processing.streamprocessor.TypedRecordProcessor;
import io.camunda.zeebe.engine.processing.streamprocessor.sideeffect.SideEffectProducer;
import io.camunda.zeebe.engine.processing.streamprocessor.sideeffect.SideEffectQueue;
import io.camunda.zeebe.engine.processing.streamprocessor.sideeffect.SideEffects;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.StateWriter;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.TypedRejectionWriter;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.TypedResponseWriter;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.Writers;
import io.camunda.zeebe.engine.state.KeyGenerator;
import io.camunda.zeebe.engine.state.immutable.ProcessState;
import io.camunda.zeebe.engine.state.immutable.TimerInstanceState;
import io.camunda.zeebe.engine.state.immutable.ZeebeState;
import io.camunda.zeebe.engine.state.instance.TimerInstance;
import io.camunda.zeebe.model.bpmn.util.time.Timer;
import io.camunda.zeebe.protocol.impl.record.value.batch.ResumeBatchActivityRecord;
import io.camunda.zeebe.protocol.impl.record.value.deployment.DeploymentRecord;
import io.camunda.zeebe.protocol.impl.record.value.deployment.ProcessMetadata;
import io.camunda.zeebe.util.Either;
import java.util.List;
import java.util.function.Consumer;
import org.agrona.DirectBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResumeBatchActivityProcessor
    implements TypedRecordProcessor<ResumeBatchActivityRecord> {

  private static final String COULD_NOT_CREATE_TIMER_MESSAGE =
      "Expected to create timer for start event, but encountered the following error: %s";

  private final SideEffectQueue sideEffects = new SideEffectQueue();

  private final DeploymentTransformer deploymentTransformer;
  private final ProcessState processState;
  private final TimerInstanceState timerInstanceState;
  private final CatchEventBehavior catchEventBehavior;
  private final KeyGenerator keyGenerator;
  private final ExpressionProcessor expressionProcessor;
  private final StateWriter stateWriter;
  private final DeploymentDistributionBehavior deploymentDistributionBehavior;
  private final TypedRejectionWriter rejectionWriter;
  private final TypedResponseWriter responseWriter;

  public ResumeBatchActivityProcessor(
      final ZeebeState zeebeState,
      final BpmnBehaviors bpmnBehaviors,
      final int partitionsCount,
      final Writers writers,
      final DeploymentDistributionCommandSender deploymentDistributionCommandSender,
      final KeyGenerator keyGenerator) {

    final Logger logger =
        LoggerFactory.getLogger("io.camunda.zeebe.engine.ResumeBatchActivityProcessor");
    logger.info("ResumeBatchActivityProcessor()");

    processState = zeebeState.getProcessState();
    timerInstanceState = zeebeState.getTimerState();
    this.keyGenerator = keyGenerator;
    stateWriter = writers.state();
    rejectionWriter = writers.rejection();
    responseWriter = writers.response();
    catchEventBehavior = bpmnBehaviors.catchEventBehavior();
    expressionProcessor = bpmnBehaviors.expressionBehavior();
    deploymentTransformer =
        new DeploymentTransformer(stateWriter, zeebeState, expressionProcessor, keyGenerator);
    deploymentDistributionBehavior =
        new DeploymentDistributionBehavior(
            writers, partitionsCount, deploymentDistributionCommandSender);
  }

  @Override
  public void processRecord(
      final TypedRecord<ResumeBatchActivityRecord> command,
      final Consumer<SideEffectProducer> sideEffect) {

    final Logger logger =
        LoggerFactory.getLogger("io.camunda.zeebe.engine.ResumeBatchActivityProcessor");
    logger.info("ResumeBatchActivityProcessor.processRecord()");

    logger.info("well in processRecord van ResumeBatchActivityProcessor");
    sideEffect.accept(sideEffects);
    logger.info("1");
    final ResumeBatchActivityRecord resumeEvent = command.getValue();
    logger.info("2");
    // resumeEvent.setBpmnProcessId("joh dit werkt als een tiet!");
    logger.info("3");

    final long key = keyGenerator.nextKey();
    logger.info("4");
    // responseWriter.writeEventOnCommand(
    //     key, ResumeBatchActivityIntent.RESUMED, resumeEvent, command);
    //
    // stateWriter.appendFollowUpEvent(key, ResumeBatchActivityIntent.RESUMED, resumeEvent);

    logger.info("5");

    // final boolean accepted = deploymentTransformer.transform(deploymentEvent);
    // if (accepted) {
    //   final long key = keyGenerator.nextKey();
    //
    //   try {
    //     createTimerIfTimerStartEvent(command, sideEffects);
    //   } catch (final RuntimeException e) {
    //     final String reason = String.format(COULD_NOT_CREATE_TIMER_MESSAGE, e.getMessage());
    //     responseWriter.writeRejectionOnCommand(command, RejectionType.PROCESSING_ERROR, reason);
    //     rejectionWriter.appendRejection(command, RejectionType.PROCESSING_ERROR, reason);
    //     return;
    //   }
    //
    //   responseWriter.writeEventOnCommand(key, DeploymentIntent.CREATED, deploymentEvent,
    // command);
    //
    //   stateWriter.appendFollowUpEvent(key, DeploymentIntent.CREATED, deploymentEvent);
    //
    //   deploymentDistributionBehavior.distributeDeployment(deploymentEvent, key);
    //
    // } else {
    //   responseWriter.writeRejectionOnCommand(
    //       command,
    //       deploymentTransformer.getRejectionType(),
    //       deploymentTransformer.getRejectionReason());
    //   rejectionWriter.appendRejection(
    //       command,
    //       deploymentTransformer.getRejectionType(),
    //       deploymentTransformer.getRejectionReason());
    // }
  }

  private void createTimerIfTimerStartEvent(
      final TypedRecord<DeploymentRecord> record, final SideEffects sideEffects) {
    for (final ProcessMetadata processMetadata : record.getValue().processesMetadata()) {
      if (!processMetadata.isDuplicate()) {
        final List<ExecutableStartEvent> startEvents =
            processState.getProcessByKey(processMetadata.getKey()).getProcess().getStartEvents();

        unsubscribeFromPreviousTimers(processMetadata);
        subscribeToTimerStartEventIfExists(sideEffects, processMetadata, startEvents);
      }
    }
  }

  private void subscribeToTimerStartEventIfExists(
      final SideEffects sideEffects,
      final ProcessMetadata processMetadata,
      final List<ExecutableStartEvent> startEvents) {
    for (final ExecutableCatchEventElement startEvent : startEvents) {
      if (startEvent.isTimer()) {
        // There are no variables when there is no process instance yet,
        // we use a negative scope key to indicate this
        final long scopeKey = -1L;
        final Either<Failure, Timer> timerOrError =
            startEvent.getTimerFactory().apply(expressionProcessor, scopeKey);
        if (timerOrError.isLeft()) {
          // todo(#4323): deal with this exceptional case without throwing an exception
          throw new EvaluationException(timerOrError.getLeft().getMessage());
        }

        catchEventBehavior.subscribeToTimerEvent(
            NO_ELEMENT_INSTANCE,
            NO_ELEMENT_INSTANCE,
            processMetadata.getKey(),
            startEvent.getId(),
            timerOrError.get(),
            sideEffects);
      }
    }
  }

  private void unsubscribeFromPreviousTimers(final ProcessMetadata processRecord) {
    timerInstanceState.forEachTimerForElementInstance(
        NO_ELEMENT_INSTANCE, timer -> unsubscribeFromPreviousTimer(processRecord, timer));
  }

  private void unsubscribeFromPreviousTimer(
      final ProcessMetadata processMetadata, final TimerInstance timer) {
    final DirectBuffer timerBpmnId =
        processState.getProcessByKey(timer.getProcessDefinitionKey()).getBpmnProcessId();

    if (timerBpmnId.equals(processMetadata.getBpmnProcessIdBuffer())) {
      catchEventBehavior.unsubscribeFromTimerEvent(timer);
    }
  }
}
