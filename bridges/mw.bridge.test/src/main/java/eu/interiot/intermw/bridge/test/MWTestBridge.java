/**
 * INTER-IoT. Interoperability of IoT Platforms.
 * INTER-IoT is a R&D project which has received funding from the European
 * Union’s Horizon 2020 research and innovation programme under grant
 * agreement No 687283.
 * <p>
 * Copyright (C) 2016-2018, by (Author's company of this file):
 * - XLAB d.o.o.
 * <p>
 * This code is licensed under the EPL license, available at the root
 * application directory.
 */
package eu.interiot.intermw.bridge.test;

import eu.interiot.intermw.bridge.BridgeConfiguration;
import eu.interiot.intermw.bridge.abstracts.AbstractBridge;
import eu.interiot.intermw.bridge.exceptions.BridgeException;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import eu.interiot.intermw.commons.exceptions.InvalidMessageException;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.model.IoTDevice;
import eu.interiot.intermw.commons.model.Platform;
import eu.interiot.intermw.commons.requests.*;
import eu.interiot.message.ID.EntityID;
import eu.interiot.message.ID.PropertyID;
import eu.interiot.message.Message;
import eu.interiot.message.MessageMetadata;
import eu.interiot.message.exceptions.MessageException;
import eu.interiot.message.managers.URI.URIManagerMessageMetadata;
import eu.interiot.message.payload.types.IoTDevicePayload;
import eu.interiot.message.payload.types.ObservationPayload;
import eu.interiot.message.payload.types.ResultPayload;
import eu.interiot.message.payload.types.SensorPayload;
import eu.interiot.message.utils.MessageUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@eu.interiot.intermw.bridge.annotations.Bridge(platformType = "http://inter-iot.eu/MWTestPlatform")
public class MWTestBridge extends AbstractBridge {
    private static final Logger logger = LoggerFactory.getLogger(MWTestBridge.class);
    protected ScheduledExecutorService scheduler;
    protected Map<String, ScheduledFuture<?>> subscriptionTaskMap = new HashMap<>();
    private Map<String, ScheduledFuture<?>> deviceDescoveryTaskMap = new HashMap<>();
    private ScheduledFuture<Boolean> registryInitialized;
    private int addOrRemoveDevices = 0;
    private boolean isDeviceRegistryInitMessageEnabled = false;

    //    private static final String MW_TEST_BRIDGE_DEVICE_PREFIX = "http://test.inter-iot.eu/device_phy_";
    private static final String SENSOR_ID_PREFIX = "http://test.inter-iot.eu/sensor_";
    private static final String RESULT_ID_PREFIX = "http://test.inter-iot.eu/result_";
    private static final String LOCATION_ID_PREFIX = "http://test.inter-iot.eu/location_";
    private static final String FEATURE_OF_INTEREST_ID_PREFIX = "http://test.inter-iot.eu/feature_of_interest_";
    private static final String OBSERVED_PROPERTY_ID_PREFIX = "http://test-inter-iot.eu/observed_property_";
    private static final String PHENOMENON_TIME_ID_PREFIX = "http://test.inter-iot.eu/phenomenon_time_";
    private static final String RESULT_TIME_ID_PREFIX = "http://test.inter-iot.eu/result_time_";

    public MWTestBridge(BridgeConfiguration configuration, Platform platform) throws MiddlewareException {
        super(configuration, platform);
        logger.debug("MWTestBridge is initializing...");
        // check if bridge configuration is loaded correctly
        if (!"instanceA-value1".equals(configuration.getProperty("testproperty1")) ||
                !"value2".equals(configuration.getProperty("testproperty2")) ||
                !"value3".equals(configuration.getProperty("testproperty3"))) {
            throw new BridgeException("Invalid bridge configuration.");
        }
        this.addOrRemoveDevices = Integer.valueOf(configuration.getProperty("addOrRemoveDevices"));

        String isInitMessageEnabled = configuration.getProperty("deviceReigstryInitMessage");
        this.isDeviceRegistryInitMessageEnabled = Boolean.valueOf(isInitMessageEnabled);

        scheduler = Executors.newSingleThreadScheduledExecutor();
        logger.info("MWTestBridge has been initialized successfully.");
    }

    @Override
    public Message registerPlatform(Message message) throws Exception {
        logger.debug("Registering platform {}...", platform.getPlatformId());
        Message responseMessage = createResponseMessage(message);
        logger.debug("Platform has been registered successfully.");
        return responseMessage;
    }

