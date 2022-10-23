/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
/*
 * Camunda extension used to implement batch procesing.
 *
 * Created by Sytse Oegema
 */
package io.camunda.zeebe.engine.adapter.kafka;

import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageProducer<T> {
  private Logger logger;
  private Properties properties;
  private String topic;

  public MessageProducer(String bootstrapServers, String topic, Serializer<T> valueSerializer) {
    logger = LoggerFactory.getLogger("io.camunda.zeebe.engine.adapter");
    properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    properties.setProperty(
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.setProperty(
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer.getClass().getName());
    topic = topic;
  }

  public void sendMessage(String key, T value) {
    try (KafkaProducer<String, T> producer = new KafkaProducer<>(properties)) {
      producer.send(new ProducerRecord<String, T>(topic, key, value));
      System.out.println("Message with key " + key + " sent ! in topic " + topic);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
