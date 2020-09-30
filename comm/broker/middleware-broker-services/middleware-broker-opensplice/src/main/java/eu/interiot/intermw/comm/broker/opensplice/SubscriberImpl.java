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

import DDS.*;
import eu.interiot.intermw.comm.broker.Listener;
import eu.interiot.intermw.comm.broker.Queue;
import eu.interiot.intermw.comm.broker.Subscriber;
import eu.interiot.intermw.comm.broker.Topic;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import eu.interiot.intermw.comm.broker.opensplice.util.ReflectionUtils;
import org.omg.CORBA.*;
import org.omg.CORBA.Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@eu.interiot.intermw.comm.broker.annotations.Subscriber(broker = "opensplice")
public class SubscriberImpl<M> extends AbstractOSPLService<M> implements Subscriber<M> {

    private Logger log = LoggerFactory.getLogger(SubscriberImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void afterTopicCreated() throws BrokerException {
        try {
            super.afterTopicCreated();

            mgr.registerType(typeSupportClass);
//			mgr.createTopic(this.topic.getExchangeName());
            mgr.createSubscriber();
            mgr.createReader();

            dreader = mgr.getReader();
            ReflectionUtils.narrow(readerHelperClass, dreader);
        } catch (Exception e) {
            throw new BrokerException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribe(Listener<M> listener) throws BrokerException {
        int mask = DDS.DATA_AVAILABLE_STATUS.value;
        dreader.set_listener(new OSPLListener(listener, topic), mask);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribe(Listener<M> listener, Queue queue) throws BrokerException {
        this.subscribe(listener);
    }

    private class OSPLListener implements DDS.DataReaderListener {

        private static final long serialVersionUID = 8311424526989433521L;

        private Listener<M> listener;

        private Method takeMethod = null;
        private Field msgValueField = null;
        private Class<?> seqHolder = null;

        public OSPLListener(Listener<M> listener, Topic<M> topic) throws BrokerException {
            this.listener = listener;

            try {
                Class<?> dataReader = Class.forName(dataReaderClass);
                seqHolder = Class.forName(seqHolderClass);
                msgValueField = seqHolder.getField("value");

                takeMethod = dataReader.getMethod("take", seqHolder, SampleInfoSeqHolder.class, int.class, int.class,
                        int.class, int.class);
            } catch (Exception exc) {
                throw new BrokerException(exc);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void on_data_available(DataReader dataReader) {
            java.lang.Object seqHolderObj = null;
            SampleInfoSeqHolder siSeqHolder = null;

            try {
                seqHolderObj = seqHolder.newInstance();
                siSeqHolder = new SampleInfoSeqHolder();

                takeMethod.invoke(dataReader, seqHolderObj, siSeqHolder, DDS.LENGTH_UNLIMITED.value,
                        DDS.ANY_SAMPLE_STATE.value, DDS.ANY_VIEW_STATE.value, DDS.ALIVE_INSTANCE_STATE.value);

                java.lang.Object msg_vals = msgValueField.get(seqHolderObj);
                if (msg_vals instanceof java.lang.Object[]) {
                    java.lang.Object[] vals = (java.lang.Object[]) msg_vals;
                    for (java.lang.Object val : vals) {
                        listener.handle((M) val);
                    }
                }

            } catch (Exception exc) {
                log.error(exc.getMessage(), exc);
            }
        }

        @Override
        public void on_liveliness_changed(DataReader arg0, LivelinessChangedStatus arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public void on_requested_deadline_missed(DataReader arg0, RequestedDeadlineMissedStatus arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public void on_requested_incompatible_qos(DataReader arg0, RequestedIncompatibleQosStatus arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public void on_sample_lost(DataReader arg0, SampleLostStatus arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public void on_sample_rejected(DataReader arg0, SampleRejectedStatus arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public void on_subscription_matched(DataReader arg0, SubscriptionMatchedStatus arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean _is_a(String repositoryIdentifier) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean _is_equivalent(Object other) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean _non_existent() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public int _hash(int maximum) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Object _duplicate() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void _release() {
            // TODO Auto-generated method stub

        }

        @Override
        public Object _get_interface_def() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Request _request(String operation) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Request _create_request(Context ctx, String operation, NVList arg_list, NamedValue result) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Request _create_request(Context ctx, String operation, NVList arg_list, NamedValue result,
                                       ExceptionList exclist, ContextList ctxlist) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Policy _get_policy(int policy_type) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public DomainManager[] _get_domain_managers() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Object _set_policy_override(Policy[] policies, SetOverrideType set_add) {
            // TODO Auto-generated method stub
            return null;
        }

    }
}
