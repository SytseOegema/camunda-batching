package kafka;

import play.mvc.*;
import com.google.inject.AbstractModule;
import io.camunda.batching.messaging.MessageConsumer;
import io.camunda.batching.messaging.messages.ActivityType;
import io.camunda.batching.messaging.messages.ProcessInstanceDTO;
import io.camunda.batching.messaging.messages.ProcessInstanceIntent;
import io.camunda.batching.messaging.serialization.ProcessInstanceDeserializer;
import java.util.concurrent.CompletableFuture;
import javax.inject.Singleton;
import javax.inject.Inject;
import models.ProcessInstanceModel;
import models.BatchActivityConnector.BatchActivityConnectorModel;
import models.BatchActivityConnector.BatchActivityConnectorRepository;
import models.BatchCluster.BatchClusterRepository;
import models.BatchCluster.BatchClusterState;
import models.BatchModel.BatchModelModel;
import models.BatchModel.BatchModelRepository;
import models.ProcessInstanceRepository;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
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
    super("kafka-camunda:9092", "process_instances", new ProcessInstanceDeserializer());
    // super("localhost:9092", "process_instances", new ProcessInstanceDeserializer());
    this.batchActivityConnectorRepository = batchActivityConnectorRepository;
    this.batchClusterRepository = batchClusterRepository;
    this.batchModelRepository = batchModelRepository;
    this.processInstanceRepository = processInstanceRepository;
    this.executionContext = executionContext;

    CompletableFuture.runAsync(() -> consumeMessages(), executionContext);
  }

  @Override
  public void handleMessage(ProcessInstanceDTO message) {
    OptionalInt option = processInstanceRepository.add(message).toCompletableFuture().join();
    int processInstanceId = 0;
    if(option.isPresent()) {
      processInstanceId = option.getAsInt();
    } else {
      logger.warn("Unable to store process instance in database.");
      return;
    }

    if (!isActivity(message)) {
      logger.info("not an activity");
      return;
    }

    // determine if the instance should be added to a batch cluster or
    // resume proceess flow.
    if (message.intent == ProcessInstanceIntent.READY_ELEMENT) {
      Boolean addedToCluster = false;

      Optional<List<BatchActivityConnectorModel>> connectors =
      batchActivityConnectorRepository.listByActivityId(message.elementId).join();
      if (connectors.isPresent()) {
        if (connectors.get().size() > 0) {
          logger.info("connectors pressent");
          addedToCluster = addInstanceToCluster(processInstanceId, message.processInstanceKey, connectors.get());
        }
      } else {
        logger.info("no connectors pressent");
        resumeBatchActivityFlow(new ProcessInstanceModel(message));
      }

      if (!addedToCluster) {
        resumeBatchActivityFlow(new ProcessInstanceModel(message));
      }
    }

  }

  private Boolean addInstanceToCluster(int instanceId, long processInstanceKey, List<BatchActivityConnectorModel> connectors) {
    int batchModelId = 0;
    for (BatchActivityConnectorModel connector : connectors) {
      batchModelId = connector.batchModelId;
      break;
    }
    Optional<BatchModelModel> model = batchModelRepository.get(batchModelId).toCompletableFuture().join();
    if(model.isPresent()) {
      batchClusterRepository.addInstanceToCluster(model.get(), instanceId, processInstanceKey);
      return true;
    } else {
      logger.error("Could not retrieve batch model from DB with id: " + batchModelId);
      return false;
    }
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
