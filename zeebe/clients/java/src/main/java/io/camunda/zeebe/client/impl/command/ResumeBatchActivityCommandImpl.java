/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.camunda.zeebe.client.impl.command;

import io.camunda.zeebe.client.api.JsonMapper;
import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.command.FinalCommandStep;
import io.camunda.zeebe.client.api.command.ResumeBatchActivityCommandStep1;
import io.camunda.zeebe.client.api.command.ResumeBatchActivityCommandStep1.ResumeBatchActivityCommandStep2;
import io.camunda.zeebe.client.api.response.ResumeBatchActivityResponse;
import io.camunda.zeebe.client.impl.RetriableClientFutureImpl;
import io.camunda.zeebe.client.impl.response.ResumeBatchActivityResponseImpl;
import io.camunda.zeebe.gateway.protocol.GatewayGrpc.GatewayStub;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass.ProcessInstance;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass.ResumeBatchActivityRequest;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass.ResumeBatchActivityRequest.Builder;
import io.grpc.stub.StreamObserver;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public final class ResumeBatchActivityCommandImpl
    implements ResumeBatchActivityCommandStep1, ResumeBatchActivityCommandStep2 {

  private static final Duration DEADLINE_OFFSET = Duration.ofSeconds(10);
  private final List<ProcessInstance> instanceList = new ArrayList<>();
  private final GatewayStub asyncStub;
  private final JsonMapper jsonMapper;
  private final Predicate<Throwable> retryPredicate;
  private final Builder builder;
  private final ProcessInstance.Builder processInstanceBuilder;
  private Duration requestTimeout;

  public ResumeBatchActivityCommandImpl(
      final GatewayStub asyncStub,
      final JsonMapper jsonMapper,
      final Duration requestTimeout,
      final Predicate<Throwable> retryPredicate) {
    this.asyncStub = asyncStub;
    this.jsonMapper = jsonMapper;
    this.requestTimeout = requestTimeout;
    this.retryPredicate = retryPredicate;
    builder = ResumeBatchActivityRequest.newBuilder();
    processInstanceBuilder = ProcessInstance.newBuilder();
  }

  @Override
  public FinalCommandStep<ResumeBatchActivityResponse> requestTimeout(
      final Duration requestTimeout) {
    this.requestTimeout = requestTimeout;
    return this;
  }

  @Override
  public ResumeBatchActivityCommandStep2 isBatchExecuted(final boolean isBatchExecuted) {
    builder.setIsBatchExecuted(isBatchExecuted);
    return this;
  }

  @Override
  public ResumeBatchActivityCommandStep2 addProcessInstance(
      final long processInstanceKey,
      final String bpmnProcessId,
      final int processVersion,
      final long processDefinitionKey,
      final String elementId,
      final String bpmnElementType,
      final Long flowScopeKey) {
    processInstanceBuilder.setProcessInstanceKey(processInstanceKey);
    processInstanceBuilder.setBpmnProcessId(bpmnProcessId);
    processInstanceBuilder.setProcessVersion(processVersion);
    processInstanceBuilder.setProcessDefinitionKey(processDefinitionKey);
    processInstanceBuilder.setElementId(elementId);
    processInstanceBuilder.setBpmnElementType(bpmnElementType);
    processInstanceBuilder.setFlowScopeKey(flowScopeKey);

    instanceList.add(processInstanceBuilder.build());

    return this;
  }

  @Override
  public ResumeBatchActivityCommandStep2 variables(final String variables) {
    builder.setVariables(variables);
    return this;
  }

  @Override
  public ZeebeFuture<ResumeBatchActivityResponse> send() {
    builder.addAllProcessInstances(instanceList);
    final ResumeBatchActivityRequest request = builder.build();

    final RetriableClientFutureImpl<
            ResumeBatchActivityResponse, GatewayOuterClass.ResumeBatchActivityResponse>
        future =
            new RetriableClientFutureImpl<>(
                ResumeBatchActivityResponseImpl::new,
                retryPredicate,
                streamObserver -> send(request, streamObserver));

    send(request, future);
    return future;
  }

  private void send(
      final ResumeBatchActivityRequest request,
      final StreamObserver<GatewayOuterClass.ResumeBatchActivityResponse> future) {
    asyncStub
        .withDeadlineAfter(requestTimeout.plus(DEADLINE_OFFSET).toMillis(), TimeUnit.MILLISECONDS)
        .resumeBatchActivity(request, future);
  }
}
