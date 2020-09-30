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
package eu.interiot.intermw.comm.broker.mqtt;

import eu.interiot.intermw.comm.broker.Broker;
import eu.interiot.intermw.comm.broker.Publisher;
import eu.interiot.intermw.comm.broker.Queue;
import eu.interiot.intermw.comm.broker.Topic;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.List;

@eu.interiot.intermw.comm.broker.annotations.Publisher(broker = "mqtt")
public class PublisherImpl<M> extends AbstractMQTTService<M> implements Publisher<M> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Broker broker, List<Queue> queues, Class<Topic<M>> topicClass) throws BrokerException {
        this.init(broker, topicClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Broker broker, List<Queue> queues, String exchangeName, Class<M> messageClass)
            throws BrokerException {
        this.init(broker, exchangeName, messageClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(M message) throws BrokerException {
        try {
            this.topic.setMessage(message);
            MqttMessage mqttMessage = new MqttMessage(this.topic.serialize().getBytes());
            mqttMessage.setQos(this.getQos());
            this.mqttClient.publish(this.topic.getExchangeName(), mqttMessage);
        } catch (Exception e) {
            throw new BrokerException(e);
        }
    }
}
