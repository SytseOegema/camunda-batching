/*
 * Messaging implementation
 *
 * Created by Sytse Oegema
 */
package io.camunda.batching.messaging;

import io.camunda.batching.messaging.serialization.AbstractSerializer;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageProducer<T> {
  private Logger logger;
  private Properties properties;
  private String topic;

  public MessageProducer(String bootstrapServers, String topic, AbstractSerializer<T> valueSerializer) {
    this.logger = LoggerFactory.getLogger("messagingPackage.Producer[" + topic + "]");
    this.properties = new Properties();
    this.properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    this.properties.setProperty(
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    this.properties.setProperty(
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer.getClass().getName());
    this.topic = new String(topic); // is null
  }

  public void sendMessage(String key, T value) {
    logger.info("sendMessage()");
    try (KafkaProducer<String, T> producer = new KafkaProducer<>(properties)) {
      producer.send(new ProducerRecord<String, T>(topic, key, value));
      logger.info("Message produced on topic " + topic + " with key " + key);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