    @Override
    public Message updatePlatform(Message message) throws Exception {
        logger.debug("Updating platform {}...", platform.getPlatformId());
        UpdatePlatformReq req = new UpdatePlatformReq(message);
        Platform updatedPlatform = req.getPlatform();
        if (!Objects.equals(updatedPlatform.getBaseEndpoint(), platform.getBaseEndpoint())) {
            logger.debug("baseEndpoint has been changed.");
        }
        if (!Objects.equals(updatedPlatform.getUsername(), platform.getUsername()) ||
                !Objects.equals(updatedPlatform.getEncryptedPassword(), platform.getEncryptedPassword()) ||
                !Objects.equals(updatedPlatform.getEncryptionAlgorithm(), platform.getEncryptionAlgorithm())) {
            logger.debug("Authentication data has been changed.");
        }

        platform = updatedPlatform;

        logger.debug("Platform has been updated successfully.");
        return createResponseMessage(message);
    }

    @Override
    public Message unregisterPlatform(Message message) throws Exception {
        logger.debug("Unregistering platform {}...", platform.getPlatformId());
        Message responseMessage = createResponseMessage(message);
        logger.debug("Platform has been unregistered successfully.");
        return responseMessage;
    }

    @Override
    public Message subscribe(Message message) throws InvalidMessageException {
        SubscribeReq subscribeReq = new SubscribeReq(message);
        String conversationId = subscribeReq.getConversationId();
        ObservationDispatcher dispatcher = new ObservationDispatcher(subscribeReq.getDeviceIds(), conversationId);
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleAtFixedRate(dispatcher, 1, 2, TimeUnit.SECONDS);
        subscriptionTaskMap.put(conversationId, scheduledFuture);
        logger.debug("Subscription {} has been scheduled.", conversationId);
        return createResponseMessage(message);
    }

    @Override
    public Message unsubscribe(Message message) throws BridgeException {
        UnsubscribeReq unsubscribeReq = new UnsubscribeReq(message);
        String subscriptionId = unsubscribeReq.getSubscriptionId();
        if (!subscriptionTaskMap.containsKey(subscriptionId)) {
            throw new BridgeException("Subscription " + subscriptionId + " does not exist.");
        }
        ScheduledFuture<?> scheduledFuture = subscriptionTaskMap.get(subscriptionId);
        scheduledFuture.cancel(true);
        subscriptionTaskMap.remove(subscriptionId);
        logger.debug("Subscription {} has been canceled.", subscriptionId);
        return createResponseMessage(message);
    }

    @Override
    public Message query(Message message) {
        Message responseMessage = createResponseMessage(message);

        IoTDevicePayload reqIoTDevicePayload = message.getPayloadAsGOIoTPPayload().asIoTDevicePayload();
        Set<EntityID> reqIoTDevices = reqIoTDevicePayload.getIoTDevices();
        if (reqIoTDevices.isEmpty()) {
            IoTDevicePayload resIoTDevicePayload = responseMessage.getPayloadAsGOIoTPPayload().asIoTDevicePayload();
            for (int i = 0; i < 10; i++) {
                EntityID reqIoTDevice = new EntityID(SENSOR_ID_PREFIX + i);
                resIoTDevicePayload.createIoTDevice(reqIoTDevice);

                ObservationPayload observationPayload = resIoTDevicePayload.asObservationPayload();
                observationPayload.createObservation(reqIoTDevice);

//                ObservationPayload observationPayload = resIoTDevicePayload.asObservationPayload();
                observationPayload.setHasFeatureOfInterest(reqIoTDevice, new EntityID(FEATURE_OF_INTEREST_ID_PREFIX + i));
                observationPayload.setHasLocation(reqIoTDevice, new EntityID(LOCATION_ID_PREFIX + i));
                observationPayload.setHasResult(reqIoTDevice, new EntityID(RESULT_ID_PREFIX + i));
                observationPayload.setMadeBySensor(reqIoTDevice, new EntityID(SENSOR_ID_PREFIX + i));
                observationPayload.setObservedProperty(reqIoTDevice, new EntityID(OBSERVED_PROPERTY_ID_PREFIX + i));
                observationPayload.setPhenomenonTime(reqIoTDevice, new EntityID(PHENOMENON_TIME_ID_PREFIX + i));
                observationPayload.setResultTime(reqIoTDevice, RESULT_TIME_ID_PREFIX + i);

            }

        } else {
            // IoTDevice list supplied, return same list with additional "sensor data"
            IoTDevicePayload resIoTDevicePayload = responseMessage.getPayloadAsGOIoTPPayload().asIoTDevicePayload();
            int i = 0;
            for (EntityID reqIoTDevice : reqIoTDevices) {
                resIoTDevicePayload.createIoTDevice(reqIoTDevice);

                ObservationPayload observationPayload = resIoTDevicePayload.asObservationPayload();
                observationPayload.setHasFeatureOfInterest(reqIoTDevice, new EntityID(FEATURE_OF_INTEREST_ID_PREFIX + i));
                observationPayload.setHasLocation(reqIoTDevice, new EntityID(LOCATION_ID_PREFIX + i));
                observationPayload.setHasResult(reqIoTDevice, new EntityID(RESULT_ID_PREFIX + i));
                observationPayload.setMadeBySensor(reqIoTDevice, new EntityID(SENSOR_ID_PREFIX + i));
                observationPayload.setObservedProperty(reqIoTDevice, new EntityID(OBSERVED_PROPERTY_ID_PREFIX + i));
                observationPayload.setPhenomenonTime(reqIoTDevice, new EntityID(PHENOMENON_TIME_ID_PREFIX + i));
                observationPayload.setResultTime(reqIoTDevice, RESULT_TIME_ID_PREFIX + i);

            }
        }
        logger.debug("Publishing QUERY message from bridge. ConversationId {}", responseMessage.getMetadata().getConversationId());
        return responseMessage;
    }

