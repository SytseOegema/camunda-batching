wsk trigger create MyKafkaTrigger -f  /whisk.system/messaging/kafkaFeed -p brokers "[\"kafka-camunda:9092\"]" -p topic input -p isJSONData true -i
