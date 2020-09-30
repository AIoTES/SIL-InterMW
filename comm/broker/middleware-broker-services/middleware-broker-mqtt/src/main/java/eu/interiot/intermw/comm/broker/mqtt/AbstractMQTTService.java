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
import eu.interiot.intermw.comm.broker.abstracts.AbstractService;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Date;
import java.util.Properties;

public abstract class AbstractMQTTService<M> extends AbstractService<M> {

    protected MqttClient mqttClient;
    protected MqttConnectOptions connOpts = new MqttConnectOptions();
    protected MemoryPersistence persistence = new MemoryPersistence();

    public final static String MQTT_PROPERTIY_PREFIX = "mqtt.";

    private final static String MQTT_HOST_PROPERTY = "host";
    private final static String MQTT_QOS_PROPERTY = "qos";
    private final static String MQTT_CLIENTID_PROPERTY = "client-id";
    private final static String MQTT_USER_PROPERTY = "user";
    private final static String MQTT_PASSWORD_PROPERTY = "password";
    private final static int DEFAULT_QOS = 2;

    /**
     * {@inheritDoc}
     */
    @Override
    public void createTopic(String name) throws BrokerException {
        // UNUSED
    }

    @Override
    public void deleteTopic(String name) throws BrokerException {
        // UNUSED
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initConnection(Broker broker) throws BrokerException {
        this.broker = broker;
        connOpts.setCleanSession(true);

        String password = this.getPassword();
        if (!StringUtils.isEmpty(password)) {
            connOpts.setPassword(password.toCharArray());
        }

        String userName = this.getUserName();
        if (!StringUtils.isEmpty(userName)) {
            connOpts.setUserName(userName);
        }

        try {
            mqttClient = new MqttClient(this.getHost(), this.getClientId(), persistence);
            mqttClient.connect(connOpts);
        } catch (Exception e) {
            throw new BrokerException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanUp() throws BrokerException {
        if (this.mqttClient != null) {
            try {
                this.mqttClient.disconnect();
            } catch (Exception e) {
                throw new BrokerException(e);
            }
        }
    }

    protected int getQos() {
        try {
            return Integer.parseInt(this.getMQTTProperties().getProperty(MQTT_QOS_PROPERTY));
        } catch (Exception e) {
            return DEFAULT_QOS;
        }
    }

    protected Properties getMQTTProperties() {
        return this.broker.getConfiguration().getPropertiesWithPrefix(MQTT_PROPERTIY_PREFIX);
    }

    protected String getClientId() {
        return this.getMQTTProperties().getProperty(MQTT_CLIENTID_PROPERTY) + getUniqueClientId();
    }

    protected String getUniqueClientId() {
        return String.valueOf(new Date().getTime());
    }

    private String getHost() {
        return this.getMQTTProperties().getProperty(MQTT_HOST_PROPERTY);
    }

    private String getUserName() {
        return this.getMQTTProperties().getProperty(MQTT_USER_PROPERTY);
    }

    private String getPassword() {
        return this.getMQTTProperties().getProperty(MQTT_PASSWORD_PROPERTY);
    }
}
