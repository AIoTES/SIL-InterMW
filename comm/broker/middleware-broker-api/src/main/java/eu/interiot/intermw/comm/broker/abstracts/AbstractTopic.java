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
package eu.interiot.intermw.comm.broker.abstracts;

import eu.interiot.intermw.comm.broker.Broker;
import eu.interiot.intermw.comm.broker.Routing;
import eu.interiot.intermw.comm.broker.Serializer;
import eu.interiot.intermw.comm.broker.Topic;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import eu.interiot.intermw.comm.broker.exceptions.EmptyTopicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * An abstract {@link Topic} implementation
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 *
 * @param <M>
 *            The message class this {@link Topic} is tied to
 */
public abstract class AbstractTopic<M> implements Topic<M> {

    private final static Logger log = LoggerFactory.getLogger(AbstractTopic.class);

    protected M message;
    protected Broker broker;
    protected Routing<M> routing;

    private Serializer<M> json;
    private Class<M> messageClass;

    private String exchangeName;

    /**
     * Creates a new {@link Topic} given a {@link Broker} instance
     *
     * @param broker
     *            The {@link Broker} instance
     * @throws BrokerException
     */
    public AbstractTopic(Broker broker) throws BrokerException {
        this.json = broker.createSerializer();

        this.broker = broker;
    }

    /**
     * Creates a new {@link Topic} instance
     *
     * @param exchangeName
     *            The name of the message broker exchange (or topic)
     * @param broker
     *            The {@link Broker} instance
     * @param messageClass
     *            The message class this {@link Topic} works with
     * @throws BrokerException
     */
    public AbstractTopic(String exchangeName, Broker broker, Class<M> messageClass) throws BrokerException {
        this(broker);

        this.messageClass = messageClass;
        this.exchangeName = exchangeName;
    }

    // /**
    // * Creates a new {@link Topic} instance
    // *
    // * @param exchangeName
    // * The name of the message broker exchange (or topic)
    // * @param broker
    // * The {@link Broker} instance
    // * @param routing
    // * The {@link Routing}instance
    // * @param messageClass
    // * The message class this {@link Topic} works with
    // * @throws BrokerException
    // */
    // public AbstractTopic(String exchangeName, Broker broker,
    // Routing<M> routing, Class<M> messageClass)
    // throws BrokerException {
    // this(exchangeName, broker, messageClass);
    // this.routing = routing;
    // }

    /**
     * {@inheritDoc}
     */
    @Override
    public String serialize() throws BrokerException {
        if (message == null) {
            throw new EmptyTopicException();
        }
        return json.serialize(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public M deserialize(String message, Class<M> type) throws BrokerException {
        return json.deserialize(message, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public M getMessage() throws EmptyTopicException {
        if (message == null) {
            throw new EmptyTopicException();
        }
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMessage(M message) throws EmptyTopicException {
        if (message == null) {
            throw new EmptyTopicException();
        }
        this.message = message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getPublishingRoutings() {
        List<String> routings = null;
        if (this.routing != null) {
            try {
                routings = this.routing.getPublishingRoutings(this, null);
            } catch (BrokerException e) {
                log.error(e.getMessage(), e);
            }
        }

        return routings;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSubscribingRoutings() {
        List<String> routings = null;
        if (this.routing != null) {
            try {
                routings = this.routing.getSubscribingRoutings(this);
            } catch (BrokerException e) {
                log.error(e.getMessage(), e);
            }
        }

        return routings;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Broker getBroker() {
        return this.broker;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Class<M> getMessageClass() {
        if (this.messageClass == null) {
            this.messageClass = (Class<M>) ((ParameterizedType) getClass().getGenericSuperclass())
                    .getActualTypeArguments()[0];
        }

        return this.messageClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getExchangeName() {
        return this.exchangeName;
    }
}
