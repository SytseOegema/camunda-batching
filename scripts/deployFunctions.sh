echo "This script will build and deploy 2 functions to openwhisk."
echo "-"
echo "Both functions are used in the example process."
echo "-"
echo "1. foodOrder - a function for determining the dish given a preference."
echo "1. foodDelivery - a function for determining the delivery time given the dish."

# create batching package
wsk package create batching -i

cd ../src/functions

cd foodDelivery
mvn clean package
wsk action create batching/foodDelivery target/my-func-1.0-SNAPSHOT-jar-with-dependencies.jar --main MyFunction -i --web true
cd ..


cd foodOrder
mvn clean package
wsk action create batching/foodOrder target/my-func-1.0-SNAPSHOT-jar-with-dependencies.jar --main MyFunction -i --web true
cd ..

cd ../scripts
