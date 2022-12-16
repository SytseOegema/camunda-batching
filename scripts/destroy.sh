./stop.sh

cd ../docker/openwhisk-docker-compose/
echo "Destroy Openwhisk"
make destroy

echo "Destroy camunda batch"
cd ../camunda-platform/
pwd
docker-compose down -v

cd ../../scripts
