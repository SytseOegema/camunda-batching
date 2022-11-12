# fixen van process flow

__DONE__
1. zorg dat alleen bepaalde Executable tasks worden gepauzeerd in the BpmnStreamProcessor. Deze loggen.

__To DO__

2. Zorg dat je simpel weg alle activities stopt en daarna weer activeert door de client.
   kijk eens naar BpmnStateBehavior en dan getElement() op basis van context
3. Zorg dat je activties die zo genaamd uitgevoerd zijn in batch de huidige activity laat overslaan.
   check engine/processing/bpmn/task voor outgoing sequence flow to do.
4. Nu moet je alleen nog de variables kunnen updaten.
   kijk naar engine/processing/variable/UpdateVariableDocumentProcessor.

Ohja en natuurlijk moet er nog een kafka producer komen die alles automatiseert richting de batch-controller
