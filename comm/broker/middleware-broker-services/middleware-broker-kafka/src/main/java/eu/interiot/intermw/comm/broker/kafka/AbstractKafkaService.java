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
package eu.interiot.intermw.comm.broker.kafka;

import eu.interiot.intermw.comm.broker.Service;
import eu.interiot.intermw.comm.broker.abstracts.AbstractService;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import kafka.admin.AdminUtils;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

import java.util.Properties;

/**
 * {@link AbstractService} implementation for Kafka Services
 *
 * @param <M> The message class this {@link Service} works with
 * @author <a href="mailto:aromeu@prodevelop.es">Alberto Romeu</a>
 */
public abstract class AbstractKafkaService<M> extends AbstractService<M> {

    public final static String KAFKA_PROPERTIY_PREFIX = "kafka.";
    public final static String TOPIC_PROPERTIY_PREFIX = "topic-" + KAFKA_PROPERTIY_PREFIX;
    public final static String CUSTOM_PROPERTIY_PREFIX = "custom-" + KAFKA_PROPERTIY_PREFIX;

    private final static String ZOOKEEPER_HOSTS_LIST_PROPERTY = "zookeeper.connect";
    private final static String REPLICAS_PROPERTY = "replicas";
    private final static String PARTITIONS_PROPERTY = "partitions";
    private final static String CONNECTION_TIMEOUT_PROPERTY = "connection.timeout";

    private final static int DEFAULT_NUMBER_OF_PARTITIONS = 1;
    private final static int DEFAULT_NUMBER_OF_REPLICATION = 1;
    private final static int DEFAULT_TIMEOUT = 10000;

    private boolean isTopicCreated = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void createTopic(String name) throws BrokerException {
        ZkClient zkClient = null;
        ZkUtils zkUtils = null;

        try {
            if (!isTopicCreated) {
                String zookeeperHosts = getZookeeperHostList();

                zkClient = new ZkClient(zookeeperHosts, getTimeout(), getTimeout(), ZKStringSerializer$.MODULE$);
                zkUtils = new ZkUtils(zkClient, new ZkConnection(zookeeperHosts), false);

                boolean topicExists = AdminUtils.topicExists(zkUtils, name);
                if (!topicExists) {
                    AdminUtils.createTopic(zkUtils, name, getPartitions(), getReplicas(), getTopicProperties());
                }

                isTopicCreated = true;
            }
        } catch (Exception ex) {
            throw new BrokerException(ex);
        } finally {
            if (zkClient != null) {
                try {
                    zkClient.close();
                } catch (Exception e) {
                    throw new BrokerException(e);
                }
            }
        }
    }

    @Override
    public void deleteTopic(String name) throws BrokerException {
        ZkClient zkClient = null;

        try {
            String zookeeperHosts = getZookeeperHostList();

            zkClient = new ZkClient(zookeeperHosts, getTimeout(), getTimeout(), ZKStringSerializer$.MODULE$);
            zkClient.deleteRecursive(ZkUtils.getTopicPath(name));
        } catch (Exception e) {
            throw new BrokerException(e);
        } finally {
            if (zkClient != null) {
                try {
                    zkClient.close();
                } catch (Exception e) {
                    throw new BrokerException(e);
                }
            }
        }
    }

    private String getZookeeperHostList() {
        return this.getKafkaProperties().getProperty(ZOOKEEPER_HOSTS_LIST_PROPERTY);
    }

    protected Properties getKafkaProperties() {
        return this.broker.getConfiguration().getPropertiesWithPrefix(KAFKA_PROPERTIY_PREFIX, true);
    }

    protected Properties getTopicProperties() {
        return this.broker.getConfiguration().getPropertiesWithPrefix(TOPIC_PROPERTIY_PREFIX);
    }

    protected Properties getCustomProperties() {
        return this.broker.getConfiguration().getPropertiesWithPrefix(CUSTOM_PROPERTIY_PREFIX);
    }

    protected int getReplicas() {
        try {
            String replicas = this.getCustomProperties().getProperty(REPLICAS_PROPERTY);
            return Integer.valueOf(replicas);
        } catch (Exception e) {
            return DEFAULT_NUMBER_OF_REPLICATION;
        }
    }

    protected int getPartitions() {
        try {
            String partitions = this.getCustomProperties().getProperty(PARTITIONS_PROPERTY);
            return Integer.valueOf(partitions);
        } catch (Exception e) {
            return DEFAULT_NUMBER_OF_PARTITIONS;
        }
    }

    protected int getTimeout() {
        try {
            String timeout = this.getCustomProperties().getProperty(CONNECTION_TIMEOUT_PROPERTY);
            return Integer.valueOf(timeout);
        } catch (Exception e) {
            return DEFAULT_TIMEOUT;
        }
    }
}
