FROM eclipse-temurin:17.0.5_8-jre

# Configure the connector runtime version
ARG SPRING_ZEEBE_VERSION=8.1.9

RUN mkdir /opt/app
# Download Spring Zeebe Connector Runtime from Maven Central
ADD https://repo1.maven.org/maven2/io/camunda/spring-zeebe-connector-runtime/${SPRING_ZEEBE_VERSION}/spring-zeebe-connector-runtime-${SPRING_ZEEBE_VERSION}-with-dependencies.jar /opt/app/

# add my connector to the runtime thingy
ADD ./target/connector-template-0.1.0-SNAPSHOT-with-dependencies.jar /opt/app/

# Use entry point to allow downstream images to add JVM arguments using CMD
ENTRYPOINT ["java", "-cp", "/opt/app/*", "io.camunda.connector.runtime.ConnectorRuntimeApplication"]
