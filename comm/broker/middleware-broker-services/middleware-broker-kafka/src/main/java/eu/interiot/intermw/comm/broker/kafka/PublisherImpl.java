/**
 * INTER-IoT. Interoperability of IoT Platforms.
 * INTER-IoT is a R&D project which has received funding from the European
 * Union<92>s Horizon 2020 research and innovation programme under grant
 * agreement No 687283.
 * <p>
 * Copyright (C) 2016-2018, by (Author's company of this file):
 * - Prodevelop S.L.
 * <p>
 * <p>
 * For more information, contact:
 * - @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 * - Project coordinator:  <a href="mailto:coordinator@inter-iot.eu"></a>
 * <p>
 * <p>
 * This code is licensed under the EPL license, available at the root
 * application directory.
 */
package eu.interiot.intermw.comm.broker.kafka;

import eu.interiot.intermw.comm.broker.Broker;
import eu.interiot.intermw.comm.broker.Publisher;
import eu.interiot.intermw.comm.broker.Queue;
import eu.interiot.intermw.comm.broker.Topic;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import eu.interiot.intermw.commons.interfaces.Configuration;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 */
@eu.interiot.intermw.comm.broker.annotations.Publisher(broker = "kafka")
public class PublisherImpl<M> extends AbstractKafkaService<M> implements Publisher<M> {

    private final static Logger log = LoggerFactory.getLogger(PublisherImpl.class);

    private Properties properties;
    private Producer<String, String> producer;

    @Override
    public void init(Broker broker, List<Queue> queues, Class<Topic<M>> topicClass) throws BrokerException {
        log.warn("Queues support not implemented for Kafka");
        log.warn(
                "The publisher will be initiated with no queues assigned. Take care of this if it is not what you expected");

        this.init(broker, topicClass);
    }

    /**
     * Queues support not implemented for Kafka
     *
     * @throws BrokerException
     * @see PublisherImpl#init(Configuration)
     */
    @Override
    public void init(Broker broker, List<eu.interiot.intermw.comm.broker.Queue> queues, String exchangeName,
                     Class<M> messageClass) throws BrokerException {
        log.warn("Queues support not implemented for Kafka");
        log.warn(
                "The publisher will be initiated with no queues assigned. Take care of this if it is not what you expected");

        this.init(broker, exchangeName, messageClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initConnection(Broker broker) throws BrokerException {
        properties = this.getKafkaProperties();
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try {
            this.cleanUp();
        } catch (BrokerException ignore) {
            log.warn(ignore.getMessage());
        }

        producer = new KafkaProducer<>(properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(M message) throws BrokerException {
        if (producer == null) {
            throw new BrokerException(
                    "Producer is null! Cannot publish any message. Please check your client configuration");
        }

        topic.setMessage(message);

        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic.getExchangeName(), topic.serialize());
        producer.send(producerRecord);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanUp() throws BrokerException {
        try {
            if (producer != null) {
                producer.close();
            }
        } catch (Exception e) {
            throw new BrokerException(e.getMessage(), e);
        }
    }
}
