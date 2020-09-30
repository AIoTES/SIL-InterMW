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

import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import eu.interiot.intermw.comm.broker.rabbitmq.exceptions.MissingCredentialsException;
import eu.interiot.intermw.commons.interfaces.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * A singleton to manage connections to RabbitMQ
 *
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 */
public class ResourceManager {

    private Logger log = LoggerFactory.getLogger(ResourceManager.class);

    private static Map<String, ResourceManager> connections = new HashMap<String, ResourceManager>();

    private ConnectionFactory cf;

    /**
     * Gets a single instance of the {@link ResourceManager} for the given
     * parameters
     *
     * @param configuration A {@link Configuration} instance with the properties needed to
     *                      connect to RabbitMQ
     * @return A {@link ResourceManager} to connect to RabbitMQ
     * @throws BrokerException Thrown when any of the parameters required is missing
     */
    public static ResourceManager getInstance(Configuration configuration) throws BrokerException {
        String host = configuration.getRabbitmqHostname();
        int port = configuration.getRabbitmqPort();
        String user = configuration.getRabbitmqUsername();
        String password = configuration.getRabbitmqPassword();

        if (host == null || user == null || password == null) {
            throw new MissingCredentialsException();
        }

        String key = host + user;
        if (!connections.containsKey(key)) {
            connections.put(key, new ResourceManager(host, port, user, password));
        }
        return connections.get(key);
    }

    /**
     * The {@link ConnectionFactory} to connect to RabbitMQ
     *
     * @return
     */
    public ConnectionFactory getConnectionFactory() {
        return cf;
    }

    /**
     * Constructor, which we make private to enforce the singleton pattern.
     */
    private ResourceManager() {

    }

    /**
     * Constructor, which we make private to enforce the singleton pattern.
     */
    private ResourceManager(String host, int port, String username, String password) {
        this.connect(host, port, username, password);
    }

    /**
     * Creates the instance {@link CachingConnectionFactory} object
     *
     * @param host     The RabbitMQ host
     * @param port     The RabbitMQ port
     * @param username The RabbitMQ username
     * @param password The RabbitMQ password
     */
    private void connect(String host, int port, String username, String password) {
        cf = new CachingConnectionFactory(host, port);
        ((CachingConnectionFactory) cf).setUsername(username);
        ((CachingConnectionFactory) cf).setPassword(password);

        log.info("Connecting to " + host + " with the rabbit user " + username);
    }
}
