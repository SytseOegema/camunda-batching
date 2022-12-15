cd ../docker/openwhisk-docker-compose/
echo "Stop Openwhisk"
make stop

echo "Stop camunda batch"
cd ../camunda-platform/
docker-compose stop

cd ../../scripts
