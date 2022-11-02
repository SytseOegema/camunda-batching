package batching.client;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.camunda.zeebe.client.api.response.ResumeBatchActivityResponse;

public final class ResumeBatchActivity {

  public static void main(final String[] args) {
    final String defaultAddress = "localhost:26500";
    final ZeebeClientBuilder clientBuilder;


    final String bpmnProcessId = "Eten";
    if (args.length > 0) {
      final String resourcePath = args[0];
    }

    clientBuilder = ZeebeClient.newClientBuilder().gatewayAddress(defaultAddress).usePlaintext();
    try (final ZeebeClient client = clientBuilder.build()) {

      System.out.println("Creating process instance");

      final ResumeBatchActivityResponse response =
        client
          .newResumeBatchActivityCommand()
          .bpmnProcessId(bpmnProcessId)
          .send()
          .join();

        System.out.println(
          "Process instance created with key: " + response.getBpmnProcessId ());
    }
  }
}
