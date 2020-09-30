package eu.interiot.intermw.comm.broker.opensplice;

        /*
         *                         OpenSplice DDS
         *
         *   This software and documentation are Copyright 2006 to 2013 PrismTech
         *   Limited and its licensees. All rights reserved. See file:
         *
         *                     $OSPL_HOME/LICENSE
         *
         *   for full copyright notice and license terms.
         *
         */

import DDS.*;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import eu.interiot.intermw.comm.broker.opensplice.util.ErrorHandler;
import org.opensplice.dds.dcps.TypeSupportImpl;

public class DDSEntityManager_6_4 {

    private DomainParticipantFactory dpf;
    private DomainParticipant participant;
    private Topic topic;
    private TopicQosHolder topicQos = new TopicQosHolder();
    private PublisherQosHolder pubQos = new PublisherQosHolder();
    private SubscriberQosHolder subQos = new SubscriberQosHolder();

    private DataWriterQosHolder WQosH = new DataWriterQosHolder();

    private Publisher publisher;
    private DataWriter writer;

    private Subscriber subscriber;
    private DataReader reader;

    private String typeName;
    private String partitionName;

    public void createParticipant(String partitionName) {
        dpf = DomainParticipantFactory.get_instance();
        ErrorHandler.checkHandle(dpf, "DomainParticipantFactory.get_instance");

        participant = dpf.create_participant(DOMAIN_ID_DEFAULT.value, PARTICIPANT_QOS_DEFAULT.value, null,
                STATUS_MASK_NONE.value);
        ErrorHandler.checkHandle(dpf, "DomainParticipantFactory.create_participant");
        this.partitionName = partitionName;
    }

    public void deleteParticipant() {
        dpf.delete_participant(participant);
    }

    public void registerType(TypeSupportImpl ts) {
        typeName = ts.get_type_name();
        int status = ts.register_type(participant, typeName);
        ErrorHandler.checkStatus(status, "register_type");
    }

    public void createTopic(String topicName) {
        int status = -1;
        participant.get_default_topic_qos(topicQos);
        topicQos.value.reliability.kind = ReliabilityQosPolicyKind.RELIABLE_RELIABILITY_QOS;
        topicQos.value.durability.kind = DurabilityQosPolicyKind.TRANSIENT_DURABILITY_QOS;
        status = participant.set_default_topic_qos(topicQos.value);
        ErrorHandler.checkStatus(status, "DomainParticipant.set_default_topic_qos");
        topic = participant.create_topic(topicName, typeName, topicQos.value, null, STATUS_MASK_NONE.value);
        ErrorHandler.checkHandle(topic, "DomainParticipant.create_topic");
    }

    public void deleteTopic() {
        int status = participant.delete_topic(topic);
        ErrorHandler.checkStatus(status, "DDS.DomainParticipant.delete_topic");
    }

    public void createPublisher() {
        int status = participant.get_default_publisher_qos(pubQos);
        ErrorHandler.checkStatus(status, "DomainParticipant.get_default_publisher_qos");

        pubQos.value.partition.name = new String[1];
        pubQos.value.partition.name[0] = partitionName;
        publisher = participant.create_publisher(pubQos.value, null, STATUS_MASK_NONE.value);
        ErrorHandler.checkHandle(publisher, "DomainParticipant.create_publisher");
    }

    public void deletePublisher() {
        participant.delete_publisher(publisher);
    }

    public void createWriter() {
        publisher.get_default_datawriter_qos(WQosH);
        publisher.copy_from_topic_qos(WQosH, topicQos.value);
        WQosH.value.writer_data_lifecycle.autodispose_unregistered_instances = false;
        writer = publisher.create_datawriter(topic, WQosH.value, null, STATUS_MASK_NONE.value);
        ErrorHandler.checkHandle(writer, "Publisher.create_datawriter");
    }

    public void createSubscriber() {
        int status = participant.get_default_subscriber_qos(subQos);
        ErrorHandler.checkStatus(status, "DomainParticipant.get_default_subscriber_qos");

        subQos.value.partition.name = new String[1];
        subQos.value.partition.name[0] = partitionName;
        subscriber = participant.create_subscriber(subQos.value, null, STATUS_MASK_NONE.value);
        ErrorHandler.checkHandle(subscriber, "DomainParticipant.create_subscriber");
    }

    public void deleteSubscriber() {
        participant.delete_subscriber(subscriber);
    }

    public void createReader() {
        reader = subscriber.create_datareader(topic, DATAREADER_QOS_USE_TOPIC_QOS.value, null, STATUS_MASK_NONE.value);
        ErrorHandler.checkHandle(reader, "Subscriber.create_datareader");
    }

    public DataReader getReader() {
        return reader;
    }

    public DataWriter getWriter() {
        return writer;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public Topic getTopic() {
        return topic;
    }

    public DomainParticipant getParticipant() {
        return participant;
    }

    /**
     * Register a type by providing full class name. It must be in classpath at
     * runtime and must be a subclass of {@link TypeSupportImpl}
     *
     * This method is here because it cannot be static (calls non-static
     * registerType(...))
     *
     * @param typeSupportImpl_classname
     * @throws BrokerException
     */
    @SuppressWarnings("rawtypes")
    public void registerType(String typeSupportImpl_classname) throws BrokerException {

        TypeSupportImpl impl = null;
        Class klass = null;

        try {
            klass = Class.forName(typeSupportImpl_classname);
            if (!TypeSupportImpl.class.isAssignableFrom(klass)) {
                throw new Exception("Class provided is not a subclass of TypeSupportImpl");
            }
            impl = (TypeSupportImpl) klass.newInstance();
        } catch (Exception exc) {
            throw new BrokerException("Error while registering type.", exc);
        }
        // call normal function
        registerType(impl);
    }

}