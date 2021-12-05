package org.gengine.messaging.benchmark;

public class BootstrapArguments
{
    public String brokerUrl;
    public String brokerUsername;
    public String brokerPassword;
    public int numMessages;
    public String endpointSend;
    public String endpointReceive;
    public boolean runProducer = true;
    public boolean runConsumer = true;
    public int numSections = 100; // see also BenchmarkMessage
}
