/*
 * Messaging implementation
 *
 * Created by Sytse Oegema
 */
package io.camunda.batching.messaging.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.batching.messaging.messages.ProcessInstanceDTO;

public final class ProcessInstanceDeserializer extends AbstractDeserializer<ProcessInstanceDTO> {
  @Override
  public ProcessInstanceDTO deserialize(String arg0, byte[] arg1) {
    ObjectMapper mapper = new ObjectMapper();
    ProcessInstanceDTO object = null;
    try {
      object = mapper.readValue(arg1, ProcessInstanceDTO.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return object;
  }
}
