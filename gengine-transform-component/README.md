
Overview
========

This forms the basis of a content transformer node which acts as a
`MessageConsumer` to process `TransformationRequest` objects and sends
`TransformationReply` objects to a specified `MessageProducer`.

No assumptions are made as to how those Java object messages are routed
to or from the transformer node.

A `ContentTransformerWorker` performs the actual work of converting
the transformation options Java objects to command-line or API 
calls against `ContentReference` objects.