    @Override
    public Message listDevices(Message message) {
        String conversationId = MessageUtils.generateConversationID();
        message.getMetadata().setConversationId(conversationId);

        logger.debug("Publishing DEVICE_REGISTRY_INITIALIZE");
        DeviceRegistryInitialize registryInitialize = new DeviceRegistryInitialize();
        registryInitialized = scheduler.schedule(registryInitialize, 0, TimeUnit.SECONDS);

        DeviceUpdatesDispatcher dispatcher = new DeviceUpdatesDispatcher();

        ScheduledFuture<?> scheduledFuture = scheduler.schedule(dispatcher, 2, TimeUnit.SECONDS);
        deviceDescoveryTaskMap.put(conversationId, scheduledFuture);
        logger.debug("DeviceUpdates {} has been scheduled.", conversationId);
        return createResponseMessage(message);
    }

    @Override
    public Message platformCreateDevices(Message message) throws InvalidMessageException {
        PlatformCreateDeviceReq req = new PlatformCreateDeviceReq(message);
        for (IoTDevice device : req.getDevices()) {
            logger.debug("Creating (starting to manage) new device {} on the platform {}...", device.getDeviceId(), device.getHostedBy());
        }

        return createResponseMessage(message);
    }

    @Override
    public Message platformUpdateDevices(Message message) throws InvalidMessageException {

        PlatformUpdateDeviceReq req = new PlatformUpdateDeviceReq(message);
        for (IoTDevice device : req.getDevices()) {
            logger.debug("Updating old device {} on the platform {}...", device.getDeviceId(), device.getHostedBy());
        }

        return createResponseMessage(message);

    }

    @Override
    public Message platformDeleteDevices(Message message) throws InvalidMessageException {
        PlatformDeleteDeviceReq req = new PlatformDeleteDeviceReq(message);
        for (String device : req.getDeviceIds()) {
            logger.debug("Updating old device {} on the platform {}...", device, req.getPlatformId());
        }

        return createResponseMessage(message);
    }

    @Override
    public Message observe(Message message) {
        return null;
    }

    @Override
    public Message actuate(Message message) throws InvalidMessageException {
//         TODO check if this response message conforms to documentation about actuation responses
        return createResponseMessage(message);
    }

    @Override
    public Message error(Message message) {
        return null;
    }

    @Override
    public Message unrecognized(Message message) {
        return null;
    }

