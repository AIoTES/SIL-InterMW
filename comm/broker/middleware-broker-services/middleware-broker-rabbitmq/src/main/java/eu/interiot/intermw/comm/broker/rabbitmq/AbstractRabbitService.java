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

import eu.interiot.intermw.comm.broker.Broker;
import eu.interiot.intermw.comm.broker.Service;
import eu.interiot.intermw.comm.broker.abstracts.AbstractService;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

/**
 * An abstract {@link Service} implementation for RabbitMQ
 *
 * @param <M> The message class this {@link Service} works with
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 */
public class AbstractRabbitService<M> extends AbstractService<M> {

    protected RabbitTemplate template;

    protected RabbitAdmin admin;
    protected SimpleMessageListenerContainer container;
    protected final static boolean DEFAULT_DURABILITY = true;
    protected final static boolean DEFAULT_AUTODELETE = false;
    protected final static MessageDeliveryMode DEFAULT_MESSAGE_DELIVERY_MODE = MessageDeliveryMode.NON_PERSISTENT;
    protected final static boolean DEFAULT_QUEUE_DURABILITY = false;
    protected final static boolean DEFAULT_QUEUE_EXCLUSIVE = false;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initConnection(Broker broker) throws BrokerException {
        template = new RabbitTemplate(
                ResourceManager.getInstance(broker.getConfiguration()).getConnectionFactory());
        admin = new RabbitAdmin(ResourceManager.getInstance(broker.getConfiguration()).getConnectionFactory());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createTopic(String name) throws BrokerException {
        TopicExchange exchange = new TopicExchange(name, DEFAULT_DURABILITY, DEFAULT_AUTODELETE);
        admin.declareExchange(exchange);
    }

    @Override
    public void deleteTopic(String name) throws BrokerException {
        admin.deleteExchange(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanUp() throws BrokerException {
        if (container != null) {
            container.destroy();
        }
    }
}