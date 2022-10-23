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

public class MessageConsumer<T> {
  private Logger logger;
  private Properties properties;
  private String topic;

  public MessageConsumer(String bootstrapServers, String topic, AbstractDeserializer<T> valueDeserializer) {
    this.logger = LoggerFactory.getLogger("io.camunda.zeebe.engine.adapter");
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

  public void readMessage() {
    logger.info("readMessage()");
    try (KafkaConsumer<String, T> consumer = new KafkaConsumer<String, T>(properties)) {
      logger.info("okay");
      consumer.subscribe(Arrays.asList(topic));
      logger.info("okay good");
      while (true) {
        logger.info("in loop");
        ConsumerRecords<String, T> messages = consumer.poll(100);
        for (ConsumerRecord<String, T> message : messages) {
          logger.info("Message received " + message.key());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