    private Message createObservationMessage(String messageString, String conversationId, String deviceId) throws IOException, MessageException {
        Message message = new Message(messageString);
        message.getMetadata().setConversationId(conversationId);
        message.getMetadata().setMessageID(MessageUtils.generateMessageID());
        message.getMetadata().asPlatformMessageMetadata().setSenderPlatformId(new EntityID(platform.getPlatformId()));


        EntityID observationEntityId = message.getPayloadAsGOIoTPPayload().asObservationPayload().getObservations().iterator().next();
        message.getPayloadAsGOIoTPPayload().asObservationPayload().setMadeBySensor(observationEntityId, new EntityID(deviceId));
        return message;
    }

    protected Message createComplexObservationMessage(String conversationId, String deviceId, String sensorValue) {
        ObservationPayload payload = new ObservationPayload();

        // Observation 1 ----
        EntityID observationID = new EntityID("http://test.inter-iot.eu/observations/1");
        payload.createObservation(observationID);

        // Observation Name
        payload.asGOIoTPPayload().setDataPropertyAssertionForEntity(
                observationID,
                new PropertyID("http://inter-iot.eu/GOIoTP#hasName"),
                "Observation name");

        // Observation result time
        payload.setResultTime(observationID, new Date().toString());

        // Result and payload.hasResult
        ResultPayload resultPayload = payload.asResultPayload();
        EntityID resultID = new EntityID();
        payload.setHasResult(observationID, resultID);
        resultPayload.createResult(resultID);
        resultPayload.setHasLocation(resultID, new EntityID("http://test.inter-iot.eu/TestLocation"));
        resultPayload.setHasResultValue(resultID, sensorValue);
        resultPayload.setHasUnit(resultID, new EntityID("http://sweet.jpl.nasa.gov/2.3/reprSciUnits.owl#degreeC"));

        // Sensor and payload.madeBySensor
        SensorPayload sensorPayload = payload.asSensorPayload();
        EntityID sensorID = new EntityID();
        payload.setMadeBySensor(observationID, sensorID);
        sensorPayload.createSensor(sensorID);
        sensorPayload.setHasName(sensorID, "Humidity sensor");
        sensorPayload.setHasName(observationID, "Humidity outside");
        sensorPayload.setIsHostedBy(sensorID, new EntityID("http://localhost:8080/in-name/humidity"));

        // IoTDevice
        IoTDevicePayload ioTDevicePayload = payload.asIoTDevicePayload();
        EntityID ioTDeviceID = new EntityID(deviceId);
        ioTDevicePayload.createIoTDevice(ioTDeviceID);
        ioTDevicePayload.setHasName(ioTDeviceID, "Some sensoring device");

        Message observationMessage = new Message();
        observationMessage.setPayload(payload);

        MessageMetadata metadata = observationMessage.getMetadata();
        metadata.setMessageID(MessageUtils.generateMessageID());
        metadata.setDateTimeStamp(Calendar.getInstance());
        metadata.setConversationId(conversationId);
        metadata.addMessageType(URIManagerMessageMetadata.MessageTypesEnum.OBSERVATION);
        metadata.asPlatformMessageMetadata().setSenderPlatformId(new EntityID(platform.getPlatformId()));

        return observationMessage;
    }

    private Message createSimpleObservationMessage(String conversationId, String deviceId, int sensorValue) {
        ObservationPayload payload = new ObservationPayload();
        EntityID observationID = new EntityID("http://test.inter-iot.eu/observations/1");
        payload.createObservation(observationID);
        payload.setHasLocation(observationID, new EntityID("http://test.inter-iot.eu/TestLocation"));
        payload.setHasFeatureOfInterest(observationID, new EntityID("http://test.inter-iot.eu/foi"));
        payload.setHasResult(observationID, new EntityID("http://test.inter-iot.eu/" + sensorValue));
        payload.setMadeBySensor(observationID, new EntityID(deviceId));
        payload.setObservedProperty(observationID, new EntityID("http://test.inter-iot.eu/temperature"));
        payload.setPhenomenonTime(observationID, new EntityID("http://test.inter-iot.eu/phenomenonTime"));
        payload.setResultTime(observationID, new Date().toString());

        Message observationMessage = new Message();
        observationMessage.setPayload(payload);

        MessageMetadata metadata = observationMessage.getMetadata();
        metadata.setMessageID(MessageUtils.generateMessageID());
        metadata.setDateTimeStamp(Calendar.getInstance());
        metadata.setConversationId(conversationId);
        metadata.addMessageType(URIManagerMessageMetadata.MessageTypesEnum.OBSERVATION);
        metadata.asPlatformMessageMetadata().setSenderPlatformId(new EntityID(platform.getPlatformId()));

        return observationMessage;
    }


