# Camunda batch processing extension

This repository contains an BPMN system with an extension for batch processing.
It is largely based on two systems: Camunda(BPMN system) and
OpenWishk(Serverless FaaS framework). This project has been developed for my
Master Thesis. For the motivation, goal and conclusion of the project please
read the thesis

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

## Zeebe

Requires java 17

## Batch broker

Requires java 11

Use following command in Ubuntu to switch between java versions.

```
sudo update-alternatives --config java
```
