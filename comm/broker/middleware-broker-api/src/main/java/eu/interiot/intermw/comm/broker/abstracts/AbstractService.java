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
import eu.interiot.intermw.comm.broker.Service;
import eu.interiot.intermw.comm.broker.Topic;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * An abstract {@link Service} implementation
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 *
 * @param <M>
 *            The message class this {@link Service} works with
 */
public abstract class AbstractService<M> implements Service<M> {

    private final Logger log = LoggerFactory.getLogger(AbstractService.class);

    protected Broker broker;
    protected Topic<M> topic;

    /**
     * Override for initializing the connection of this {@link Service} to the
     * message broker implementation
     *
     * @param broker
     *            The {@link Broker} instance
     * @throws BrokerException
     */
    protected abstract void initConnection(Broker broker) throws BrokerException;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Broker broker, Class<? extends Topic<M>> topicClass) throws BrokerException {
        this.init(broker);
        this.createTopic(topicClass);
        this.afterTopicCreated();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Broker broker, String exchangeName, Class<M> messageClass) throws BrokerException {
        this.init(broker);
        this.createTopic(exchangeName, messageClass);
        this.afterTopicCreated();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Topic<M> getTopic() {
        return this.topic;
    }

    /**
     * This is a hook for subclasses to perform anything after the {@link Topic}
     * has been created
     *
     * @throws BrokerException
     */
    protected void afterTopicCreated() throws BrokerException {
        this.createTopic(this.topic.getExchangeName());
    }

    private void init(Broker broker) throws BrokerException {
        this.broker = broker;
        this.initConnection(broker);

        if (log.isTraceEnabled()) {
            log.trace("new publisher inited");
        }
    }

    private void createTopic(Class<? extends Topic<M>> topicClass) throws BrokerException {
        try {
            topic = topicClass.getConstructor(Broker.class).newInstance(this.broker);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new BrokerException(e);
        }
    }

    private void createTopic(String exchangeName, Class<M> messageClass) throws BrokerException {
        Class<Topic<M>> topicClass = broker.getDefaultTopicClass();
        try {
            topic = topicClass.getConstructor(String.class, Broker.class, Class.class).newInstance(exchangeName,
                    this.broker, messageClass);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new BrokerException(e);
        }
    }
}
