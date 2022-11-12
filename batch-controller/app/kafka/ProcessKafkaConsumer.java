package kafka;

import com.google.inject.AbstractModule;
import io.camunda.batching.messaging.MessageConsumer;
import io.camunda.batching.messaging.messages.ProcessDTO;
import io.camunda.batching.messaging.serialization.ProcessDeserializer;
import java.util.concurrent.CompletableFuture;
import javax.inject.Singleton;
import javax.inject.Inject;
import models.ProcessRepository;

@Singleton
public class ProcessKafkaConsumer extends MessageConsumer<ProcessDTO> {
  private ProcessRepository repository;
  private KafkaProcessExecutionContext executionContext;

  @Inject
  public ProcessKafkaConsumer(ProcessRepository repository, KafkaProcessExecutionContext executionContext) {
    super("kafka:9092", "processes", new ProcessDeserializer());
    this.repository = repository;
    this.executionContext = executionContext;

    CompletableFuture.runAsync(() -> consumeMessages(), executionContext);
  }

  @Override
  public void handleMessage(ProcessDTO message) {
    logger.info("Received message with process name: " + message.name);
    repository.add(message);
  }
}
