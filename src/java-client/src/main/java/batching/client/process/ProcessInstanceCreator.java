package batching.client;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;

public final class ProcessInstanceCreator {

  public static void main(final String[] args) {
    final String defaultAddress = "localhost:26500";
    final ZeebeClientBuilder clientBuilder;


    String bpmnProcessId = "Eten";
    if (args.length > 0) {
      bpmnProcessId = args[0];
    }

    clientBuilder = ZeebeClient.newClientBuilder().gatewayAddress(defaultAddress).usePlaintext();
    try (final ZeebeClient client = clientBuilder.build()) {

      System.out.println("Creating process instance");

      final ProcessInstanceEvent processInstanceEvent =
        client
          .newCreateInstanceCommand()
          .bpmnProcessId(bpmnProcessId)
          .latestVersion()
          .send()
          .join();

        System.out.println(
          "Process instance created with key: " + processInstanceEvent.getProcessInstanceKey());
    }
  }
}
