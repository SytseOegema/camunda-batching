cd ../docker/openwhisk-docker-compose/
echo "Setup Openwhisk"
make quick-start-fast

echo "Setup camunda batch"
cd ../camunda-platform/
docker-compose up -d

cd ../../scripts
