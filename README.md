# ⚙ Gengine

> Unleashing the Power of Distributed Content Management and Transformation

## Overview

Gengine is a distributed content and file investigation and manipulation framework that leverages messaging queues to perform common low-level tasks such as:

- Content change handling
- Transformations
- Metadata extraction
- Hash/checksum computation

These tasks can be resource-intensive, so the focus of the project is on creating lightweight nodes that enable an extremely scalable platform.

## Key Components

Gengine comprises several conceptual pieces that can be mixed and matched to fit various needs. Common end-to-end implementations are also available.

### Client

Task messages are generated and sent, containing a reference to the source content and other necessary options depending on the task. The content reference could be a file on a shared volume, an S3 path, a CMIS document ID, etc.

### Routing

A message routing system directs the request to the appropriate queue for consumption by processing nodes.

### Component

Listens for messages on a relevant queue and calls on workers to perform the task on the source content reference, possibly sending a reply that can be consumed by the original requester or elsewhere.

### Task Nodes

Bootstraps one or more components.

![architecture](/architecture-trans-node.png "Example architecture of a simple image transform node")

Messages can also be produced by other means, different messaging systems and routing can be plugged in, or node workers can be used locally, foregoing messaging altogether.

## Project Layout

### Commons

- `gengine-commons`: Serves as the minimal base for many content definitions, handling, and utility operations.

### Content Handlers

- `gengine-content-handlers/gengine-content-handler-s3`: Handles reading and writing files to Amazon S3.
- `gengine-content-handlers/gengine-content-handler-webdav`: Handles reading and writing files to a WebDAV server.
- `gengine-content-handlers/gengine-content-handler-tempfile`: Creates a file provider with a directory path of the `CleaningTempFileProvider`'s temp dir.

### Messaging

- `gengine-messaging/gengine-messaging-commons`: Defines generic `MessageConsumer` and `MessageProducer` interfaces and contains a Jackson-based JSON marshaller.
- `gengine-messaging/gengine-messaging-camel`: Apache Camel `MessageProducer` and `RequestReplyMessageProducer` implementations.
- `gengine-messaging/gengine-messaging-amqp-direct`: A Qpid-based AMQP endpoint for both producing and consuming messages.
- `gengine-messaging/gengine-messaging-benchmark`: Provides a simple benchmark to measure the performance of brokers.

### Transform

- `gengine-transform/gengine-transform-commons`: Contains basic definitions of transformation options objects for `ContentTransformerWorker`s.
- `gengine-transform/gengine-transform-messaging`: Contains basic definitions of `TransformationRequest` and `TransformationReply` objects.
- `gengine-transform/gengine-transform-worker-ffmpeg`: Converts transformation options to ffmpeg command-line parameters.
- `gengine-transform/gengine-transform-worker-imagemagick`: Converts transformation options to ImageMagick command-line parameters.
- `gengine-transform/gengine-transform-component`: Basis of a content transformer node that processes `TransformationRequest` objects.

### Hash

- `gengine-hash/gengine-hash-commons`: Contains basic definitions of `ContentHashWorker`s.
- `gengine-hash/gengine-hash-messaging`: Contains basic definitions of `HashRequest` and `HashReply` objects.
- `gengine-hash/gengine-hash-worker-javase`: Java SE implementation for computing file hashes.
- `gengine-hash/gengine-hash-component`: Basis of a content hash component that processes `HashRequest` objects.

### Nodes

- `gengine-node-simple`: Bootstraps components to form executable nodes for processing messages.

### ActiveMQ Broker

- `gengine-messaging/messaging-broker-activemq`: A convenience project to start an ActiveMQ broker with a single Maven command.

## Contributing

Thanks for your interest in contributing to Gengine!

We welcome contributions to Gengine. Please submit pull requests, report issues, or create suggestions to help improve the project.

### Reporting Issues

Report issues in the [GitHub Issues](https://github.com/ChenInfo/Gengine/issues) section of this repository.

## License

This project is licensed under the LGPL-3.0 License. See the [LICENSE](LICENSE) file for details.
