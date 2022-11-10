/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.engine.processing;

import static io.camunda.zeebe.protocol.record.intent.DeploymentIntent.CREATE;

import io.camunda.zeebe.engine.metrics.JobMetrics;
import io.camunda.zeebe.engine.metrics.ProcessEngineMetrics;
import io.camunda.zeebe.engine.processing.batch.ResumeBatchActivityProcessor;
import io.camunda.zeebe.engine.processing.bpmn.behavior.BpmnBehaviorsImpl;
import io.camunda.zeebe.engine.processing.deployment.DeploymentCreateProcessor;
import io.camunda.zeebe.engine.processing.deployment.distribute.CompleteDeploymentDistributionProcessor;
import io.camunda.zeebe.engine.processing.deployment.distribute.DeploymentDistributeProcessor;
import io.camunda.zeebe.engine.processing.deployment.distribute.DeploymentDistributionCommandSender;
import io.camunda.zeebe.engine.processing.deployment.distribute.DeploymentRedistributor;
import io.camunda.zeebe.engine.processing.incident.IncidentEventProcessors;
import io.camunda.zeebe.engine.processing.job.JobEventProcessors;
import io.camunda.zeebe.engine.processing.message.MessageEventProcessors;
import io.camunda.zeebe.engine.processing.message.command.SubscriptionCommandSender;
import io.camunda.zeebe.engine.processing.streamprocessor.TypedRecordProcessor;
import io.camunda.zeebe.engine.processing.streamprocessor.TypedRecordProcessorContext;
import io.camunda.zeebe.engine.processing.streamprocessor.TypedRecordProcessors;
import io.camunda.zeebe.engine.processing.streamprocessor.sideeffect.SideEffectQueue;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.Writers;
import io.camunda.zeebe.engine.processing.timer.DueDateTimerChecker;
import io.camunda.zeebe.engine.state.KeyGenerator;
import io.camunda.zeebe.engine.state.immutable.ZeebeState;
import io.camunda.zeebe.engine.state.migration.DbMigrationController;
import io.camunda.zeebe.engine.state.mutable.MutableZeebeState;
import io.camunda.zeebe.protocol.impl.record.value.processinstance.ProcessInstanceRecord;
import io.camunda.zeebe.protocol.record.RecordType;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.DeploymentDistributionIntent;
import io.camunda.zeebe.protocol.record.intent.DeploymentIntent;
import io.camunda.zeebe.protocol.record.intent.ResumeBatchActivityIntent;
import io.camunda.zeebe.util.FeatureFlags;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EngineProcessors {

  private EngineProcessors() {}

  public static TypedRecordProcessors createEngineProcessors(
      final TypedRecordProcessorContext typedRecordProcessorContext,
      final int partitionsCount,
      final SubscriptionCommandSender subscriptionCommandSender,
      final DeploymentDistributionCommandSender deploymentDistributionCommandSender,
      final Consumer<String> onJobsAvailableCallback,
      final FeatureFlags featureFlags) {

    final Logger logger =
        LoggerFactory.getLogger("io.camunda.zeebe.engine.ResumeBatchActivityProcessor");
    logger.info("createEngineProcessors()");

    final MutableZeebeState zeebeState = typedRecordProcessorContext.getZeebeState();
    final var writers = typedRecordProcessorContext.getWriters();
    final TypedRecordProcessors typedRecordProcessors =
        TypedRecordProcessors.processors(zeebeState.getKeyGenerator(), writers);

    // register listener that handles migrations immediately, so it is the first to be called
    typedRecordProcessors.withListener(new DbMigrationController(zeebeState));

    typedRecordProcessors.withListener(zeebeState);

    final int partitionId = typedRecordProcessorContext.getPartitionId();

    final DueDateTimerChecker timerChecker =
        new DueDateTimerChecker(zeebeState.getTimerState(), featureFlags);

    final var jobMetrics = new JobMetrics(partitionId);
    final var processEngineMetrics = new ProcessEngineMetrics(zeebeState.getPartitionId());
    final var sideEffectQueue = new SideEffectQueue();

    final BpmnBehaviorsImpl bpmnBehaviors =
        createBehaviors(
            zeebeState,
            writers,
            subscriptionCommandSender,
            partitionsCount,
            timerChecker,
            jobMetrics,
            processEngineMetrics,
            sideEffectQueue);

    addBatchRelatedProcessorAndServices(
        bpmnBehaviors,
        zeebeState,
        typedRecordProcessors,
        writers,
        partitionsCount,
        deploymentDistributionCommandSender,
        zeebeState.getKeyGenerator());

    addDeploymentRelatedProcessorAndServices(
        bpmnBehaviors,
        zeebeState,
        typedRecordProcessors,
        writers,
        partitionsCount,
        deploymentDistributionCommandSender,
        zeebeState.getKeyGenerator());
    addMessageProcessors(
        bpmnBehaviors, subscriptionCommandSender, zeebeState, typedRecordProcessors, writers);

    final TypedRecordProcessor<ProcessInstanceRecord> bpmnStreamProcessor =
        addProcessProcessors(
            zeebeState,
            bpmnBehaviors,
            typedRecordProcessors,
            subscriptionCommandSender,
            writers,
            timerChecker,
            sideEffectQueue);

    JobEventProcessors.addJobProcessors(
        typedRecordProcessors,
        zeebeState,
        onJobsAvailableCallback,
        bpmnBehaviors,
        writers,
        jobMetrics);

    addIncidentProcessors(zeebeState, bpmnStreamProcessor, typedRecordProcessors, writers);

    final TypedRecordProcessor processor =
        typedRecordProcessors
            .getRecordProcessorMap()
            .get(
                RecordType.COMMAND,
                ValueType.RESUME_BATCH_ACTIVITY,
                ResumeBatchActivityIntent.RESUME.value());

    if (processor == null) {
      logger.info("och nee null");
    } else {
      logger.info("toch wel success");
    }

    return typedRecordProcessors;
  }

  private static BpmnBehaviorsImpl createBehaviors(
      final MutableZeebeState zeebeState,
      final Writers writers,
      final SubscriptionCommandSender subscriptionCommandSender,
      final int partitionsCount,
      final DueDateTimerChecker timerChecker,
      final JobMetrics jobMetrics,
      final ProcessEngineMetrics processEngineMetrics,
      final SideEffectQueue sideEffectQueue) {
    return new BpmnBehaviorsImpl(
        sideEffectQueue,
        zeebeState,
        writers,
        jobMetrics,
        processEngineMetrics,
        subscriptionCommandSender,
        partitionsCount,
        timerChecker);
  }

  private static TypedRecordProcessor<ProcessInstanceRecord> addProcessProcessors(
      final MutableZeebeState zeebeState,
      final BpmnBehaviorsImpl bpmnBehaviors,
      final TypedRecordProcessors typedRecordProcessors,
      final SubscriptionCommandSender subscriptionCommandSender,
      final Writers writers,
      final DueDateTimerChecker timerChecker,
      final SideEffectQueue sideEffectQueue) {
    return ProcessEventProcessors.addProcessProcessors(
        zeebeState,
        bpmnBehaviors,
        typedRecordProcessors,
        subscriptionCommandSender,
        timerChecker,
        writers,
        sideEffectQueue);
  }

  private static void addBatchRelatedProcessorAndServices(
      final BpmnBehaviorsImpl bpmnBehaviors,
      final ZeebeState zeebeState,
      final TypedRecordProcessors typedRecordProcessors,
      final Writers writers,
      final int partitionsCount,
      final DeploymentDistributionCommandSender deploymentDistributionCommandSender,
      final KeyGenerator keyGenerator) {

    final var processor =
        new ResumeBatchActivityProcessor(
            zeebeState.getProcessState(),
            zeebeState.getElementInstanceState(),
            keyGenerator,
            writers,
            bpmnBehaviors);
    typedRecordProcessors.onCommand(
        ValueType.RESUME_BATCH_ACTIVITY, ResumeBatchActivityIntent.RESUME, processor);
  }

  private static void addDeploymentRelatedProcessorAndServices(
      final BpmnBehaviorsImpl bpmnBehaviors,
      final ZeebeState zeebeState,
      final TypedRecordProcessors typedRecordProcessors,
      final Writers writers,
      final int partitionsCount,
      final DeploymentDistributionCommandSender deploymentDistributionCommandSender,
      final KeyGenerator keyGenerator) {

    // on deployment partition CREATE Command is received and processed
    // it will cause a distribution to other partitions
    final var processor =
        new DeploymentCreateProcessor(
            zeebeState,
            bpmnBehaviors,
            partitionsCount,
            writers,
            deploymentDistributionCommandSender,
            keyGenerator);
    typedRecordProcessors.onCommand(ValueType.DEPLOYMENT, CREATE, processor);

    // periodically retries deployment distribution
    final var deploymentRedistributor =
        new DeploymentRedistributor(
            deploymentDistributionCommandSender, zeebeState.getDeploymentState());
    typedRecordProcessors.withListener(deploymentRedistributor);

    // on other partitions DISTRIBUTE command is received and processed
    final DeploymentDistributeProcessor deploymentDistributeProcessor =
        new DeploymentDistributeProcessor(
            zeebeState.getProcessState(),
            zeebeState.getMessageStartEventSubscriptionState(),
            deploymentDistributionCommandSender,
            writers,
            keyGenerator);
    typedRecordProcessors.onCommand(
        ValueType.DEPLOYMENT, DeploymentIntent.DISTRIBUTE, deploymentDistributeProcessor);

    // completes the deployment distribution
    final var completeDeploymentDistributionProcessor =
        new CompleteDeploymentDistributionProcessor(zeebeState.getDeploymentState(), writers);
    typedRecordProcessors.onCommand(
        ValueType.DEPLOYMENT_DISTRIBUTION,
        DeploymentDistributionIntent.COMPLETE,
        completeDeploymentDistributionProcessor);
  }

  private static void addIncidentProcessors(
      final ZeebeState zeebeState,
      final TypedRecordProcessor<ProcessInstanceRecord> bpmnStreamProcessor,
      final TypedRecordProcessors typedRecordProcessors,
      final Writers writers) {
    IncidentEventProcessors.addProcessors(
        typedRecordProcessors, zeebeState, bpmnStreamProcessor, writers);
  }

  private static void addMessageProcessors(
      final BpmnBehaviorsImpl bpmnBehaviors,
      final SubscriptionCommandSender subscriptionCommandSender,
      final MutableZeebeState zeebeState,
      final TypedRecordProcessors typedRecordProcessors,
      final Writers writers) {
    MessageEventProcessors.addMessageProcessors(
        bpmnBehaviors.eventTriggerBehavior(),
        typedRecordProcessors,
        zeebeState,
        subscriptionCommandSender,
        writers);
  }
}
