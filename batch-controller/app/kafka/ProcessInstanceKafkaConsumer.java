package kafka;

import com.google.inject.AbstractModule;
import io.camunda.batching.messaging.MessageConsumer;
import io.camunda.batching.messaging.messages.ProcessInstanceDTO;
import io.camunda.batching.messaging.serialization.ProcessInstanceDeserializer;
import java.util.concurrent.CompletableFuture;
import javax.inject.Singleton;
import javax.inject.Inject;
import models.ProcessInstanceRepository;

@Singleton
public class ProcessInstanceKafkaConsumer extends MessageConsumer<ProcessInstanceDTO> {
  private ProcessInstanceRepository repository;
  private KafkaProcessInstanceExecutionContext executionContext;

  @Inject
  public ProcessInstanceKafkaConsumer(ProcessInstanceRepository repository, KafkaProcessInstanceExecutionContext executionContext) {
    super("kafka:9092", "process_instances", new ProcessInstanceDeserializer());
    this.repository = repository;
    this.executionContext = executionContext;

    CompletableFuture.runAsync(() -> consumeMessages(), executionContext);
  }

  @Override
  public void handleMessage(ProcessInstanceDTO message) {
    logger.info("Received message with process instance key: " + message.processInstanceKey);
    repository.add(message);
  }
}
