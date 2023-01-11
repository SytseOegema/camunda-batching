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
import managers.ProcessInstanceActivityManager;
import managers.ProcessInstanceFlowManager;
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

    if (!ProcessInstanceActivityManager.isActivity(message)) {
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
          addedToCluster = addInstanceToCluster(processInstanceId, message, connectors.get());
        }
      } else {
        logger.info("no connectors pressent");
        ProcessInstanceFlowManager.resumeBatchActivityFlow(new ProcessInstanceModel(message));
      }

      if (!addedToCluster) {
        ProcessInstanceFlowManager.resumeBatchActivityFlow(new ProcessInstanceModel(message));
      }
    }

  }

  private Boolean addInstanceToCluster(int instanceId, ProcessInstanceDTO processInstance, List<BatchActivityConnectorModel> connectors) {
    for (BatchActivityConnectorModel connector : connectors) {
      if (ProcessInstanceActivityManager
        .instanceSatisfiesConnectorConditions(processInstance, connector)
      ) {
        Optional<BatchModelModel> model = batchModelRepository.get(connector.batchModelId).toCompletableFuture().join();
        if(model.isPresent()) {
          batchClusterRepository.addInstanceToCluster(model.get(), instanceId, processInstance.processInstanceKey);
          return true;
        } else {
          logger.error("Could not retrieve batch model from DB with id: " + connector.batchModelId);
          return false;
        }
      }
    }
    return false;
  }
}
