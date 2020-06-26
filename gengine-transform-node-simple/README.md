
Overview
========

A simple AMQP content transformer node that listens to one queue and 
delegates to one transformer worker for the lightest weight node possible.

The `TransformerNodeBootsrap` configures the transformer worker based on a provided properties file,
instantiates an AMQP transformer node, and starts it's message listener.

The transform worker from lower level dependencies is specified in the properties file and is used
to convert transformation options Java objects to command-line parameters executed against File objects.

Usage
=====

First build the jar via Maven:

    mvn clean install

either from the parent folder or this project.

Start an AMQP broker.  See the `messaging-broker-activemq` project.

The single jar with dependencies should be launched with a parameter of the
path to the properties file, i.e.:

    java -jar target/gengine-transform-node-simple-0.1-SNAPSHOT-jar-with-dependencies.jar target/imagemagick.properties

You should see a message indicating that the node is waiting for a message.

Once a message is received the node will send a reply confirming it received the
request and will perform the transformation, then sends another reply message indicating
the transformation is complete.
