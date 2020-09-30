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
package eu.interiot.intermw.comm.broker.opensplice;

import DDS.DataReader;
import DDS.DataWriter;
import eu.interiot.intermw.comm.broker.Broker;
import eu.interiot.intermw.comm.broker.abstracts.AbstractService;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public abstract class AbstractOSPLService<M> extends AbstractService<M> {

    private Logger log = LoggerFactory.getLogger(AbstractOSPLService.class);

    public final static String OSPL_PROPERTIY_PREFIX = "ospl.";

    public final static String OSPL_PARTITIONNAME_PROPERTY = "partition";

    protected final static String TYPE_SUPPORT_SUFFIX = "TypeSupport";
    protected final static String WRITER_HELPER_SUFFIX = "DataWriterHelper";
    protected final static String DATA_WRITER_SUFFIX = "DataWriter";
    protected final static String READER_HELPER_SUFFIX = "DataReaderHelper";
    protected final static String DATA_READER_SUFFIX = "DataReader";
    protected final static String SEQ_HOLDER_SUFFIX = "SeqHolder";

    protected DDSEntityManager_6_4 mgr;
    protected DataWriter dwriter;
    protected DataReader dreader;

    protected String typeSupportClass;
    protected String writerHelperClass;
    protected String dataWriterClass;
    protected String readerHelperClass;
    protected String dataReaderClass;
    protected String seqHolderClass;

    @Override
    protected void initConnection(Broker broker) throws BrokerException {
        try {
            mgr = new DDSEntityManager_6_4();

            // FIXME should be parameterizable for each topic?
            mgr.createParticipant(getPartitionName());
        } catch (Exception e) {
            throw new BrokerException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void afterTopicCreated() throws BrokerException {
        try {
            String className = this.topic.getMessageClass().getCanonicalName();
            log.debug("class name: " + className);
            typeSupportClass = className + TYPE_SUPPORT_SUFFIX;
            writerHelperClass = className + WRITER_HELPER_SUFFIX;
            dataWriterClass = className + DATA_WRITER_SUFFIX;
            readerHelperClass = className + READER_HELPER_SUFFIX;
            dataReaderClass = className + DATA_READER_SUFFIX;
            seqHolderClass = className + SEQ_HOLDER_SUFFIX;
        } catch (Exception e) {
            throw new BrokerException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createTopic(String name) throws BrokerException {
        mgr.createTopic(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteTopic(String name) throws BrokerException {
        mgr.deleteTopic();
    }

    @Override
    public void cleanUp() throws BrokerException {
        try {
            if (dwriter != null) {
                mgr.getPublisher().delete_datawriter(dwriter);
                mgr.deletePublisher();
                mgr.deleteTopic();
            }

            if (dreader != null) {
                mgr.getSubscriber().delete_datareader(dreader);
                mgr.deleteSubscriber();
            }

            mgr.deleteParticipant();
        } catch (Exception e) {
            throw new BrokerException(e);
        }
    }

    protected Properties getOSPLProperties() {
        return this.broker.getConfiguration().getPropertiesWithPrefix(OSPL_PROPERTIY_PREFIX);
    }

    private String getPartitionName() throws BrokerException {
        String partitionName = this.getOSPLProperties().getProperty(OSPL_PARTITIONNAME_PROPERTY);
        if (StringUtils.isEmpty(partitionName)) {
            throw new BrokerException(
                    "Property: " + OSPL_PARTITIONNAME_PROPERTY + " CANNOT be null. Please review your configuration");
        }

        return partitionName;
    }
}
