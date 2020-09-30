/**
 * INTER-IoT. Interoperability of IoT Platforms.
 * INTER-IoT is a R&D project which has received funding from the European
 * Union<92>s Horizon 2020 research and innovation programme under grant
 * agreement No 687283.
 *
 * Copyright (C) 2016-2018, by (Author's company of this file):
 * - Prodevelop S.L.
 *
 *
 * For more information, contact:
 * - @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 * - Project coordinator:  <a href="mailto:coordinator@inter-iot.eu"></a>
 *
 *
 * This code is licensed under the EPL license, available at the root
 * application directory.
 */
package eu.interiot.intermw.comm.broker.examples;

import eu.interiot.intermw.comm.broker.Broker;
import eu.interiot.intermw.comm.broker.BrokerContext;
import eu.interiot.intermw.comm.broker.Publisher;
import eu.interiot.intermw.comm.broker.Subscriber;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.interfaces.Configuration;
import eu.interiot.intermw.commons.model.enums.BrokerTopics;

/**
 * Usage example of the Broker broker API
 *
 * By changing the Maven profile (and the {@link Configuration}) it is possible
 * to change the broker implementation with no changes in the client
 * implementation
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 *
 */
public class Example {

    private Broker broker;

    public static void main(String[] args) throws MiddlewareException {
        try {
            // PropertyConfigurator.configure("log4j.properties");
        } catch (Exception ignore) {

        }

        Example client = new Example(args);
        client.pubsub();
    }

    public Example(String[] args) throws MiddlewareException {
        broker = BrokerContext.getBroker();
    }

    protected void pubsub() throws BrokerException {
        Publisher<Message> publisher = broker.createPublisher(BrokerTopics.DEFAULT.getTopicName(), Message.class);
        Subscriber<Message> subscriber = broker.createSubscriber(BrokerTopics.DEFAULT.getTopicName(), Message.class);

        Message message = new Message("Example");

        subscriber.subscribe(m -> System.out.println(m));

        publisher.publish(message);

        publisher.deleteTopic(BrokerTopics.DEFAULT.getTopicName());

        subscriber.cleanUp();
        publisher.cleanUp();
    }
}