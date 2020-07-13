
Overview
========

A Gengine Dropwizard node with all known components configured and enabled.


Usage
=====

First build the jar via Maven:

    mvn clean install

either from the parent folder or this project.

Start an AMQP broker.  See the `messaging-broker-activemq` project.

The single jar with dependencies can be launched with parameters passed for the 
command and configuration YAML file:

    java -jar target/gengine-node-dropwizard-0.1-SNAPSHOT.jar server config.yml

You should see the components being initialized in the logs and endtires 
indicating that the node is waiting for a messages for each component.

Once a message is received the node will send a reply which varies per component.
Some components will confirm it received the request and will perform the content action
of the component's worker, then send another reply message indicating the transformation is complete.
Other components will simply perform the action and reply.
