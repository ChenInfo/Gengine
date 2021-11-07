Overview
========

Runs an [ActiveMQ](http://activemq.apache.org) broker.

Usage
=====

To start the broker run:

    mvn activemq:run
    
You can then access the [admin console](http://localhost:8161/admin)
with a username/password of admin/admin.

If you want to see the logging message you'll have to specify the path in the command:

    mvn activemq:run -Dlog4j.configuration=file:///<full path to log4j.properties>

As of now, runs ActiveMQ 5.8 (note: build & run with JDK 1.7). 

Alternatively, you may choose use docker, for example:

    https://hub.docker.com/r/webcenter/activemq/ (or similar)

ActiveMQ License
================

Note that the code for the ActiveMQ admin console is included here for convenience and
falls under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html)
