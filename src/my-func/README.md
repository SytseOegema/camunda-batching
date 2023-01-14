# My Function

> OpenWhisk function

This folder contains an OpenWhisk function that may be used in the example
process.

The example function requires the input parameter `foodPreference` and returns
an output with the property `foodOrder`.

## Building
```
mvn package clean
```

## Deploy
Deploys the function in the namespace batching under the name foodChoices.
```
wsk action create batching/foodChoices target/my-func-1.0-SNAPSHOT-jar-with-dependencies.jar --main MyFunction -i --web true
```
