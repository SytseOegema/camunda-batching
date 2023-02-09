# Quick Start Guide

This guide explains how to setup the Camunda BPM System with the batch extension
on Docker using docker compose. No manual actions with docker are required
because scripts have been created to launch everything.

## Launch the systems
In the [scripts folder](../scripts) you can find the scripts to launch the
system.

1. Execute `run.sh` to deploy the BPM Enactment system with Batch controller
and the FaaS platform.
2. Execute `deployFunctions.sh` to deploy 2 functions to the FaaS platform that
are used in the example process.

## Create the process
Go to the [Batch Management Tool](http://localhost:8080) and use it to create
a process. The example process can be found in the
[src folder](../src/camunda-srcs/crossdocking.bpmn). The example process defines
a BPMN process for optimizing the docking planning in a crossdock.

The Batch Management Tool can be used to create process instances. Example
inputs that can be used are:

```
{
    "licensePlate": "Truck1",
    "location": "Groningen",
    "goods": [
        {
            "description": "bananas",
            "postalCode": "7701CK"
        },
        {
            "description": "bananas",
            "postalCode": "5431BE"
        },
        {
            "description": "bananas",
            "postalCode": "1111AA"
        }
    ]
}
```

```
{
    "licensePlate": "Truck2",
    "location": "Groningen",
    "goods": [
        {
            "description": "bananas",
            "postalCode": "1111AA"
        },
        {
            "description": "bananas",
            "postalCode": "5431BE"
        },
        {
            "description": "bananas",
            "postalCode": "1111AA"
        }
    ]
}
```

```
{
    "licensePlate": "Truck3",
    "location": "Groningen",
    "goods": [
        {
            "description": "bananas",
            "postalCode": "7701CK"
        },
        {
            "description": "bananas",
            "postalCode": "5431BE"
        },
        {
            "description": "bananas",
            "postalCode": "7701CK"
        }
    ]
}
```

## Add Batch Model
Go to the [Batch Management Tool](http://localhost:8080/#/batch-models)
and create a batch model.

For the example process the following parameter settings should be used:

| Parameter | Value |
| --------- | ----- |
| MaxBatchSize | 3 |
| URI | https://openwhisk_apigateway_1/api/v1/web/guest/batching/determineInboundDoor.json |
| GroupByParameter | location |

## Connect Batch Model
After creating the process and the batch model, we now need to create the connection. Go to the process and click on the + button after activty Activity_1esuqna. Now select your batch model. You can add conditions but this is not required.
