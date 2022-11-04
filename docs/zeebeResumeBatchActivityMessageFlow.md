# Zeebe resume batch activity extension

> Documentation of the message flow for resuming a batch activity.

Zeebe is the workflow engine of the Camunda version 8 platform. This repository
(folder [zeebe](../zeebe)) contains the zeebe implementation of version 8.1.0
with an extension that supports batch processing. This document describes the
internal process flow of Zeebe and how it is extended for the specific batch
processing task of resuming the regular process flow of a batch activity. This
document merely describes how a client interaction message is handled. The
actual updating of the process flow is described
[here](./zeebeResumeBatchActivityProcessFlow.md).

## Highlevel overview

Zeebe is an elaborate software system that consists of numerous modules. In this
document only the modules relevant to the batch extension are discussed. For
information on the other modules we refer you to the
[camunda documentation](https://docs.camunda.io/)
and the [zeebe github repo](https://github.com/camunda/zeebe).

The relevant modules for the batch extension are:
- broker : the server implementation of Zeebe.
- engine : the BPMS workflow implementation.
- gateway : the implementation of the GRPC serverside endpoints.
- protocol : the implementation and definition of communication message formats.
- client : the client library that can be used to interact with the GRPC
endpoints.

## Individual components

### Broker

The broker is the server implementation of Zeebe. It contains entry classes and
startup scripts/protocols. It combines all underlying modules into a service.
The batch extension only requires a single change in the broker which maps
CommandMessages with value type RESUME_BATCH_ACTIVITY to a
ResumeBatchActivityRecord. This is added in the `CommandApiRequestReader`

Changed files:
- [CommandApiRequestReader.java](https://github.com/SytseOegema/camunda-batching/commit/a250f2ccdca5ea3540fd46302adc0bfd0cb72b53#diff-e4a1127d09b06b9c48c96b07dbc647bee1c3d47eafe6472668fb73e42a83fdb6R47)

### Gateway

The gateway implementation is split over 3 folders: gateway, gateway-protocol
and gateway-protocol-impl. The gateway folder contains the implementation of
the message handling, gateway-protocol folder contains the GRPC endpoint
definitions and I have no clue what gateway-protocol-impl does because I did not
have to use it.

**gateway-protocol**

The folder contains the `gateway.proto` file. This file defines the GRPC
endpoint interface. It specifices the available methods and the required message
formats for interacting with these methods. The `gateway.proto` file is extended
with a new method `ResumeBatchActivity` and with the two messages
`ResumeBatchActivityRequest` and `ResumeBatchActivityResponse`. These define the
interface for resuming a batch activity.

**gateway**

The folder contains all the functionality to handle the methods specified in the
interface. To add functionality for resuming a batch activity the following
changes have been made. The flow and implementation logic is similar to the
default implementation.

`GatewayGrpcService`

Maps the interface to implementation. `resumeBatchActivity` method is
implemented and calls `EndpointManager.resumeBatchActivity`.

`EndpointManager`

Implement the `resumeBatchActivity` action. Simply add the method and use the
internal `sendRequest()` method to implement the regular flow. Uses the
response- and requestmapper.

`ResponseMapper`

Maps the internal response object to the response message in the GRPC format.

`RequestMapper`

Maps the GRPC request object to the internally used `ResumeBatchActivityRecord`.

`BrokerResumeBatchActivityRequest`

The class that actually implements the request mapping.


### Engine

The engine implements the actual process flow behaviour. Also the deployment of
new process is done by the engine as are all other process flow / process state
interactions. The engine is a complex piece of software which is described in
more details [here](./zeebeEngineArchitecture). In the remainder of this section
the changes to the engine are described that were required to resume a batch
activity.

`TypedEventRegistry`

Contains a registry of the ValueTypes and how these are mapped to the
corresponing Record class.

`ResumeBatchActivityProcessor`

This is the processor instance that eventually handles a command of the type
RESUME_BATCH_ACTIVITY. This processor initiates followup commands that actually
resume the regular process flow.

`EngineProcessors`

This class is called on the startup of a Zeebe partition. It registers all the
Processors to the engine which is used by the ProcessingStatemachine. The
EngineProcessors is extended such that it also registers the
ResumeBatchActivityProcessor.

`Engine`

The engine is the record processor implementation for a Zeebe partition. It
contains an EnumSet of support valuetypes. This had to be updated such that the
new value type  RESUME_BATCH_ACTIVITY was also supported.



### Protocol

The protocol implementation is split over 5 folders. Only the two folders
protocol and protocol-impl have been updated for the extension.

**protocol-impl**

Here the internal message formats are defined as records. Mostly these are
simple data objects with properties, getters and setters. The properties
correspond to the properties in the GRPC request message. A single new file is
added `ResumeBatchActivityRecord`.

**protocol**

This folder defines types which are used throughout the Zeebe workflow engine to
determine how a record should be processed. The following classes have been
updated:

`ValueTypeMapping`

Maps all entities related to one an other. ValueType, RecordValue and Intent

`Intent`

Contains a list of all the Intent classes.

`ResumeBatchActivityIntent`

Defines the intent of a ResumeBatchActivityRecord.

`ResumeBatchActivityRecordValue`

interface for the ResumeBatchActivityRecord.



Besides these classes also the
[protocol.xml](https://github.com/SytseOegema/camunda-batching/blob/main/zeebe/protocol/src/main/resources/protocol.xml)
has been updated. This defines a new ValueType, `RESUME_BATCH_ACTIVITY`.
