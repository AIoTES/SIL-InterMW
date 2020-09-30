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

import DDS.DataWriter;
import DDS.HANDLE_NIL;
import eu.interiot.intermw.comm.broker.Broker;
import eu.interiot.intermw.comm.broker.Publisher;
import eu.interiot.intermw.comm.broker.Queue;
import eu.interiot.intermw.comm.broker.Topic;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import eu.interiot.intermw.comm.broker.opensplice.util.ErrorHandler;
import eu.interiot.intermw.comm.broker.opensplice.util.ReflectionUtils;

import java.util.List;

@eu.interiot.intermw.comm.broker.annotations.Publisher(broker = "opensplice")
public class PublisherImpl<M> extends AbstractOSPLService<M> implements Publisher<M> {

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
    protected void afterTopicCreated() throws BrokerException {
        try {
            super.afterTopicCreated();

            // create Type
            mgr.registerType(typeSupportClass);

            // create Topic
//			mgr.createTopic(this.topic.getExchangeName());

            // create Publisher
            mgr.createPublisher();

            // create DataWriter
            mgr.createWriter();
            DataWriter dataWriter = mgr.getWriter();
            dwriter = (DataWriter) ReflectionUtils.narrow(writerHelperClass, dataWriter);
        } catch (Exception e) {
            throw new BrokerException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(M message) throws BrokerException {
        ReflectionUtils.register_instance(dwriter, message);
        int status = ReflectionUtils.write(dwriter, message, HANDLE_NIL.value);
        ErrorHandler.checkStatus(status, dataWriterClass + ".write");
    }
}
