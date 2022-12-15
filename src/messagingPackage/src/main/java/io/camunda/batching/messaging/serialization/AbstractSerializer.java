/*
 * Messaging implementation
 *
 * Created by Sytse Oegema
 */
package io.camunda.batching.messaging.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.apache.kafka.common.serialization.Serializer;

public abstract class AbstractSerializer<T> implements Serializer<T> {
  @Override
  public void configure(Map<String, ?> map, boolean b) {}

  @Override
  public byte[] serialize(String arg0, T arg1) {
    byte[] retVal = null;
    final ObjectMapper objectMapper = new ObjectMapper();
    try {
      retVal = objectMapper.writeValueAsString(arg1).getBytes();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return retVal;
  }

  @Override
  public void close() {}
}
