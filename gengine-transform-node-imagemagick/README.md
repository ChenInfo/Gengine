
Overview
========

An ImageMagick AMQP content transformer node.

The `ImageMagickAmqpContentTransformerNodeBootsrap` itself is really just a convenience class
to configure and instantiate an AMQP transformer node and start it's message listener.

The `ImageMagickontentTransformerNodeWorker` from the `gengine-transform-worker-imagemagick` dependency is used
to convert transformation options Java objects to command-line parameters executed against File objects.

Usage
=====

First build the jar via Maven:

    mvn clean install

either from the parent folder or this project.

Start an AMQP broker.  See the `messaging-broker-activemq` project.

The single jar with dependencies can be launched with parameters passed for the 
AMQP broker host, request queue, and reply queue, i.e.:

    java -jar target/gengine-transform-node-imagemagick-0.1-SNAPSHOT-jar-with-dependencies.jar localhost cheninfo.transform.worker.request.image cheninfo.transform.worker.reply.image

You should see a message indicating that the node is waiting for a message.

Once a message is received the node will send a reply confirming it received the
request and will perform the transformation, then sends another reply message indicating
the transformation is complete.
