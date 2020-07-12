
Overview
========

This forms the basis of a content hash component which acts as a
`MessageConsumer` to process `HashRequest` objects and sends
`HashReply` objects to a specified `MessageProducer`.

No assumptions are made as to how those Java object messages are routed
to or from the hash component.

A `ContentHashNodeWorker` performs the actual work of computing
the hash value.
