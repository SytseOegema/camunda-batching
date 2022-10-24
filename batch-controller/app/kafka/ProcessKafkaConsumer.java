package kafka;

import com.google.inject.AbstractModule;
import io.camunda.batching.messaging.MessageConsumer;
import io.camunda.batching.messaging.messages.ProcessDTO;
import io.camunda.batching.messaging.serialization.ProcessDeserializer;
import javax.inject.Singleton;
import kafka.ProcessKafkaConsumer;

@Singleton
public class ProcessKafkaConsumer extends MessageConsumer<ProcessDTO> {

  public ProcessKafkaConsumer() {
    super("kafka:9092", "test", new ProcessDeserializer());
    consumeMessages();
  }

  @Override
  public void handleMessage(ProcessDTO message) {
    logger.info("handleMessage()");
    logger.info(message.name);
  }
}
