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
import eu.interiot.intermw.comm.broker.exceptions.ErrorCodes;
import eu.interiot.intermw.commons.interfaces.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * A default {@link Broker} implementation
 *
 * It uses reflection to create instances of the classes annotated as
 * {@link Publisher}, {@link Subscriber}, {@link Topic}, etc.
 *
 * @see eu.interiot.intermw.comm.broker.annotations
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 *
 */
@eu.interiot.intermw.comm.broker.annotations.Broker
public class DefaultBroker implements Broker {
    private final Logger log = LoggerFactory.getLogger(DefaultBroker.class);

    private final Configuration configuration;
    private final String brokerImplementation;

    /**
     * Creates a new {@link DefaultBroker} given a {@link Configuration}
     *
     * @param configuration
     *            The {@link Configuration} instance
     * @throws BrokerException
     */
    public DefaultBroker(Configuration configuration, String brokerImplementation) throws BrokerException {
        log.debug("Create default broker with configuration " + configuration.getClass().getName());
        this.configuration = configuration;
        this.brokerImplementation = brokerImplementation;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <M> Publisher<M> createPublisher(Class<? extends Topic<M>> topicClass) throws BrokerException {
        try {
            Publisher<M> publisher = (Publisher<M>) BrokerContext.getPublisherClass(brokerImplementation).newInstance();
            publisher.init(this, topicClass);
            return publisher;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BrokerException(e);
        } catch (BrokerException e) {
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <M> Subscriber<M> createSubscriber(Class<? extends Topic<M>> topicClass) throws BrokerException {
        try {
            Subscriber<M> subscriber = (Subscriber<M>) BrokerContext.getSubscriberClass(brokerImplementation).newInstance();
            subscriber.init(this, topicClass);
            return subscriber;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BrokerException(e);
        } catch (BrokerException e) {
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <M> Serializer<M> createSerializer() throws BrokerException {
        try {
            return (Serializer<M>) BrokerContext.getSerializer().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BrokerException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration getConfiguration() {
        return this.configuration;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <M> Subscriber<M> createSubscriber(String exchangeName, Class<M> messageClass) throws BrokerException {
        try {
            Subscriber<M> subscriber = (Subscriber<M>) BrokerContext.getSubscriberClass(brokerImplementation).newInstance();
            subscriber.init(this, exchangeName, messageClass);
            return subscriber;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BrokerException(e);
        } catch (BrokerException e) {
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <M> Subscriber<M> createSubscriber(String exchangeName, String className) throws BrokerException {
        Class<M> messageClass;
        try {
            messageClass = (Class<M>) Class.forName(className);
            return this.createSubscriber(exchangeName, messageClass);
        } catch (Exception e) {
            throw new BrokerException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <M> Publisher<M> createPublisher(String exchangeName, Class<M> messageClass) throws BrokerException {
        try {
            Publisher<M> publisher = (Publisher<M>) BrokerContext.getPublisherClass(brokerImplementation).newInstance();
            publisher.init(this, exchangeName, messageClass);
            return publisher;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BrokerException(e);
        } catch (BrokerException e) {
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <M> Publisher<M> createPublisher(String exchangeName, String className) throws BrokerException {
        Class<M> messageClass;
        try {
            messageClass = (Class<M>) Class.forName(className);
            return this.createPublisher(exchangeName, messageClass);
        } catch (Exception e) {
            throw new BrokerException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <M> Class<Topic<M>> getDefaultTopicClass() throws BrokerException {
        Class<? extends Topic<?>> topicClass = null;
        Set<Class<? extends Topic<Object>>> keySet = BrokerContext.getTopics().keySet();
        for (Class<? extends Topic<Object>> key : keySet) {
            eu.interiot.intermw.comm.broker.annotations.Topic annotation = key
                    .getAnnotation(eu.interiot.intermw.comm.broker.annotations.Topic.class);
            if (annotation != null && annotation.isDefault()) {
                topicClass = key;
                break;
            }
        }

        if (topicClass == null) {
            throw new BrokerException(ErrorCodes.DEFAULT_TOPIC_EXCEPTION.errorCode,
                    ErrorCodes.DEFAULT_TOPIC_EXCEPTION.errorDescription);
        }

        return (Class<Topic<M>>) topicClass;
    }

    @Override
    public String getBrokerImplementation() {
        return brokerImplementation;
    }
}
