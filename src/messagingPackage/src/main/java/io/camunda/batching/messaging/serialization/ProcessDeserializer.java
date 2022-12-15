/*
 * Messaging implementation
 *
 * Created by Sytse Oegema
 */
package io.camunda.batching.messaging.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.batching.messaging.messages.ProcessDTO;

public final class ProcessDeserializer extends AbstractDeserializer<ProcessDTO> {
  @Override
  public ProcessDTO deserialize(String arg0, byte[] arg1) {
    ObjectMapper mapper = new ObjectMapper();
    ProcessDTO object = null;
    try {
      object = mapper.readValue(arg1, ProcessDTO.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return object;
  }
}
