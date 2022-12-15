/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.engine.processing.bpmn;

import static io.camunda.zeebe.util.buffer.BufferUtil.bufferAsString;

import io.camunda.zeebe.engine.Loggers;
import io.camunda.zeebe.engine.adapter.ProcessInstanceProducer;
import io.camunda.zeebe.engine.api.TypedRecord;
import io.camunda.zeebe.engine.metrics.ProcessEngineMetrics;
import io.camunda.zeebe.engine.processing.bpmn.behavior.BpmnBehaviors;
import io.camunda.zeebe.engine.processing.bpmn.behavior.BpmnIncidentBehavior;
import io.camunda.zeebe.engine.processing.bpmn.behavior.BpmnStateTransitionBehavior;
import io.camunda.zeebe.engine.processing.deployment.model.element.ExecutableFlowElement;
import io.camunda.zeebe.engine.processing.streamprocessor.TypedRecordProcessor;
import io.camunda.zeebe.engine.processing.streamprocessor.sideeffect.SideEffectProducer;
import io.camunda.zeebe.engine.processing.streamprocessor.sideeffect.SideEffectQueue;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.TypedRejectionWriter;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.Writers;
import io.camunda.zeebe.engine.state.immutable.ElementInstanceState;
import io.camunda.zeebe.engine.state.immutable.ProcessState;
import io.camunda.zeebe.engine.state.immutable.VariableState;
import io.camunda.zeebe.engine.state.mutable.MutableZeebeState;
import io.camunda.zeebe.protocol.impl.encoding.MsgPackConverter;
import io.camunda.zeebe.protocol.impl.record.value.processinstance.ProcessInstanceRecord;
import io.camunda.zeebe.protocol.record.RejectionType;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.camunda.zeebe.protocol.record.value.BpmnElementType;
import java.util.Optional;
import java.util.function.Consumer;
import org.slf4j.Logger;

public final class BpmnStreamProcessor implements TypedRecordProcessor<ProcessInstanceRecord> {

  private static final Logger LOGGER = Loggers.PROCESS_PROCESSOR_LOGGER;
  private final ProcessInstanceProducer messageProducer = new ProcessInstanceProducer();
  private final BpmnElementContextImpl context = new BpmnElementContextImpl();
  private BpmnElementContext activatingContext = null;

  private final SideEffectQueue sideEffectQueue;
  private final ProcessState processState;
  private final VariableState variableState;
  private final ElementInstanceState elementInstanceState;
  private final BpmnElementProcessors processors;
  private final ProcessInstanceStateTransitionGuard stateTransitionGuard;
  private final BpmnStateTransitionBehavior stateTransitionBehavior;
  private final TypedRejectionWriter rejectionWriter;
  private final BpmnIncidentBehavior incidentBehavior;

  public BpmnStreamProcessor(
      final BpmnBehaviors bpmnBehaviors,
      final MutableZeebeState zeebeState,
      final Writers writers,
      final SideEffectQueue sideEffectQueue,
      final ProcessEngineMetrics processEngineMetrics) {
    processState = zeebeState.getProcessState();
    variableState = zeebeState.getVariableState();
    elementInstanceState = zeebeState.getElementInstanceState();

    rejectionWriter = writers.rejection();
    incidentBehavior = bpmnBehaviors.incidentBehavior();
    stateTransitionGuard = bpmnBehaviors.stateTransitionGuard();
    stateTransitionBehavior =
        new BpmnStateTransitionBehavior(
            zeebeState.getKeyGenerator(),
            bpmnBehaviors.stateBehavior(),
            processEngineMetrics,
            this::getContainerProcessor,
            writers);
    processors = new BpmnElementProcessors(bpmnBehaviors, stateTransitionBehavior);
    this.sideEffectQueue = sideEffectQueue;
  }

  private BpmnElementContainerProcessor<ExecutableFlowElement> getContainerProcessor(
      final BpmnElementType elementType) {
    return processors.getContainerProcessor(elementType);
  }

  @Override
  public void processRecord(
      final TypedRecord<ProcessInstanceRecord> record,
      final Consumer<SideEffectProducer> sideEffect) {

    // initialize
    sideEffectQueue.clear();
    sideEffect.accept(sideEffectQueue);

    final var intent = (ProcessInstanceIntent) record.getIntent();
    final var recordValue = record.getValue();

    context.init(record.getKey(), recordValue, intent);

    final var bpmnElementType = recordValue.getBpmnElementType();
    final var processor = processors.getProcessor(bpmnElementType);
    final ExecutableFlowElement element = getElement(recordValue, processor);

    stateTransitionGuard
        .isValidStateTransition(context, element)
        .ifRightOrLeft(
            ok -> {
              processEvent(intent, processor, element);
            },
            violation -> {
              LOGGER.info("violation: " + violation.getMessage());
              rejectionWriter.appendRejection(
                  record, RejectionType.INVALID_STATE, violation.getMessage());
            });
  }

