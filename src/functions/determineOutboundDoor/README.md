# My Function

> OpenWhisk function

This folder contains an OpenWhisk function that may be used in the example
process.

The example function requires the input parameter `content` which should be a
json object with the property `goods` which is an array with good information.

It returns an output with the outbound doors in each of these goods.

## Building
```
mvn package clean
```

## Deploy
Deploys the function in the namespace batching under the name foodChoices.
```
wsk action create batching/determineOutboundDoor target/my-func-1.0-SNAPSHOT-jar-with-dependencies.jar --main MyFunction -i --web true
```
