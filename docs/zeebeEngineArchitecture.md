# Zeebe Engine architecture

> Documents the software architecture of the Zeebe workflow engine

Zeebe is the workflow engine of the Camunda version 8 platform. This repository
(folder [zeebe](../zeebe)) contains the zeebe implementation of version 8.1.0
with an extension that supports batch processing. This document describes the
architecture of the Zeebe workflow engine and how it is instantiated by the
broker on startup.

# Broker

The Zeebe broker is the server implementation of Zeebe. It instanciates all the
modules that are required for Zeebe to function. The main module is the workflow
engine which manages the flow and state of all processes and process instances.
The major requirement for Zeebe in the camunda 8 platform was scalability and
failover. Therefore Zeebe is constructed according to Partitions. A single
gateway forms the access points to all these partitions(in development often
a single partition is used). The `PartitionFactury` is used to instantiate a
partition. A partition contains all logic that an engine instance requires,
handling messages, state and state transfers.


# Engine


Through all kinds of classes the whole partition is created
containing an engine `StreamProcessor`. The StreamProcessor makes sure there are
processors for the incoming GRPC events but also processors that handle internal
process flow events.


## Process Flow

The main instance for processing the process flow instances is the
`BpmnStreamProcessor`. Each state transition goes through the
`BpmnStateTransitionBehavior` most of the times handling a record results in
the creation of a followup event. The whole flow dependes on the state of a
process instance. This is specified via the ProcessInstanceIntent and has the
following intents:

- CANCEL((short) 0, false),
- SEQUENCE_FLOW_TAKEN((short) 1),
- ELEMENT_ACTIVATING((short) 2),
- ELEMENT_ACTIVATED((short) 3),
- ELEMENT_COMPLETING((short) 4),
- ELEMENT_COMPLETED((short) 5),
- ELEMENT_TERMINATING((short) 6),
- ELEMENT_TERMINATED((short) 7),
- ACTIVATE_ELEMENT((short) 8),
- COMPLETE_ELEMENT((short) 9),
- TERMINATE_ELEMENT((short) 10);


The flow goes like this where the ACTIVATE_ELEMENT and COMPLETE_ELEMENT are
events that can also be triggered from external systems. Termination flow is not
included in the flow diagram but follows a similar trajectory. On completing the
final activity/reaching the end of a flow. There is simply no follow up command.

```
ACTIVATE_ELEMENT -> ELEMENT_ACTIVATING -> ELEMENT_ACTIVATED---
      |                                                      |
SEQUENCE_FLOW_TAKEN                                          |
      |                                                      |
ELEMENT_COMPLETED <- ELEMENT_COMPLETING <- COMPLETE_ELEMENT <-
```


Should go to:

```
ACTIVATE_ELEMENT -> ELEMENT_PAUSED ---------> RESUME_BATCH_ACTIVITY
      |                                              |
SEQUENCE_FLOW_TAKEN <------------------------------  |
      |                                     ELEMENT_ACTIVATING
      |                                              |
      |                                   ELEMENT_ACTIVATED
      |                                              |  
ELEMENT_COMPLETED <- ELEMENT_COMPLETING <- COMPLETE_ELEMENT
```

This requires changes in the `BpmnStreamProcessor` and `BpmnStateTransitionBehavior`
