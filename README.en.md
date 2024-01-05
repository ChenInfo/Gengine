# ⚙ Gengine

[中文版本](README.md)

> Empowering Distributed Content Management and Transformation

## Overview

Gengine is a distributed content and file investigation and manipulation framework designed to handle resource-intensive tasks with high scalability. By leveraging messaging queues and lightweight nodes, Gengine efficiently processes common low-level tasks such as:

- Content change handling
- Transformations
- Metadata extraction
- Hash/checksum computation

In the era of User Generated Content (UGC) and rapid digital transformation, managing and processing vast amounts of multimedia content is crucial across various industries, including media, entertainment, social platforms, and cloud services. Gengine provides a robust framework to handle these challenges efficiently.

## Key Features

- **High Scalability**: Architected to scale horizontally, handling large volumes of data and tasks efficiently.
- **Modular Design**: Comprises modular components that can be mixed and matched to fit different needs.
- **Extensibility**: Supports various messaging systems, routing mechanisms, and can integrate with different content sources like S3, WebDAV, etc.
- **Distributed Processing**: Leverages lightweight nodes for distributed processing, making it suitable for cloud and microservices architectures.

## Applications and Industries

Gengine's capabilities make it highly useful in scenarios such as:

- **Multimedia Processing for UGC Platforms**: Efficiently handle transcoding, resizing, and transformation of user-uploaded media.
- **Digital Asset Management**: Manage and process digital content for media and entertainment industries.
- **Cloud Services**: Provide backend processing for cloud-based content services, including storage and retrieval optimization.
- **E-commerce**: Optimize product images and videos for web and mobile platforms.
- **Big Data and Analytics**: Preprocess large datasets for analysis, including extracting metadata and content transformations.
- **Healthcare**: Handle and process medical imaging data securely and efficiently.

## Architecture

### Client

Clients generate and send task messages containing references to the source content and necessary options. The content reference could be:

- A file on a shared volume
- An Amazon S3 path
- A CMIS document ID
- A WebDAV URL, etc.

### Routing

A message routing system directs the requests to appropriate queues for processing by nodes.

### Nodes

Task nodes bootstrap one or more components, listening for messages on relevant queues and invoking workers to perform tasks on the content.

### Workers

Workers perform the actual processing tasks, such as content transformation, metadata extraction, or hash computation.

![architecture](/architecture-trans-node.png "Example architecture of a simple image transform node")

Messages can also be produced by other means, different messaging systems and routing can be plugged in, or node workers can be used locally, foregoing messaging altogether.

## Project Modules

### Commons

- **gengine-commons**: The minimal base for content definitions, handling, and utility operations.

### Content Handlers

- **gengine-content-handler-s3**: Handles reading and writing files to Amazon S3.
- **gengine-content-handler-webdav**: Handles reading and writing files to a WebDAV server.
- **gengine-content-handler-tempfile**: Manages temporary files for processing.

### Messaging

- **gengine-messaging-commons**: Defines generic interfaces for message producers and consumers, with JSON marshalling support.
- **gengine-messaging-camel**: Apache Camel implementations of message producers.
- **gengine-messaging-amqp-direct**: Qpid-based AMQP endpoint for producing and consuming messages.
- **gengine-messaging-benchmark**: Tool for benchmarking messaging brokers.

### Transform

- **gengine-transform-commons**: Definitions of transformation option objects for content transformers.
- **gengine-transform-messaging**: Definitions of transformation request and reply objects.
- **gengine-transform-worker-ffmpeg**: Integrates with FFmpeg for media transformations.
- **gengine-transform-worker-imagemagick**: Integrates with ImageMagick for image transformations.
- **gengine-transform-component**: The basis for content transformer nodes.

### Hash

- **gengine-hash-commons**: Definitions of content hash workers.
- **gengine-hash-messaging**: Definitions of hash request and reply objects.
- **gengine-hash-worker-javase**: Java SE implementation for computing file hashes.
- **gengine-hash-component**: The basis for content hash components.

### Nodes

- **gengine-node-simple**: Bootstraps components to form executable nodes for processing messages.

### Health Monitoring

- **gengine-health-commons**: Interfaces and implementations to monitor the health of system components.

### Messaging Broker

- **gengine-messaging-broker-activemq**: Starts an ActiveMQ broker with a single Maven command for testing and development.

## Getting Started

To get started with Gengine, follow these steps:

1. **Clone the Repository**

   ```bash
   git clone https://github.com/ChenInfo/Gengine.git
   ```

2. **Build the Project**

   Navigate to the root directory and build the project using Maven:

   ```bash
   mvn clean install
   ```

3. **Start the Messaging Broker**

   Start an AMQP broker, such as ActiveMQ:

   ```bash
   cd gengine-messaging/messaging-broker-activemq
   mvn activemq:run
   ```

4. **Configure and Run Nodes**

   Configure nodes using provided properties files and run them. For example, to run a simple node:

   ```bash
   cd gengine-node-simple
   java -jar target/gengine-node-simple-0.X-SNAPSHOT-jar-with-dependencies.jar path/to/config.properties
   ```

5. **Send Messages with Your Client, or Use the Provided Benchmark Tool**

## Contributing

We welcome contributions to Gengine! Please submit pull requests, report issues, or create suggestions to help improve the project.

### Reporting Issues

Report issues in the [GitHub Issues](https://github.com/ChenInfo/Gengine/issues) section of this repository.

## License

This project is licensed under the LGPL-3.0 License. See the [LICENSE](LICENSE) file for details.
