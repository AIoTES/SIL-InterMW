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
import eu.interiot.intermw.comm.broker.exceptions.EmptyTopicException;

import java.util.List;

/**
 * An abstraction for configuring the topic exchange of a message broker
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 * @param <M>
 *            The message class this {@link Topic} is tied to
 *
 */
public interface Topic<M> {

    /**
     * FIXME DON'T USE THIS AT THE MOMENT
     *
     *
     * A list of routing or binding keys for this topic's message when it is
     * published
     *
     * Depending on the message bus implementation this routing keys can be
     * ignored
     *
     * The usual use of the publishing routing keys consists on assigning a key
     * to each message of a {@link Topic} so that they can be filtered by a
     * {@link Subscriber}
     *
     * Usually zero or one routing key should be provided. For more than one
     * routing key, the message will be published several times, each one with a
     * different routing key
     *
     * @return The list of publishing routing keys
     */
    public List<String> getPublishingRoutings();

    /**
     * FIXME DON'T USE THIS AT THE MOMENT
     *
     * A list of routing or binding keys for a subscriber of this topic's
     * messages
     *
     * Depending on the message bus implementation this routing keys can be
     * ignored
     *
     * The usual use of the subscribing routing keys consists on filtering
     * messages by their publishing routing key
     *
     * When no subscribing routings are provided, the {@link Subscriber} should
     * receive all the messages published
     *
     * When a single subscribing routing key is provided it is expected some
     * filtering
     *
     * When more than one subscribing routing key are provided it is expected to
     * create different subscribers, each one with a different subscribing
     * routing key
     *
     * @return The list of publishing routing keys
     */
    public List<String> getSubscribingRoutings();

    /**
     * An exchange is an entity in the middle of a publisher and a queue in some
     * message bus implementations
     *
     * @return
     */
    public String getExchangeName();

    /**
     * Serializes a message as a string
     *
     * @return The string
     * @throws EmptyTopicException
     */
    public String serialize() throws BrokerException;

    /**
     * Deserializes a string into a message instance
     *
     * Each call to this method creates a new instance of the message
     *
     * @param message
     *            The message instance as a string
     * @param type
     *            The message type which is known at runtime
     * @return The message instance
     * @throws EmptyTopicException
     */
    public M deserialize(String message, Class<M> type) throws BrokerException;

    /**
     * The message instance
     *
     * @return
     * @throws EmptyTopicException
     */
    public M getMessage() throws EmptyTopicException;

    /**
     * Sets the message instance
     *
     * @return
     * @throws EmptyTopicException
     */
    public void setMessage(M message) throws EmptyTopicException;

    /**
     * The {@link Broker} instance
     *
     * @return
     */
    public Broker getBroker();

    /**
     * The message class this {@link Topic} is tied to
     *
     * @return
     */
    public Class<M> getMessageClass();

}
