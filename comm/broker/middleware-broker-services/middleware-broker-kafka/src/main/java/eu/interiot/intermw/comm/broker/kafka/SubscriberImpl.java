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
import eu.interiot.intermw.comm.broker.Listener;
import eu.interiot.intermw.comm.broker.Subscriber;
import eu.interiot.intermw.comm.broker.Topic;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import kafka.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;

/**
 * This class wraps a Kafka Consumer into an {@link ISubscriber}
 *
 * A Kafka Consumer blocks the {@link Thread} in which it has been thrown, so it
 * needs to be executed in a different thread. This is the reason why this class
 * implements the {@link Runnable} interface
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 */
@eu.interiot.intermw.comm.broker.annotations.Subscriber(broker = "kafka")
public class SubscriberImpl<M> extends AbstractKafkaService<M> implements Subscriber<M> {

    private final static Logger log = LoggerFactory.getLogger(SubscriberImpl.class);

    private Properties properties;
    private ConsumerConfig consumerConfig;
    private KafkaConsumer<String, String> consumer;
    private Listener<M> listener;
    private MessageThread thread;

    /**
     * Subscribes to the topic in a new {@link Thread}. This is because Kafka
     * seems to block the current thread
     *
     * @see Subscriber#subscribe(Object,
     * eu.interiot.intermw.comm.broker.Listener)
     */
    public void subscribe(Listener<M> listener) throws BrokerException {
        this.listener = listener;

        closeThread();

        thread = new MessageThread();
        thread.start();
    }

    /**
     * No queues support in Kafka
     *
     * @see SubscriberImpl#subscribe(Topic, Listener)
     */
    public void subscribe(Listener<M> listener, eu.interiot.intermw.comm.broker.Queue _queue) throws BrokerException {
        this.subscribe(listener);
    }

    /**
     * {@inheritDoc}
     */
    public void cleanUp() throws BrokerException {
        log.debug("Unsubscribing from Kafka topic {}...", topic.getExchangeName());
        try {
            // TODO: closing the consumer causes "java.util.ConcurrentModificationException: KafkaConsumer is not safe for multi-threaded access"
            //if (consumer != null) {
            //    consumer.close();
            //}

        } catch (Exception e) {
            throw new BrokerException(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initConnection(Broker broker) throws BrokerException {
        log.debug("Creating Kafka consumer...");
        properties = this.getKafkaProperties();
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        consumer = new KafkaConsumer<>(properties);
        log.debug("Kafka consumer has been created successfully.");
    }

    private void closeThread() throws BrokerException {
        try {
            if (thread != null) {
                thread.terminate = true;
                if (thread.isInterrupted()) {
                    thread.notify();
                }
            }
        } catch (Exception e) {
            throw new BrokerException(e.getMessage(), e);
        }
    }

    private class MessageThread extends Thread {

        boolean terminate = false;

        /**
         * Connects a Kafka Consumer to the topic. Every message is handled
         * through the {@link Listener} passed as an argument to the
         * {@link #subscribe(Topic, Listener)} method
         */
        @Override
        public void run() {
            super.run();

            consumer.subscribe(Arrays.asList(topic.getExchangeName()));

            while (true) {
                if (terminate) {
                    break;
                }

                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        M messageInstance = topic.deserialize(record.value(), topic.getMessageClass());
                        listener.handle(messageInstance);
                        consumer.commitSync();
                    } catch (Exception e) {
                        log.error("Failed to handle message: " + e.getMessage(), e);
                    }
                }
            }
        }
    }
}
