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
import eu.interiot.intermw.commons.interfaces.Configuration;

/**
 * A factory for retrieving {@link Publisher}, {@link Subscriber},
 * {@link Serializer}, {@link Topic} instances and so on
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 *
 */
public interface Broker {

    /**
     * Creates a new {@link Publisher} given a {@link Topic} class
     *
     * @see #createPublisher(String, Class)
     * @param topicClass
     *            The class of the {@link Topic} to use
     * @return A new {@link Publisher} instance to use for a {@link Topic}
     * @throws BrokerException
     */
    public <M> Publisher<M> createPublisher(Class<? extends Topic<M>> topicClass) throws BrokerException;

    /**
     * Creates a new {@link Subscriber} given a {@link Topic} class
     *
     * @see #createSubscriber(String, Class)
     * @param topicClass
     *            The class of the {@link Topic} to use
     * @return A new {@link Subscriber} instance
     * @throws BrokerException
     */
    public <M> Subscriber<M> createSubscriber(Class<? extends Topic<M>> topicClass) throws BrokerException;

    /**
     * Creates a new {@link Publisher} given the <exchangeName> and the
     * <className> of the messages that are going to be published.
     *
     * This method for creating a {@link Publisher} is useful when the message
     * class is not known at compile time
     *
     * @param exchangeName
     *            The name of the exchange where to publish messages in the
     *            broker
     * @param className
     *            The Class name of the messages to be published. It will be
     *            created by reflection
     * @return A new {@link Publisher} instance
     * @throws BrokerException
     */
    public <M> Publisher<M> createPublisher(String exchangeName, String className) throws BrokerException;

    /**
     * Creates a new {@link Publisher} given the <exchangeName> and the Class of
     * the messages that are going to be published.
     *
     * This is the preferred method to create a {@link Publisher}
     *
     * @param exchangeName
     *            The name of the exchange where to publish messages in the
     *            broker
     * @param messageClass
     *            The Class of the messages to be published
     * @return A new {@link Publisher} instance
     * @throws BrokerException
     */
    public <M> Publisher<M> createPublisher(String exchangeName, Class<M> messageClass) throws BrokerException;

    /**
     * Creates a new {@link Subscriber} given the <exchangeName> and the
     * <className> of the messages that are going to be subscribed to
     *
     * @param exchangeName
     *            The name of the exchange where to subscribe messages from the
     *            broker
     * @param className
     *            the Class name of the messages to be subscribed to. It will be
     *            created by reflection
     * @return A new {@link Subscriber} instance
     * @throws BrokerException
     */
    public <M> Subscriber<M> createSubscriber(String exchangeName, String className) throws BrokerException;

    /**
     * Creates a new {@link Subscriber} given the <exchangeName> and the Class
     * of the messages that are going to be subscribed to
     *
     * @param exchangeName
     *            The name of the exchange where to subscribe messages from the
     *            broker
     * @param messageClass
     *            the Class of the messages to be subscribed to
     * @return A new {@link Subscriber} instance
     * @throws BrokerException
     */
    public <M> Subscriber<M> createSubscriber(String exchangeName, Class<M> messageClass) throws BrokerException;

    /**
     * The instance of {@link Serializer} that is going to be used for
     * publishing and subscribing to messages
     *
     * @return A {@link Serializer} insetance
     * @throws BrokerException
     */
    public <M> Serializer<M> createSerializer() throws BrokerException;

    /**
     * Gets the {@link Configuration} instance used to create this
     * {@link Broker} instance
     *
     * @return The {@link Configuration} instance
     */
    public Configuration getConfiguration();

    /**
     * The default {@link Topic} class to be used when no {@link Topic} are
     * registered in this {@link Broker} instance
     *
     * This {@link Topic} is the one used by the
     * {@link #createPublisher(String, Class)} and
     * {@link #createSubscriber(String, Class)} methods
     *
     * @see #createPublisher(String, Class)
     * @see #createSubscriber(String, Class)
     * @return
     * @throws BrokerException
     */
    public <M> Class<Topic<M>> getDefaultTopicClass() throws BrokerException;

    String getBrokerImplementation();
}