package org.gengine.messaging.benchmark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Boostrap which creates and runs a {@link BenchmarkRunner}
 *
 */
public class Bootstrap
{
    private static final Log logger = LogFactory.getLog(Bootstrap.class);

    protected static final String USAGE_MESSAGE =
            "\n\nUSAGE: brokerUrl numMessages "
            + "[endpointSend] [endpointReceive] [consume-only] [produce-only]\n\n"
            + "\tbrokerUrl\tThe broker URL, example: tcp://localhost:61616\n"
            + "\tnumMessages\tThe number of messages to send and/or expect\n"
            + "\tendpointSend\tThe endpoint to send messages to, default: queue:gengine.test.benchmark\n"
            + "\tendpointReceive\tThe endpoint to consumer messages from, default: queue:gengine.test.benchmark\n"
            + "\tconsume-only\tConsume only, do not produce messages\n"
            + "\tproduce-only\tProduce only, do not consumer messages\n";

    public static void main(String[] args)
    {
        BootstrapArguments argsObject = parse(args);
        try
        {
            BenchmarkRunner runner = new BenchmarkRunner(
                    argsObject.brokerUrl,
                    argsObject.endpointSend,
                    argsObject.endpointReceive,
                    argsObject.numMessages,
                    argsObject.runProducer,
                    argsObject.runConsumer);
            runner.runBenchmark();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            System.exit(1);
        }
    }

    public static BootstrapArguments parse(String args[])
    {
        if (args.length < 2)
        {
            throw new IllegalArgumentException(USAGE_MESSAGE);
        }

        BootstrapArguments argsObject = new BootstrapArguments();

        argsObject.brokerUrl = args[0];

        argsObject.numMessages = Integer.valueOf(args[1]);

        for (int i = 2; i < 6; i++)
        {
            if (args.length > i)
            {
                if (args[i].equals("consume-only"))
                {
                    argsObject.runProducer = false;
                }
                else if (args[i].equals("produce-only"))
                {
                    argsObject.runConsumer = false;
                }
                else if (argsObject.endpointSend == null && isSupportedEndpoint(args[i]))
                {
                    argsObject.endpointSend = args[i];
                }
                else if (argsObject.endpointReceive == null && isSupportedEndpoint(args[i]))
                {
                    argsObject.endpointReceive = args[i];
                }
            }
        }

        return argsObject;
    }

    protected static boolean isSupportedEndpoint(String endpoint)
    {
        return endpoint != null && (endpoint.startsWith("queue") ||
                endpoint.startsWith("topic"));
    }
}
