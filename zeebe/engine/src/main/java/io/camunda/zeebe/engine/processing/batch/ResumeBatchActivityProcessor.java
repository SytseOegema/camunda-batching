/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.engine.processing.batch;

import static io.camunda.zeebe.util.buffer.BufferUtil.bufferAsString;

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
import io.camunda.zeebe.engine.state.deployment.DeployedProcess;
import io.camunda.zeebe.engine.state.immutable.ElementInstanceState;
import io.camunda.zeebe.engine.state.immutable.ProcessState;
import io.camunda.zeebe.engine.state.instance.ElementInstance;
import io.camunda.zeebe.msgpack.spec.MsgpackReaderException;
import io.camunda.zeebe.protocol.impl.record.value.batch.ResumeBatchActivityRecord;
import io.camunda.zeebe.protocol.impl.record.value.processinstance.ProcessInstanceRecord;
import io.camunda.zeebe.protocol.record.RejectionType;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.camunda.zeebe.protocol.record.intent.ResumeBatchActivityIntent;
import io.camunda.zeebe.protocol.record.value.batch.ProcessInstance;
import io.camunda.zeebe.util.Either;
import org.agrona.DirectBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResumeBatchActivityProcessor
    implements CommandProcessor<ResumeBatchActivityRecord> {

  private static final String ERROR_MESSAGE_NO_NONE_START_EVENT =
      "Expected to create instance of process with none start event, but there is no such event";

  private final ProcessInstanceRecord newProcessInstance = new ProcessInstanceRecord();

  private final SideEffectQueue sideEffectQueue = new SideEffectQueue();

  private final ProcessState processState;
  private final VariableBehavior variableBehavior;
  private final ElementInstanceState elementInstanceState;

  private final KeyGenerator keyGenerator;
  private final TypedCommandWriter commandWriter;
  private final TypedRejectionWriter rejectionWriter;
  private final TypedResponseWriter responseWriter;

  private final ElementActivationBehavior elementActivationBehavior;
  private final Logger logger;

  public ResumeBatchActivityProcessor(
      final ProcessState processState,
      final ElementInstanceState elementInstanceState,
      final KeyGenerator keyGenerator,
      final Writers writers,
      final BpmnBehaviors bpmnBehaviors) {
    this.processState = processState;
    variableBehavior = bpmnBehaviors.variableBehavior();
    this.elementInstanceState = elementInstanceState;
    this.keyGenerator = keyGenerator;
    commandWriter = writers.command();
    rejectionWriter = writers.rejection();
    responseWriter = writers.response();
    elementActivationBehavior = bpmnBehaviors.elementActivationBehavior();
    logger = LoggerFactory.getLogger("batch.ResumeBatchActivityProcessor");
  }

  @Override
  public boolean onCommand(
      final TypedRecord<ResumeBatchActivityRecord> command,
      final CommandControl<ResumeBatchActivityRecord> controller) {

    sideEffectQueue.clear();

    logger.info("ResumebatchActivtyProcessor - resuming a batch activity")

    final ResumeBatchActivityRecord record = command.getValue();

    if (record.hasProcessInstances()) {
      for (ProcessInstance instance : record.getProcessInstances()) {
        logger.info("instance key: " + instance.getElementInstanceKey());
        logger.info("element id: " + instance.getElementId());
        logger.info("is batch executed: " + record.getIsBatchExecuted());

        getProcess(instance)
            .ifRightOrLeft(
                process ->
                    resumeProcessFlow(
                        controller,
                        process,
                        instance,
                        instance.getElementInstanceKey(),
                        record.getIsBatchExecuted()),
                rejection -> {
                  logger.info("No process found for instance:" + instance.getElementInstanceKey());
                  controller.reject(
                      RejectionType.INVALID_STATE,
                      "No process found for instance:" + instance.getElementInstanceKey());
                });
      }
      controller.accept(ResumeBatchActivityIntent.RESUMED, record);
    } else {
      controller.reject(
          RejectionType.INVALID_STATE, "There are no process instances in the request");
    }

    return true;
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

  private void resumeProcessFlow(
      final CommandControl<ResumeBatchActivityRecord> controller,
      final DeployedProcess process,
      final ProcessInstance instance,
      final long elementInstanceKey,
      final boolean isBatchExecuted) {
    final var processInstance = initProcessInstanceRecord(instance);

    if (isBatchExecuted) {
      logger.info("Continue flow by trying to complete current activity");
      updateVariableDocument(instance)
          .ifRightOrLeft(
              instanceRetValue ->
                  commandWriter.appendFollowUpCommand(
                      elementInstanceKey, ProcessInstanceIntent.COMPLETE_ELEMENT, processInstance),
              rejection ->
                  controller.reject(
                      RejectionType.INVALID_STATE,
                      "Could not update variables:" + elementInstanceKey));
    } else {
      logger.info("Resume flow by trying to activate current activity");
      updateVariableDocument(instance)
          .ifRightOrLeft(
              instanceRetValue ->
                  commandWriter.appendFollowUpCommand(
                      elementInstanceKey, ProcessInstanceIntent.RESUME_ELEMENT, processInstance),
              rejection ->
                  controller.reject(
                      RejectionType.INVALID_STATE,
                      "Could not update variables:" + elementInstanceKey));
    }
  }

  private Either<Rejection, DeployedProcess> getProcess(final ProcessInstance instance) {
    final DirectBuffer bpmnProcessId = instance.getBpmnProcessIdBuffer();

    if (bpmnProcessId.capacity() > 0) {
      if (instance.getProcessVersion() >= 0) {
        return getProcess(bpmnProcessId, instance.getProcessVersion());
      } else {
        return getProcess(bpmnProcessId);
      }
    } else if (instance.getProcessDefinitionKey() >= 0) {
      return getProcess(instance.getProcessDefinitionKey());
    } else {
      return Either.left(
          new Rejection(
              RejectionType.INVALID_ARGUMENT,
              "Expected at least a bpmnProcessId or a key greater than -1, but none given"));
    }
  }

  private Either<Rejection, DeployedProcess> getProcess(final DirectBuffer bpmnProcessId) {
    final DeployedProcess process = processState.getLatestProcessVersionByProcessId(bpmnProcessId);
    if (process != null) {
      return Either.right(process);
    } else {
      return Either.left(
          new Rejection(
              RejectionType.NOT_FOUND,
              String.format(
                  "Expected to find process definition with process ID '%s', but none found",
                  bufferAsString(bpmnProcessId))));
    }
  }

  private Either<Rejection, DeployedProcess> getProcess(
      final DirectBuffer bpmnProcessId, final int version) {
    final DeployedProcess process =
        processState.getProcessByProcessIdAndVersion(bpmnProcessId, version);
    if (process != null) {
      return Either.right(process);
    } else {
      return Either.left(
          new Rejection(
              RejectionType.NOT_FOUND,
              String.format(
                  "Expected to find process definition with process ID '%s' and version '%d', but none found",
                  bufferAsString(bpmnProcessId), version)));
    }
  }

  private Either<Rejection, DeployedProcess> getProcess(final long key) {
    final DeployedProcess process = processState.getProcessByKey(key);
    if (process != null) {
      return Either.right(process);
    } else {
      return Either.left(
          new Rejection(
              RejectionType.NOT_FOUND,
              String.format(
                  "Expected to find process definition with key '%d', but none found", key)));
    }
  }

  private ProcessInstanceRecord initProcessInstanceRecord(final ProcessInstance processInstance) {
    newProcessInstance.reset();
    newProcessInstance.setBpmnProcessId(processInstance.getBpmnProcessId());
    newProcessInstance.setVersion(processInstance.getProcessVersion());
    newProcessInstance.setProcessDefinitionKey(processInstance.getProcessDefinitionKey());
    newProcessInstance.setProcessInstanceKey(processInstance.getProcessInstanceKey());
    newProcessInstance.setBpmnElementType(processInstance.getBpmnElementType());
    newProcessInstance.setElementId(processInstance.getElementId());
    newProcessInstance.setFlowScopeKey(processInstance.getFlowScopeKey());
    return newProcessInstance;
  }

  private Either<Rejection, ProcessInstance> updateVariableDocument(
      final ProcessInstance processInstance) {
    final ElementInstance scope =
        elementInstanceState.getInstance(processInstance.getFlowScopeKey());
    if (scope == null || scope.isTerminating() || scope.isInFinalState()) {
      final String reason =
          String.format(
              "Expected to update variables for element with key '%d', but no such element was found",
              processInstance.getFlowScopeKey());
      return Either.left(new Rejection(RejectionType.NOT_FOUND, reason));
    }

    final long processDefinitionKey = scope.getValue().getProcessDefinitionKey();
    final long processInstanceKey = scope.getValue().getProcessInstanceKey();
    final DirectBuffer bpmnProcessId = scope.getValue().getBpmnProcessIdBuffer();
    try {
      variableBehavior.mergeDocument(
          scope.getKey(),
          processDefinitionKey,
          processInstanceKey,
          bpmnProcessId,
          processInstance.getVariablesBuffer());
    } catch (final MsgpackReaderException e) {
      final String reason =
          String.format(
              "Expected document to be valid msgpack, but it could not be read: '%s'",
              e.getMessage());
      return Either.left(new Rejection(RejectionType.INVALID_STATE, reason));
    }

    final long key = keyGenerator.nextKey();
    return Either.right(processInstance);
  }

  private record Rejection(RejectionType type, String reason) {}
}