    private class DeviceRegistryInitialize implements Callable<Boolean> {

        @Override
        public Boolean call() throws Exception {

            if (isDeviceRegistryInitMessageEnabled) {
                ClassLoader classLoader = MWTestBridge.class.getClassLoader();
                String deviceInitJson = IOUtils.toString(classLoader.getResource("device_init.json"), "UTF-8");

                Message deviceRegistryInitializeMessage = new Message(deviceInitJson);
                MessageMetadata metadata = deviceRegistryInitializeMessage.getMetadata();
                metadata.setConversationId(MessageUtils.generateConversationID());
                metadata.addMessageType(URIManagerMessageMetadata.MessageTypesEnum.DEVICE_REGISTRY_INITIALIZE);
                metadata.asPlatformMessageMetadata().setSenderPlatformId(new EntityID(platform.getPlatformId()));

                logger.debug("Sending DEVICE_REGISTRY_INITIALIZE message = " + deviceRegistryInitializeMessage.serializeToJSONLD());
                publisher.publish(deviceRegistryInitializeMessage);
            }

            return true;
        }
    }

    private class DeviceUpdatesDispatcher implements Runnable {

        @Override
        public void run() {
            try {
                registryInitialized.get();
                for (int i = 0; i < addOrRemoveDevices; i++) {
                    logger.debug("Sending DEVICE_ADD_OR_UPDATE / DEVICE_REMOVE message");

                    Message deviceAddOrUpdate = new Message();
                    MessageMetadata metadata = deviceAddOrUpdate.getMetadata();
                    metadata.setConversationId(MessageUtils.generateConversationID());
                    metadata.addMessageType(URIManagerMessageMetadata.MessageTypesEnum.DEVICE_ADD_OR_UPDATE);
                    metadata.asPlatformMessageMetadata().setSenderPlatformId(new EntityID(platform.getPlatformId()));

                    IoTDevicePayload ioTDevicePayload = new IoTDevicePayload();

                    // Create devices
                    EntityID ioTDeviceID = new EntityID("http://test.inter-iot.eu/device_phy_" + i);
                    ioTDevicePayload.createIoTDevice(ioTDeviceID);
                    ioTDevicePayload.setHasName(ioTDeviceID, "New device name " + i);

                    deviceAddOrUpdate.setPayload(ioTDevicePayload);

                    publisher.publish(deviceAddOrUpdate);
                }
            } catch (InterruptedException | ExecutionException | BrokerException e) {
                e.printStackTrace();
            }
        }
    }

    private class ObservationDispatcher implements Runnable {
        private List<String> deviceIds;
        private String conversationId;

        public ObservationDispatcher(List<String> deviceIds, String conversationId) {
            this.deviceIds = deviceIds;
            this.conversationId = conversationId;
        }

        @Override
        public void run() {
            logger.debug("Sending observation messages for subscription {}...", conversationId);
            int sensorValue = 10;
            ClassLoader classLoader = getClass().getClassLoader();
            String messageString = null;
            try {
                messageString = IOUtils.toString(classLoader.getResource("observation.json"), "UTF-8");
            } catch (IOException e) {
                logger.error("Could not read observation.json", e);
            }
            int i = 0;
            for (String deviceId : deviceIds) {
                Message observationMessage = null;
                if (i % 3 == 0) {
                    try {
                        observationMessage = createObservationMessage(messageString, conversationId, deviceId);
                        logger.debug("Dispatching observation message from json file");
                    } catch (IOException | MessageException e) {
                        logger.error("Exception while parsing observation message.", e);
                        break;
                    }
                } else if (i % 3 == 1) {
                    observationMessage = createComplexObservationMessage(conversationId, deviceId, String.valueOf(sensorValue));
                    logger.debug("Dispatching complex observation message");
                } else {
                    observationMessage = createSimpleObservationMessage(conversationId, deviceId, sensorValue);
                    logger.debug("Dispatching simple observation message");
                }
                i++;

                try {
                    logger.debug("Dispatching observation message: " + observationMessage.serializeToJSONLD());
                    publisher.publish(observationMessage);
                    logger.debug("Observation for device {} has been dispatched.", deviceId);
                } catch (BrokerException | IOException e) {
                    logger.error("Failed to dispatch observation message.", e);
                }
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
