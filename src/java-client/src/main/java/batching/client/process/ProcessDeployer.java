package batching.client;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.Process;

public final class ProcessDeployer {

  public static void main(final String[] args) {
    final String defaultAddress = "localhost:26500";
    final ZeebeClientBuilder clientBuilder;

    if (args.length == 0) {
      return;
    }
    final String resourcePath = args[0];

    clientBuilder = ZeebeClient.newClientBuilder().gatewayAddress(defaultAddress).usePlaintext();
    try (final ZeebeClient client = clientBuilder.build()) {

      final DeploymentEvent deploymentEvent =
          client
              .newDeployResourceCommand()
              .addResourceFromClasspath(resourcePath)
              .send()
              .join();

      System.out.println("Deployment created with key: " + deploymentEvent.getKey());

      for (Process process : deploymentEvent.getProcesses()) {
        System.out.println("process id : " + process.getBpmnProcessId());
        System.out.println("process definition key : " + process.getProcessDefinitionKey());
        System.out.println("process version : " + process.getVersion());
        System.out.println("process resource name : " + process.getResourceName());
      }
    }
  }
}
