/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.engine.processing.deployment.model.transformer;

import io.camunda.zeebe.engine.processing.deployment.model.element.ExecutableJobWorkerElement;
import io.camunda.zeebe.engine.processing.deployment.model.element.ExecutableProcess;
import io.camunda.zeebe.engine.processing.deployment.model.transformation.ModelElementTransformer;
import io.camunda.zeebe.engine.processing.deployment.model.transformation.TransformContext;
import io.camunda.zeebe.engine.processing.deployment.model.transformer.zeebe.TaskDefinitionTransformer;
import io.camunda.zeebe.engine.processing.deployment.model.transformer.zeebe.TaskHeadersTransformer;
import io.camunda.zeebe.logstreams.impl.Loggers;
import io.camunda.zeebe.model.bpmn.instance.FlowElement;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeTaskDefinition;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeTaskHeaders;
import org.slf4j.Logger;

public final class JobWorkerElementTransformer<T extends FlowElement>
    implements ModelElementTransformer<T> {

  private static final Logger LOG = Loggers.PROCESSOR_LOGGER;

  private final Class<T> type;
  private final TaskDefinitionTransformer taskDefinitionTransformer =
      new TaskDefinitionTransformer();
  private final TaskHeadersTransformer taskHeadersTransformer = new TaskHeadersTransformer();

  public JobWorkerElementTransformer(final Class<T> type) {
    this.type = type;
  }

  @Override
  public Class<T> getType() {
    return type;
  }

  @Override
  public void transform(final T element, final TransformContext context) {
    // LOG.info("Transforming jobworker task!\n");
    // LOG.info(element.getName());

    final ExecutableProcess process = context.getCurrentProcess();
    final ExecutableJobWorkerElement jobWorkerElement =
        process.getElementById(element.getId(), ExecutableJobWorkerElement.class);

    final var taskDefinition = element.getSingleExtensionElement(ZeebeTaskDefinition.class);
    taskDefinitionTransformer.transform(jobWorkerElement, context, taskDefinition);

    final var taskHeaders = element.getSingleExtensionElement(ZeebeTaskHeaders.class);
    taskHeadersTransformer.transform(jobWorkerElement, taskHeaders, element);

    // checkBatchTaskSet(jobWorkerElement);
  }

  public void checkBatchTaskSet(final ExecutableJobWorkerElement event) {
    LOG.info("jaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    LOG.info(String.valueOf(event.getJobWorkerProperties().getIsBatchTask()));
  }
}
