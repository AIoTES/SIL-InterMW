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

import eu.interiot.intermw.comm.broker.Listener;
import eu.interiot.intermw.comm.broker.Queue;
import eu.interiot.intermw.comm.broker.Subscriber;
import eu.interiot.intermw.comm.broker.Topic;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@eu.interiot.intermw.comm.broker.annotations.Subscriber(broker = "mqtt")
public class SubscriberImpl<M> extends AbstractMQTTService<M> implements Subscriber<M> {

    private Logger log = LoggerFactory.getLogger(SubscriberImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribe(Listener<M> listener) throws BrokerException {
        try {
            mqttClient.setCallback(new MQTTListener(listener, topic));
            mqttClient.subscribe(topic.getExchangeName(), this.getQos());
        } catch (Exception e) {
            throw new BrokerException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribe(Listener<M> listener, Queue queue) throws BrokerException {
        this.subscribe(listener);
    }

    private class MQTTListener implements MqttCallback {

        private Listener<M> listener;
        private Topic<M> topic;

        public MQTTListener(Listener<M> listener, Topic<M> topic) {
            this.listener = listener;
            this.topic = topic;
        }

        @Override
        public void messageArrived(String topicName, MqttMessage message) {
            try {
                listener.handle(topic.deserialize(new String(message.getPayload()), topic.getMessageClass()));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }

        @Override
        public void connectionLost(Throwable e) {
            log.error("connection lost", e);
        }
    }
}
