/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.it.startup;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.qa.util.testcontainers.ContainerLogsDumper;
import io.camunda.zeebe.qa.util.testcontainers.ZeebeTestContainerDefaults;
import io.zeebe.containers.ZeebeBrokerContainer;
import io.zeebe.containers.ZeebeGatewayContainer;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Tests a small deployment of one standalone broker and gateway, running "rootless". While this
 * cannot guarantee OpenShift compatibility, it's the most common compatibility issue.
 *
 * <p>See <a
 * href="https://docs.openshift.com/container-platform/latest/openshift_images/create-images.html">here</a>
 * for more.
 *
 * <p>From their docs: By default, OpenShift Container Platform runs containers using an arbitrarily
 * assigned user ID. This provides additional security against processes escaping the container due
 * to a container engine vulnerability and thereby achieving escalated permissions on the host node.
 * For an image to support running as an arbitrary user, directories and files that are written to
 * by processes in the image must be owned by the root group and be read/writable by that group.
 * Files to be executed must also have "group" execute permissions.
 *
 * <p>You can read more about UIDs/GIDs <a
 * href="https://cloud.redhat.com/blog/a-guide-to-openshift-and-uids">here</a>.
 */
@Testcontainers
final class RootlessImageTest {
  private final Network network = Network.newNetwork();

  @Container
  private final ZeebeBrokerContainer broker =
      new ZeebeBrokerContainer(ZeebeTestContainerDefaults.defaultTestImage())
          .withNetwork(network)
          .withCreateContainerCmdModifier(cmd -> cmd.withUser("1000620000:0"));

  @Container
  private final ZeebeGatewayContainer gateway =
      new ZeebeGatewayContainer(ZeebeTestContainerDefaults.defaultTestImage())
          .withNetwork(network)
          .withCreateContainerCmdModifier(cmd -> cmd.withUser("1000620000:0"))
          .dependsOn(broker)
          .withEnv(
              "ZEEBE_GATEWAY_CLUSTER_INITIALCONTACTPOINTS", broker.getInternalClusterAddress());

  @SuppressWarnings("unused")
  @RegisterExtension
  final ContainerLogsDumper logsWatcher =
      new ContainerLogsDumper(() -> Map.of("broker", broker, "gateway", gateway));

  @Test
  void smokeTest() {
    // given
    final var process = Bpmn.createExecutableProcess("process").startEvent().endEvent().done();
    final ProcessInstanceResult result;
    try (final ZeebeClient client =
        ZeebeClient.newClientBuilder()
            .usePlaintext()
            .gatewayAddress(gateway.getExternalGatewayAddress())
            .build()) {
      // when
      client
          .newDeployResourceCommand()
          .addProcessModel(process, "process.bpmn")
          .send()
          .join(10, TimeUnit.SECONDS);
      result =
          client
              .newCreateInstanceCommand()
              .bpmnProcessId("process")
              .latestVersion()
              .withResult()
              .send()
              .join(10, TimeUnit.SECONDS);
    }

    // then
    assertThat(result)
        .isNotNull()
        .extracting(ProcessInstanceResult::getBpmnProcessId)
        .isEqualTo("process");
  }
}
