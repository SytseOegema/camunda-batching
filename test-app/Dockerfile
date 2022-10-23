FROM openjdk:17-alpine3.14
WORKDIR /application
COPY target/test-app-1.0-SNAPSHOT-jar-with-dependencies.jar ./
CMD ["java", "-cp", "test-app-1.0-SNAPSHOT-jar-with-dependencies.jar", "com.test.App"]
