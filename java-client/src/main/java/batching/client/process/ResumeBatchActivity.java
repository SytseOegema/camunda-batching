package batching.client;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.camunda.zeebe.client.api.response.ResumeBatchActivityResponse;
import java.util.ArrayList;
import java.util.List;

public final class ResumeBatchActivity {

  public static void main(final String[] args) {
    final String defaultAddress = "localhost:26500";
    final ZeebeClientBuilder clientBuilder;

    if (args.length < 7) {
      System.out.println("Not enough arguments. Request cannot be executed");
    } else {
      final long processInstanceKey = Long.parseLong(args[0]);
      final String bpmnProcessId = args[1];
      final int processVersion = Integer.parseInt(args[2]);
      final long processDefinitionKey = Long.parseLong(args[3]);
      final String elementId = args[4];
      final String bpmnElementType = args[5];
      final long flowScopeKey = Long.parseLong(args[6]);

      clientBuilder = ZeebeClient.newClientBuilder().gatewayAddress(defaultAddress).usePlaintext();
      try (final ZeebeClient client = clientBuilder.build()) {

        System.out.println("Creating process instance");

        final ResumeBatchActivityResponse response =
        client
        .newResumeBatchActivityCommand()
        .isBatchExecuted(false)
        .addProcessInstance(
          processInstanceKey,
          bpmnProcessId,
          processVersion,
          processDefinitionKey,
          elementId,
          bpmnElementType,
          flowScopeKey
        )
        .send()
        .join();

        System.out.println("Process response:");
        System.out.println(response.getSuccess());
      }
    }
  }
}
