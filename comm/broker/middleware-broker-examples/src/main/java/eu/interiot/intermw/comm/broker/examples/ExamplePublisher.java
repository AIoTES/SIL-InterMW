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
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.model.enums.BrokerTopics;

/**
 * Usage example of the Broker broker {@link Publisher} API
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 *
 */
public class ExamplePublisher {

    public static void main(String[] args) throws MiddlewareException {

        Message message = new Message("Example");

        Broker broker = BrokerContext.getBroker();
        Publisher<Message> publisher = broker.createPublisher(BrokerTopics.DEFAULT.getTopicName(), Message.class);
        publisher.publish(message);

        System.out.println("Message published");
    }
}
