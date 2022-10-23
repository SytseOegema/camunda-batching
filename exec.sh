# run in root folder

# install, package broker
mvn clean install -DskipTests

# go to dist folder
cd dist

# start broker
mvn exec:java -Dexec.mainClass=io.camunda.zeebe.broker.StandaloneBroker


# private static final Logger LOG = Loggers.STREAM_PROCESSING;
# LOG.debug("ProcessingScheduleService hasn't been opened yet, ignore scheduled task.");
