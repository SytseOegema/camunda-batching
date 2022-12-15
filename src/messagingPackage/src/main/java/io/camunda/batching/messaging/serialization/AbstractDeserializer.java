/*
 * Messaging implementation
 *
 * Created by Sytse Oegema
 */
package io.camunda.batching.messaging.serialization;

import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;

public abstract class AbstractDeserializer<T> implements Deserializer<T> {
  public abstract T deserialize(String arg0, byte[] arg1);

  @Override
  public void configure(Map<String, ?> map, boolean b) {}

  @Override
  public void close() {}
}
