package modules;

import com.google.inject.AbstractModule;
import kafka.ProcessKafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaModule extends AbstractModule {

  @Override
  protected void configure(){
    final Logger accessLogger = LoggerFactory.getLogger("access");
    accessLogger.warn("goh kom ik hier wel?");
    bind(ProcessKafkaConsumer.class).asEagerSingleton();
  }
}
