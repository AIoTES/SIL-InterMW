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
package eu.interiot.intermw.comm.broker.rabbitmq;

import eu.interiot.intermw.comm.broker.Listener;
import eu.interiot.intermw.comm.broker.Subscriber;
import eu.interiot.intermw.comm.broker.Topic;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import eu.interiot.intermw.comm.broker.rabbitmq.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of the {@link Subscriber} interface for RabbitMQ message
 * bus
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 *
 * @param <M>
 *            The messages class this {@link Subscriber} works with
 */
@eu.interiot.intermw.comm.broker.annotations.Subscriber(broker = "rabbitmq")
public class SubscriberImpl<M> extends AbstractRabbitService<M> implements Subscriber<M> {

    private final static Logger logger = LoggerFactory.getLogger(SubscriberImpl.class);
    private final static String DEFAULT_ROUTING_KEY = Constants.DEFAULT_ROUTING_KEY;
    private List<Binding> bindings = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribe(Listener<M> listener) throws BrokerException {
        String queueName = getDefaultQueueName();

        Queue queue = new Queue(queueName, DEFAULT_QUEUE_DURABILITY, DEFAULT_QUEUE_EXCLUSIVE,
                DEFAULT_AUTODELETE);

        if (admin.getQueueProperties(queueName) == null) {
            admin.declareQueue(queue);
        }

        createSubscriptions(topic, listener, queue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribe(Listener<M> listener, eu.interiot.intermw.comm.broker.Queue _queue)
            throws BrokerException {
        String queueName = _queue.getQueueName();

        Queue queue = new Queue(queueName, _queue.isDurable(), _queue.isExclusive(), _queue.autoDelete(),
                _queue.getArguments());
        if (admin.getQueueProperties(queueName) == null) {
            admin.declareQueue(queue);
        }
        createSubscriptions(topic, listener, queue);
    }

    protected void createSubscriptions(Topic<M> topic, Listener<M> listener, Queue queue) throws BrokerException {
        TopicExchange exchange = new TopicExchange(topic.getExchangeName());
        try {
            admin.declareExchange(exchange);
        } catch (Exception e) {
            // if the topic is already created it will throw an Exception. But we ignore it
        }

        List<String> routingKeys = topic.getSubscribingRoutings();

        if (routingKeys == null || routingKeys.isEmpty()) {
            doSubscribe(topic, listener, queue, exchange, DEFAULT_ROUTING_KEY);
        } else {
            for (String routingKey : routingKeys) {
                doSubscribe(topic, listener, queue, exchange, routingKey);
            }
        }
    }

    protected void doSubscribe(Topic<M> topic, Listener<M> listener, Queue queue, TopicExchange exchange,
                               String routingKey) throws BrokerException {
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey);
        admin.declareBinding(binding);
        bindings.add(binding);

        container = new SimpleMessageListenerContainer(
                ResourceManager.getInstance(this.broker.getConfiguration()).getConnectionFactory());
        container.setMessageListener(new MessageListenerAdapter(new RabbitListener(listener, topic)));
        container.setQueueNames(queue.getName());
        container.start();
    }

    protected String getDefaultQueueName() {
        return topic.getExchangeName() + "_queue";
    }

    private class RabbitListener {

        private Listener<M> listener;
        private Topic<M> topic;

        public RabbitListener(Listener<M> listener, Topic<M> topic) {
            this.listener = listener;
            this.topic = topic;
        }

        @SuppressWarnings({"unused"})
        public void handleMessage(Serializable message) {
            try {
                listener.handle(topic.deserialize(message.toString(), topic.getMessageClass()));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void cleanUp() {
        for (Binding binding : bindings) {
            logger.debug("Removing binding {}...", binding.toString());
            admin.removeBinding(binding);
        }

        String queueName = getDefaultQueueName();
        try {
            logger.debug("Removing RabbitMQ queue {}...", queueName);
            admin.deleteQueue(queueName);
        } catch (Exception e) {
            logger.error("Failed to delete RabbitMQ queue " + queueName, e);
        }

        TopicExchange exchange = new TopicExchange(topic.getExchangeName());
        logger.debug("Deleting exchange {}...", topic.getExchangeName());
        try {
            admin.deleteExchange(exchange.getName());
        } catch (Exception e) {
            logger.error("Failed to delete exchange " + topic.getExchangeName(), e);
        }

        if (container != null) {
            logger.debug("Removing container...");
            container.destroy();
        }
    }
}
