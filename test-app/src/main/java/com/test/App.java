package com.test;

import io.camunda.batching.messaging.MessageConsumer;
import io.camunda.batching.messaging.messages.ProcessDTO;
import io.camunda.batching.messaging.serialization.ProcessDeserializer;
public class App
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );

        final ProcessDeserializer deserializer = new ProcessDeserializer();
        MessageConsumer<ProcessDTO> consumer = new MessageConsumer<ProcessDTO>("kafka:9092", "test", deserializer);
        consumer.readMessage();
    }
}
