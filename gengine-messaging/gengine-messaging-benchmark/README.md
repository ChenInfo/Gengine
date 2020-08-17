
Overview
========

A simple benchmark that produces and consumes messages on a single AMQP queue
and measures the throughput.

Usage
=====

First build the jar via Maven:

    mvn clean package

either from this project.

Start an AMQP broker.  See the `messaging-broker-activemq` project.

The single jar with dependencies should be launched with a parameter of the
brokerUrl and the number of messages to test, i.e.:

    java -jar target/gengine-messaging-benchmark-0.1-SNAPSHOT-jar-with-dependencies.jar tcp://localhost:5672 10000

You should see a message indicating that the test is sending messages and the final statistics.
