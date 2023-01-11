package managers;

import models.ProcessInstanceModel;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.camunda.zeebe.client.api.response.ResumeBatchActivityResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessInstanceFlowManager {

  public static void resumeBatchActivityFlow(ProcessInstanceModel instance) {
    final Logger logger = LoggerFactory.getLogger("ProcessInstanceFlowManager");
    logger.info("resumeBatchActivityFlow - dus nu uitvoeren");
    ZeebeClientBuilder clientBuilder = ZeebeClient.newClientBuilder()
      .gatewayAddress("zeebe:26500")
      .usePlaintext();

    try (final ZeebeClient client = clientBuilder.build()) {

      final ResumeBatchActivityResponse response =
        client
          .newResumeBatchActivityCommand()
          .isBatchExecuted(false)
          .addProcessInstance(
            instance.processInstanceKey,
            instance.elementInstanceKey,
            instance.bpmnProcessId,
            instance.processVersion,
            instance.processDefinitionKey,
            instance.elementId,
            instance.elementType,
            instance.flowScopeKey,
            instance.variables)
          .send()
          .join();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void continueBatchActivityFlow(ProcessInstanceModel instance) {
    final Logger logger = LoggerFactory.getLogger("ProcessInstanceFlowManager");
    logger.info("continueBatchActivityFlow - dus nu overgeslagen");
    ZeebeClientBuilder clientBuilder = ZeebeClient.newClientBuilder()
      .gatewayAddress("zeebe:26500")
      .usePlaintext();

    try (final ZeebeClient client = clientBuilder.build()) {

      final ResumeBatchActivityResponse response =
        client
          .newResumeBatchActivityCommand()
          .isBatchExecuted(true)
          .addProcessInstance(
            instance.processInstanceKey,
            instance.elementInstanceKey,
            instance.bpmnProcessId,
            instance.processVersion,
            instance.processDefinitionKey,
            instance.elementId,
            instance.elementType,
            instance.flowScopeKey,
            instance.variables)
          .send()
          .join();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
