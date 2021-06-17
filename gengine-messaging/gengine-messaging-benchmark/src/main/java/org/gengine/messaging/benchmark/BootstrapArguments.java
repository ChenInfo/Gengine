package org.gengine.messaging.benchmark;

public class BootstrapArguments
{
    public String brokerUrl;
    public int numMessages;
    public String endpointSend;
    public String endpointReceive;
    public boolean runProducer = true;
    public boolean runConsumer = true;
}
