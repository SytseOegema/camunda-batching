# My Function

> OpenWhisk function

This folder contains an OpenWhisk function that may be used in the example
process.

json input example:
```
{
    "content": [
        {
            "goods": [
                {
                    "description": "bananas",
                    "outboundDoor": 3,
                    "postalCode": "7701CK"
                },
                {
                    "description": "bananas",
                    "outboundDoor": 2,
                    "postalCode": "5431BE"
                },
                {
                    "description": "bananas",
                    "outboundDoor": 1,
                    "postalCode": "1111AA"
                }
            ],
            "licensePlate": "AB"
        },
        {
            "goods": [
                {
                    "description": "bananas",
                    "outboundDoor": 3,
                    "postalCode": "7701CK"
                },
                {
                    "description": "bananas",
                    "outboundDoor": 3,
                    "postalCode": "5431BE"
                },
                {
                    "description": "bananas",
                    "outboundDoor": 1,
                    "postalCode": "1111AA"
                }
            ],
            "licensePlate": "BC"
        },
        {
            "goods": [
                {
                    "description": "bananas",
                    "outboundDoor": 1,
                    "postalCode": "7701CK"
                },
                {
                    "description": "bananas",
                    "outboundDoor": 3,
                    "postalCode": "5431BE"
                },
                {
                    "description": "bananas",
                    "outboundDoor": 1,
                    "postalCode": "1111AA"
                }
            ],
            "licensePlate": "DE"
        }
    ]
}
```

## Building
```
mvn package clean
```

## Deploy
Deploys the function in the namespace batching under the name foodChoices.
```
wsk action create batching/determineInboundDoor target/my-func-1.0-SNAPSHOT-jar-with-dependencies.jar --main MyFunction -i --web true
```
