# Faas Connector

> Camunda connector to integrate OpenWhisk

The connecor in this folder is based on the
[connector template](https://github.com/camunda/connector-template).

## How to use it
The connector should be build with the command `mvn clean package`. Thereafter
a docker image can be build using the Dockerfile using the command
`docker build --tag batching-extension/faas-connector:latest .`

The connector can be used in a process by adding the
[element-template](element-templates/template-connector.json) to the process
editor. [Read here](https://docs.camunda.io/docs/components/connectors/custom-built-connectors/connector-templates/#providing-and-using-connector-templates) how to add template to your editor.

For a Windows Desktop modeler you simply need to copy the
`template-connector.json` file to the `resources/element-templates` folder of
the Desktop modeler.

## Build

You can package the Connector by running the following command:

```bash
mvn clean package
```

This will create the following artifacts:

- A thin JAR without dependencies.
- An uber JAR containing all dependencies, potentially shaded to avoid classpath conflicts. This will not include the SDK artifacts since those are in scope `provided` and will be brought along by the respective Connector Runtime executing the Connector.


### Other resources
- https://github.com/camunda/connector-sdk/tree/main/runtime-util
- https://docs.camunda.io/docs/apis-clients/java-client-examples/job-worker-open/
- and looking into the zeebe/client options


### Shading dependencies

You can use the `maven-shade-plugin` defined in the [Maven configuration](./pom.xml) to relocate common dependencies
that are used in other Connectors and the [Connector Runtime](https://github.com/camunda-community-hub/spring-zeebe/tree/master/connector-runtime#building-connector-runtime-bundles).
This helps avoiding classpath conflicts when the Connector is executed.

Use the `relocations` configuration in the Maven Shade plugin to define the dependencies that should be shaded.
The [Maven Shade documentation](https://maven.apache.org/plugins/maven-shade-plugin/examples/class-relocation.html)
provides more details on relocations.

## API

### Input

```json
{
  "token": ".....",
  "message": "....."
}
```

### Output

```json
{
  "result": {
    "myProperty": "....."
  }
}
```

### Error codes

| Code | Description |
| - | - |
| FAIL | Message starts with 'fail' (ignoring case) |

## Test locally

Run unit tests

```bash
mvn clean verify
```

### Test with local runtime

Use the [Camunda Connector Runtime](https://github.com/camunda-community-hub/spring-zeebe/tree/master/connector-runtime#building-connector-runtime-bundles) to run your function as a local Java application.

In your IDE you can also simply navigate to the `LocalContainerRuntime` class in test scope and run it via your IDE.
If necessary, you can adjust `application.properties` in test scope.

## Element Template

The element templates can be found in the [element-templates/template-connector.json](element-templates/template-connector.json) file.
