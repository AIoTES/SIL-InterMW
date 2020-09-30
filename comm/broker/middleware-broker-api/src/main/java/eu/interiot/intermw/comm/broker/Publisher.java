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
package eu.interiot.intermw.comm.broker;

import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;

import java.util.List;

/**
 * A {@link Service} for publishing messages in a message broker instance
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 *
 * @param <M>
 *            The message class to be published
 */
public interface Publisher<M> extends Service<M> {

    /**
     * Publishes a message
     *
     * @param message
     *            The message to be published
     * @throws BrokerException
     */
    public void publish(M message) throws BrokerException;

    /**
     * Initializes a publisher and creates the corresponding queues (only if the
     * message bus supports queue creation)
     *
     * When publishing a message it should be published on each queue
     *
     * @param broker
     *            A {@link Broker} instance
     * @param queues
     *            A list of {@link Queue}
     * @param topicClass
     *            The {@link Topic} class attached to this service
     * @throws BrokerException
     *             When something went wrong
     */
    public void init(Broker broker, List<Queue> queues, Class<Topic<M>> topicClass) throws BrokerException;

    /**
     * Initializes a publisher and creates the corresponding queues (only if the
     * message bus supports queue creation)
     *
     * When publishing a message it should be published on each queue
     *
     * @param broker
     *            A {@link Broker} instance
     * @param queues
     *            A list of {@link Queue}
     * @param exchangeName
     *            The name of the exchange where to publish the messages
     * @param messageClass
     *            The name of Class of the messages that are going to be
     *            published
     * @throws BrokerException
     *             When something went wrong
     */
    public void init(Broker broker, List<Queue> queues, String exchangeName, Class<M> messageClass)
            throws BrokerException;
}
