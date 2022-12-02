package kafka;

import play.mvc.*;
import com.google.inject.AbstractModule;
import io.camunda.batching.messaging.MessageConsumer;
import io.camunda.batching.messaging.messages.ProcessInstanceDTO;
import io.camunda.batching.messaging.messages.ActivityType;
import io.camunda.batching.messaging.serialization.ProcessInstanceDeserializer;
import java.util.concurrent.CompletableFuture;
import javax.inject.Singleton;
import javax.inject.Inject;
import models.ProcessInstanceModel;
import models.BatchActivityConnector.BatchActivityConnectorModel;
import models.BatchActivityConnector.BatchActivityConnectorRepository;
import models.BatchCluster.BatchClusterRepository;
import models.BatchModel.BatchModelModel;
import models.BatchModel.BatchModelRepository;
import models.ProcessInstanceRepository;
import java.util.List;
import java.util.Optional;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.camunda.zeebe.client.api.response.ResumeBatchActivityResponse;


@Singleton
public class ProcessInstanceKafkaConsumer extends MessageConsumer<ProcessInstanceDTO> {
  private BatchActivityConnectorRepository batchActivityConnectorRepository;
  private BatchClusterRepository batchClusterRepository;
  private BatchModelRepository batchModelRepository;
  private ProcessInstanceRepository processInstanceRepository;
  private KafkaProcessInstanceExecutionContext executionContext;

  @Inject
  public ProcessInstanceKafkaConsumer(
      BatchActivityConnectorRepository batchActivityConnectorRepository,
      BatchClusterRepository batchClusterRepository,
      BatchModelRepository batchModelRepository,
      ProcessInstanceRepository processInstanceRepository,
      KafkaProcessInstanceExecutionContext executionContext
  ) {
    super("kafka:9092", "process_instances", new ProcessInstanceDeserializer());
    this.batchActivityConnectorRepository = batchActivityConnectorRepository;
    this.batchClusterRepository = batchClusterRepository;
    this.batchModelRepository = batchModelRepository;
    this.processInstanceRepository = processInstanceRepository;
    this.executionContext = executionContext;

    CompletableFuture.runAsync(() -> consumeMessages(), executionContext);
  }

  @Override
  public void handleMessage(ProcessInstanceDTO message) {
    logger.info("Received message with process instance key: " + message.processInstanceKey);
    processInstanceRepository.add(message);

    if (!isActivity(message)) {
      logger.info("not an activity");
      return;
    }

    Boolean addedToCluster = false;

    Optional<List<BatchActivityConnectorModel>> connectors =
      batchActivityConnectorRepository.listByActivityId(message.elementId).join();
    if (connectors.isPresent()) {
      if (connectors.get().size() > 0) {
        logger.info("connectors pressent");
        addedToCluster = addInstanceToCluster(new ProcessInstanceModel(message), connectors.get());
      }
    } else {
      logger.info("no connectors pressent");
      resumeBatchActivityFlow(new ProcessInstanceModel(message));
    }

    if (!addedToCluster) {
      resumeBatchActivityFlow(new ProcessInstanceModel(message));
    }
  }

  private Boolean addInstanceToCluster(ProcessInstanceModel instance, List<BatchActivityConnectorModel> connectors) {
    for (BatchActivityConnectorModel connector : connectors) {
      logger.info("connector id:" + connector.connectorId);
      logger.info("connector activityId:" + connector.activityId);
    }
    continueBatchActivityFlow(instance);
    return true;
  }

  private boolean isActivity(ProcessInstanceDTO message) {
    boolean result = false;
    switch (message.elementType) {
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

  private void resumeBatchActivityFlow(ProcessInstanceModel instance) {
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

  private void continueBatchActivityFlow(ProcessInstanceModel instance) {
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
