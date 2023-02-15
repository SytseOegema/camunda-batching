# Camunda batch processing extension

This repository contains an BPMN system with an extension for batch processing.
It is largely based on two systems: Camunda(BPMN system) and
OpenWishk(Serverless FaaS framework). This project has been developed for my
Master Thesis. For the motivation, goal and conclusion of the project please
read the thesis

[Quick Start Guide](./docs/quickStartGuide.md)

__LINK TO THESIS DOCUMENT__

 The repository structure is described in
the table below.

| Folder Name | Description                                                     |
|-------------|-----------------------------------------------------------------|
| docker      | Contains docker and docker compose files for the project.       |
| docs        | Contains documentation about the project.                       |
| src         | Contains the source code of the project.                        |
| scripts     | Contains scripts to build, deploy, stop and remove the project. |


## Project Requirements
The project has a number of requirements which need to be satisfied in order
for the project to be executable.

- Java version 11
- Java version 17
- Maven pacakge manager
- SBT package manager
- wsk cli tool
- Node
- NPM

```
private static Integer determineOutgoingDoor(String postalCode) {
  int code = Integer.parseInt(postalCode.substring(0, 4));
  // add 3333 to code to get result 1,2 or 3 instead of 0, 1 or 2
  return (code + 3333) / 3333;
}
 
 
private static JsonObject determineInboundDoor(JsonObject vehicle) {
  int bestDoor = 0;
  int bestDoorDistance = 100000;
  for (int idx = 1; idx <= 3; idx++) {
    int distance = calculateDistance(vehicle.getAsJsonArray("goods"), idx);
    if (distance < bestDoorDistance) {
      bestDoorDistance = distance;
      bestDoor = idx;
    }
  }
  JsonObject response = new JsonObject();
  response.addProperty("inboundDoor", bestDoor);
  return response;
}

private static Integer calculateDistance(JsonArray goods, Integer inboundDoorNumber) {
  int distance = 0;
  for (int idx = 0; idx < goods.size(); idx++) {
    JsonObject good = goods.get(idx).getAsJsonObject();
    distance += Math.abs(good.get("outboundDoor").getAsInt() - inboundDoorNumber);
  }
  return distance;
}
  
```

## Zeebe

Requires java 17

## Batch broker

Requires java 11

Use following command in Ubuntu to switch between java versions.

```
sudo update-alternatives --config java
```
