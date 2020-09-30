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
import eu.interiot.intermw.comm.broker.abstracts.AbstractService;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import eu.interiot.intermw.comm.broker.exceptions.ErrorCodes;
import org.apache.activemq.broker.jmx.BrokerViewMBean;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.Properties;

public abstract class AbstractActiveMQService<M> extends AbstractService<M> {

    protected Connection connection;
    protected Session session;
    protected Destination destination;

    public final static String ACTIVEMQ_PROPERTIY_PREFIX = "activemq-";

    protected String domain = "org.apache.activemq";

    protected Properties getActiveMQProperties() {
        return this.broker.getConfiguration().getPropertiesWithPrefix(ACTIVEMQ_PROPERTIY_PREFIX);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initConnection(Broker broker) throws BrokerException {
        try {
            connection = ResourceManager.getInstance(broker.getConfiguration()).getConnectionFactory()
                    .createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            throw new BrokerException(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void createTopic(String name) throws BrokerException {
        if (session == null) {
            throw new BrokerException(ErrorCodes.CANNOT_CREATE_TOPIC.errorCode,
                    ErrorCodes.CANNOT_CREATE_TOPIC.errorDescription);
        }

        try {
            destination = session.createTopic(name);
        } catch (JMSException e) {
            throw new BrokerException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * FIXME: To be tested with a running instance
     */
    public void deleteTopic(String name) throws BrokerException {
        JMXConnector jmxc = null;
        MBeanServerConnection conn = null;

        try {
            JMXServiceURL url = new JMXServiceURL(
                    "service:jmx:rmi:///jndi/rmi://" + this.getActiveMQProperties().getProperty("rmi") + "/jmxrmi");
            jmxc = JMXConnectorFactory.connect(url);
            conn = jmxc.getMBeanServerConnection();

            ObjectName brokerName = new ObjectName("org.apache.activemq:BrokerName=" + this.getActiveMQProperties().getProperty("brokername") + ",Type=Broker");
            BrokerViewMBean broker = (BrokerViewMBean) MBeanServerInvocationHandler.newProxyInstance(conn, brokerName,
                    BrokerViewMBean.class, true);

            broker.removeTopic(name);

        } catch (Exception e) {
            throw new BrokerException(e);
        } finally {
            if (jmxc != null) {
                try {
                    jmxc.close();
                } catch (Exception e) {
                    throw new BrokerException(e);
                }
            }
        }
    }

}
