
Overview
========

A simple benchmark that produces and consumes messages on a single AMQP endpoint
and measures the throughput.

Usage
=====

First build the jar via Maven:

    mvn clean package

either from this project.

Start an AMQP broker.  See the `messaging-broker-activemq` project.

The single jar with dependencies should be launched with a parameter of the
brokerUrl and the number of messages to test, i.e.:

    java -jar target/gengine-messaging-benchmark-0.3-SNAPSHOT-jar-with-dependencies.jar 'tcp://localhost:5672' 10000

You should see a message indicating that the test is sending messages and the final statistics.

Advanced Usage
==============

You can optionally specify the queue or topic you wish to use in the command line:

    java -jar target/gengine-messaging-benchmark-0.3-SNAPSHOT-jar-with-dependencies.jar 'tcp://localhost:61616' 100 'queue:foo'
    java -jar target/gengine-messaging-benchmark-0.3-SNAPSHOT-jar-with-dependencies.jar 'tcp://localhost:61616' 100 'topic:bar.foo'

You can run just the producer of messages with the `product-only` option:

    java -jar target/gengine-messaging-benchmark-0.3-SNAPSHOT-jar-with-dependencies.jar 'tcp://localhost:61616' 100 'topic:bar.foo' produce-only

You can run just the consumer of messages with the `consume-only` option, shown with a durable subscriber expecting 100 messages:

    java -jar target/gengine-messaging-benchmark-0.3-SNAPSHOT-jar-with-dependencies.jar 'tcp://localhost:61616' 100 'topic:bar.foo?clientId=1&durableSubscriptionName=bar1' consume-only
