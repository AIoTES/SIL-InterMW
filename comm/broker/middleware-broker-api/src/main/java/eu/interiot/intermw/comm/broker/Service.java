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

/**
 * An interface to be implemented by any {@link Broker} service
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 * @param <M>
 *            The message class this services works with
 */
public interface Service<M> {

    /**
     * Initializes this {@link Service} instance
     *
     * (Creates and configures the connection, etc.)
     *
     * @param broker
     *            A {@link Broker} instance
     * @param topicClass
     *            The {@link Topic} class attached to this service
     * @throws BrokerException
     *             When something went wrong
     */
    public void init(Broker broker, Class<? extends Topic<M>> topicClass) throws BrokerException;

    /**
     * Initializes this {@link Service} instance
     *
     * @param broker
     *            A {@link Broker} instance
     * @param exchangeName
     *            The name of the exchange for the {@link Topic}
     * @param messageClass
     *            The name of Class of the messages this {@link Service} is
     *            going to work with
     * @throws BrokerException
     *             When something went wrong
     */
    public void init(Broker broker, String exchangeName, Class<M> messageClass) throws BrokerException;

    /**
     * Cleans the resources created by this {@link Service} (connections,
     * queues, etc.)
     *
     * @throws BrokerException
     */
    public void cleanUp() throws BrokerException;

    /**
     * Gets the {@link Topic} instance this {@link Service} is working with
     *
     * @return
     */
    public Topic<M> getTopic();

    /**
     * Mandatory method to be implemented by {@link Service} implementations to
     * create a topic in the broker.
     *
     * New topics are created automatically by {@link Service} implementations
     * when needed
     *
     * @param name
     *            The name of the topic to create
     * @throws BrokerException
     *             When something went wrong
     */
    public void createTopic(String name) throws BrokerException;

    /**
     * Mandatory method to be implemented by {@link Service} implementations to
     * delete a topic in the broker.
     *
     * It is responsability of a client of the {@link Service} implementation to
     * call this method when a topic has to be deleted
     *
     * @param name
     *            The name of the topic to create
     * @throws BrokerException
     *             When something went wrong
     */
    public void deleteTopic(String name) throws BrokerException;
}
