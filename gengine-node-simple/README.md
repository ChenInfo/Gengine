
Overview
========

A simple AMQP content node that listens to one queue and 
delegates to one content worker for the lightest weight node possible.

The `SimpleAmqpNodeBootsrap` class uses component bootraps to configure the worker 
based on a provided properties file, instantiates an AMQP endpoint, and starts it's message listener.

The worker from lower level dependencies to be used is specified in the properties file.

Usage
=====

First build the jar via Maven:

    mvn clean install

either from the parent folder or this project.

Start an AMQP broker.  See the `messaging-broker-activemq` project.

The single jar with dependencies should be launched with a parameter of the
path to the properties file, i.e. (replace 0.X-SNAPSHOT with appropriate version):

    java -jar target/gengine-node-simple-0.X-SNAPSHOT-jar-with-dependencies.jar target/imagemagick.properties

You should see a message indicating that the node is waiting for a message.

Once a message is received the node will send a reply confirming it received the
request and will perform the transformation, then sends another reply message indicating
the transformation is complete.
