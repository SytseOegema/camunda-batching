/*
 * Messaging implementation
 *
 * Created by Sytse Oegema
 */
package io.camunda.batching.messaging;

import io.camunda.batching.messaging.serialization.AbstractDeserializer;
import java.util.Properties;
import java.util.Arrays;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MessageConsumer<T> {
  protected Logger logger;
  private Properties properties;
  private String topic;

  public MessageConsumer(String bootstrapServers, String topic, AbstractDeserializer<T> valueDeserializer) {
    this.logger = LoggerFactory.getLogger("io.camunda.zeebe.engine.adapter");
    logger.info("Logger constructed for MessageConsumer");
    this.properties = new Properties();
    this.properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    this.properties.setProperty(
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    this.properties.setProperty(
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer.getClass().getName());
    this.properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "Batch_broker");
    this.properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    this.topic = new String(topic);
  }

  public abstract void handleMessage(T message);

  public void consumeMessages() {
    logger.info("consumeMessages()");
    try (KafkaConsumer<String, T> consumer = new KafkaConsumer<String, T>(properties)) {
      consumer.subscribe(Arrays.asList(topic));
      while (true) {
        ConsumerRecords<String, T> messages = consumer.poll(20000);
        for (ConsumerRecord<String, T> message : messages) {
          handleMessage(message.value());
        }
        logger.info(String.valueOf(messages.count()) + " message received on topic " + topic);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
