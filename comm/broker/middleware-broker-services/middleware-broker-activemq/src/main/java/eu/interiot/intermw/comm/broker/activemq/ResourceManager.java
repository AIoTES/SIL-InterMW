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

import eu.interiot.intermw.comm.broker.activemq.exceptions.MissingCredentialsException;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import eu.interiot.intermw.commons.interfaces.Configuration;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;

import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import java.util.HashMap;
import java.util.Map;

public class ResourceManager implements ExceptionListener {

    private Logger log = LoggerFactory.getLogger(ResourceManager.class);

    private final static String ACTIVEMQ_HOST_PROPERTY = "activemq-host";
    private final static String ACTIVEMQ_USER_PROPERTY = "activemq-user";
    private final static String ACTIVEMQ_PASSWORD_PROPERTY = "activemq-password";

    private static Map<String, ResourceManager> connections = new HashMap<String, ResourceManager>();

    private ConnectionFactory cf;

    /**
     * Create the instance {@link CachingConnectionFactory} object
     *
     * @param host
     *            The ActiveMQ host
     * @param username
     *            The ActiveMQ username
     * @param password
     *            The ActiveMQ password
     */
    private void connect(String host, String username, String password) {
        cf = new ActiveMQConnectionFactory(username, password, host);

        log.info("Connecting to " + host + " with the user " + username);
    }

    /**
     * Gets a single instance of the {@link ResourceManager} for the given
     * parameters
     *
     * @param configuration
     *            A {@link Configuration} instance with the properties needed to
     *            connect to ActiveMQ
     * @return A {@link ResourceManager} to connect to ActiveMQ
     * @throws BrokerException
     *             Thrown when any of the parameters required is missing
     */
    public static ResourceManager getInstance(Configuration configuration) throws BrokerException {
        String host = configuration.getProperty(ACTIVEMQ_HOST_PROPERTY);
        String user = configuration.getProperty(ACTIVEMQ_USER_PROPERTY);
        String password = configuration.getProperty(ACTIVEMQ_PASSWORD_PROPERTY);

        if (host == null || user == null || password == null) {
            throw new MissingCredentialsException();
        }

        String key = host + user;
        if (!connections.containsKey(key)) {
            connections.put(key, new ResourceManager(host, user, password));
        }
        return connections.get(key);
    }

    /**
     * The {@link ConnectionFactory} to connect to ActiveMQ
     *
     * @return
     */
    public ConnectionFactory getConnectionFactory() {
        return cf;
    }

    @Override
    public void onException(JMSException error) {
        log.error("ResourceManager ERROR: ", error);
    }

    private ResourceManager() {

    }

    /**
     * Constructor, which we make private to enforce the singleton pattern.
     */
    private ResourceManager(String host, String username, String password) {
        this.connect(host, username, password);
    }
}
