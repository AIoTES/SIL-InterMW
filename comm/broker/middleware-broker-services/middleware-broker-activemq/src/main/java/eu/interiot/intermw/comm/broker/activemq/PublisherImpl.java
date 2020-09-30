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

import eu.interiot.intermw.comm.broker.Broker;
import eu.interiot.intermw.comm.broker.Publisher;
import eu.interiot.intermw.comm.broker.Queue;
import eu.interiot.intermw.comm.broker.Topic;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import java.util.List;

@eu.interiot.intermw.comm.broker.annotations.Publisher(broker = "activemq")
public class PublisherImpl<M> extends AbstractActiveMQService<M> implements Publisher<M> {

    private Logger log = LoggerFactory.getLogger(PublisherImpl.class);

    protected MessageProducer producer;

    @Override
    public void init(Broker broker, List<Queue> queues, Class<Topic<M>> topicClass) throws BrokerException {
        log.warn("This implementation of ActiveMQ DOES NOT support creation of queues");
        this.init(broker, topicClass);
    }

    @Override
    public void init(Broker broker, List<eu.interiot.intermw.comm.broker.Queue> queues, String exchangeName,
                     Class<M> messageClass) throws BrokerException {
        log.warn("This implementation of ActiveMQ DOES NOT support creation of queues");
        this.init(broker, exchangeName, messageClass);
    }

    @Override
    public void publish(M message) throws BrokerException {
        topic.setMessage(message);
        ObjectMessage objectMessage;
        try {
            if (producer == null) {
                this.createProducer(topic);
            }

            objectMessage = session.createObjectMessage();
            objectMessage.setObject(topic.serialize());
            // FIXME allow routing
            // applyRouting(message, topic);

            producer.send(objectMessage);
        } catch (JMSException e) {
            log.error("publish", e);
            throw new BrokerException(e.getMessage(), e);
        }
    }

    protected void createProducer(Topic<M> topic) throws BrokerException {
        try {
            producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        } catch (JMSException e) {
            throw new BrokerException(e.getMessage(), e);
        }
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

        if (producer != null) {
            try {
                producer.close();
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
}
