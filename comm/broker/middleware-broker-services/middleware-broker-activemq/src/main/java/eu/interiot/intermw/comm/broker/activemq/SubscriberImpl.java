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
package eu.interiot.intermw.comm.broker.activemq;

import eu.interiot.intermw.comm.broker.Listener;
import eu.interiot.intermw.comm.broker.Queue;
import eu.interiot.intermw.comm.broker.Subscriber;
import eu.interiot.intermw.comm.broker.Topic;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.List;

@eu.interiot.intermw.comm.broker.annotations.Subscriber(broker = "activemq")
public class SubscriberImpl<M> extends AbstractActiveMQService<M> implements Subscriber<M> {

    protected Logger log = LoggerFactory.getLogger(SubscriberImpl.class);

    protected MessageConsumer consumer;

    /**
     * {@inheritDoc}
     */
    public void subscribe(Listener<M> listener) throws BrokerException {
        try {
            List<String> routingKeys = topic.getSubscribingRoutings();

            if (routingKeys == null || routingKeys.isEmpty()) {
                doSubscribe(topic, listener, destination, null);
            } else {
                for (String routingKey : routingKeys) {
                    doSubscribe(topic, listener, destination, routingKey);
                }
            }
        } catch (JMSException e1) {
            log.error("subscribe", e1);
        }
    }

    protected void doSubscribe(Topic<M> topic, Listener<M> listener, Destination destination, String routingKey)
            throws JMSException {
        MessageConsumer consumer = createConsumer(destination, routingKey);
        consumer.setMessageListener(new ActiveMQListener(listener, topic));
    }

    /**
     * {@link Queue} not supported for activeMQ
     *
     * @see Subscriber#subscribe(Object, Listener)
     */
    public void subscribe(Listener<M> listener, eu.interiot.intermw.comm.broker.Queue _queue)
            throws BrokerException {
        subscribe(listener, null);
    }

    public MessageConsumer createConsumer(Destination destination, String filter) throws JMSException {
        MessageConsumer consumer;

        if (filter == null) {
            consumer = session.createConsumer(destination);
        } else {
            consumer = session.createConsumer(destination, filter);
        }

        return consumer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanUp() {
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException e) {
                log.error("cleanUp", e);
            }
        }

        if (consumer != null) {
            try {
                consumer.close();
            } catch (JMSException e) {
                log.error("cleanUp", e);
            }
        }

        if (session != null) {
            try {
                session.close();
            } catch (JMSException e) {
                log.error("cleanUp", e);
            }
        }
    }

    private class ActiveMQListener implements MessageListener {

        private Listener<M> listener;
        private Topic<M> topic;

        public ActiveMQListener(Listener<M> listener, Topic<M> topic) {
            this.listener = listener;
            this.topic = topic;
        }

        public void onMessage(Message message) {
            try {
                String topicMessage = null;
                if (message instanceof ObjectMessage) {
                    topicMessage = ((ObjectMessage) message).getObject().toString();
                } else if (message instanceof TextMessage) {
                    TextMessage tm = (TextMessage) message;
                    topicMessage = tm.getText();
                } else {
                    log.error("Unsupported ActveMQ Message type: " + message.getClass());
                }

                if (topicMessage != null) {
                    M deserializedMessage = topic.deserialize(topicMessage, topic.getMessageClass());
                    listener.handle(deserializedMessage);
                }
            } catch (Exception e) {
                log.error("onMessage", e);
            }
        }
    }
}