  private void processEvent(
      final ProcessInstanceIntent intent,
      final BpmnElementProcessor<ExecutableFlowElement> processor,
      final ExecutableFlowElement element) {

    final String variables =
        MsgPackConverter.convertToJson(
            variableState.getVariablesAsDocument(context.getFlowScopeKey()));
    messageProducer.produceMessage(context, variables, intent);

    switch (intent) {
      case ACTIVATE_ELEMENT:
        LOGGER.info("ACTIVATE_ELEMENT");
        activatingContext = stateTransitionBehavior.transitionToActivating(context);
        logLineForContext(activatingContext);

        // if this is not an activity activate the element straightaway.
        // otherwise pause the element.
        if (!isActivity(activatingContext.getBpmnElementType())) {
          processEvent(ProcessInstanceIntent.RESUME_ELEMENT, processor, element);
        }
        break;
      case COMPLETE_ELEMENT:
        LOGGER.info("COMPLETE_ELEMENT");
        final var completingContext = stateTransitionBehavior.transitionToCompleting(context);
        processor.onComplete(element, completingContext);
        break;
      case TERMINATE_ELEMENT:
        LOGGER.info("TERMINATE_ELEMENT");
        final var terminatingContext = stateTransitionBehavior.transitionToTerminating(context);
        processor.onTerminate(element, terminatingContext);
        break;
      case RESUME_ELEMENT:
        LOGGER.info("RESUME_ELEMENT");
        if (activatingContext == null) {
          activatingContext = context;
        }
        logLineForContext(activatingContext);

        stateTransitionBehavior
            .onElementActivating(element, activatingContext)
            .ifRightOrLeft(
                ok -> processor.onActivate(element, activatingContext),
                failure -> incidentBehavior.createIncident(failure, activatingContext));

        activatingContext = null;
        break;
      default:
        throw new BpmnProcessingException(
            context,
            String.format(
                "Expected the processor '%s' to handle the event but the intent '%s' is not supported",
                processor.getClass(), intent));
    }
  }

  // private long getVariableScopeKey(final BpmnElementContext context) {
  //   final var elementInstanceKey = context.getElementInstanceKey();
  //
  //   // an inner multi-instance activity needs to read from/write to its own scope
  //   // to access the input and output element variables
  //   final var isMultiInstanceActivity =
  //       elementInstanceState.getInstance(elementInstanceKey).getMultiInstanceLoopCounter() > 0;
  //   return isMultiInstanceActivity ? elementInstanceKey : context.getFlowScopeKey();
  // }

  private void logLineForContext(BpmnElementContext context) {
    LOGGER.info("element instance key element to put in client terminal: ");
    String logLine = "";
    logLine += context.getProcessInstanceKey();
    logLine += " ";
    logLine += context.getElementInstanceKey();
    logLine += " ";
    logLine += bufferAsString(context.getBpmnProcessId());
    logLine += " ";
    logLine += context.getProcessVersion();
    logLine += " ";
    logLine += context.getProcessDefinitionKey();
    logLine += " ";
    logLine += bufferAsString(context.getElementId());
    logLine += " ";
    final Optional<String> type = context.getBpmnElementType().getElementTypeName();
    if (type.isPresent()) {
      logLine += type.get();
      logLine += " ";
    }
    logLine += context.getFlowScopeKey();
    LOGGER.info(logLine);
    LOGGER.info("----------------- END --------------");
  }

  private boolean isActivity(final BpmnElementType type) {
    boolean result = false;
    switch (type) {
      case SERVICE_TASK:
        result = true;
        break;
      case RECEIVE_TASK:
        result = true;
        break;
      case USER_TASK:
        result = true;
        break;
      case MANUAL_TASK:
        result = true;
        break;
      default:
        result = false;
        break;
    }
    return result;
  }

  private ExecutableFlowElement getElement(
      final ProcessInstanceRecord recordValue,
      final BpmnElementProcessor<ExecutableFlowElement> processor) {

    return processState.getFlowElement(
        recordValue.getProcessDefinitionKey(),
        recordValue.getElementIdBuffer(),
        processor.getType());
  }
}
