# package

This folder contains a java-client that can be used to deploy processes,
create process instances or resume a batch activity. This client is purely for
development purposes. The Batch Controller provides the same functionality.

```
mvn package
```

# execute

```
java -cp target/java-client-1.0-SNAPSHOT-jar-with-dependencies.jar batching.client.ProcessDeployer diagram_1.bpmn


java -cp target/java-client-1.0-SNAPSHOT-jar-with-dependencies.jar batching.client.ProcessInstanceCreator diagram_1.bpmn
```
