package eu.interiot.intermw.comm.prm;

import eu.interiot.intermw.bridge.BridgeContext;
import eu.interiot.intermw.comm.broker.Publisher;
import eu.interiot.intermw.comm.control.abstracts.AbstractControlComponent;
import eu.interiot.intermw.commons.exceptions.ErrorCode;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.interfaces.Configuration;
import eu.interiot.intermw.commons.model.IoTDevice;
import eu.interiot.intermw.commons.model.Location;
import eu.interiot.intermw.commons.model.Platform;
import eu.interiot.intermw.commons.model.enums.BrokerTopics;
import eu.interiot.intermw.commons.requests.*;
import eu.interiot.intermw.commons.responses.DeviceAddOrUpdateRes;
import eu.interiot.intermw.commons.responses.DeviceRegistryInitializeRes;
import eu.interiot.intermw.commons.responses.DeviceRemoveRes;
import eu.interiot.intermw.services.registry.ParliamentRegistry;
import eu.interiot.intermw.services.registry.ParliamentRegistryExperimental;
import eu.interiot.intermw.services.registry.QueryBuilder;
import eu.interiot.message.ID.EntityID;
import eu.interiot.message.Message;
import eu.interiot.message.MessageMetadata;
import eu.interiot.message.MessagePayload;
import eu.interiot.message.managers.URI.URIManagerINTERMW;
import eu.interiot.message.managers.URI.URIManagerMessageMetadata.MessageTypesEnum;
import eu.interiot.message.utils.MessageUtils;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static eu.interiot.message.managers.URI.URIManagerMessageMetadata.MessageTypesEnum.*;

/*
 * The Platform Request Manager prepares and sends requests to specific platforms through bridges, using already established
 * 	permanent data streams, which it creates during startup with the help of Data Flow Manager, or it creates new data streams.
 * All data streams that go south, from the Platform Request Manager to bridges, go through permanent data streams,
 * 	which can be either routed through IPSM (when needing ontological and/or semantical translation, as decided by consulting
 * 	Platform Registry & Capabilities), or bypassing it and connecting directly to the bridges (thus eliminating the overhead).
 * All data streams that go north, from the bridges to the Platform Request Manager, need to be created as needed.
 * Platform Request Manager is during request pre-processing potentially assisted by some middleware services,
 * 	such as routing or the device registry. It sends requests to underlying platforms as/when needed.
 */

/**
 * Created by flavio_fuart on 19-Dec-16. Edited by Matevz Markovic
 * (matevz.markovic@xlab.si)
 */
//@eu.interiot.intermw.comm.prm.annotations.PlatformRequestManager
public class DefaultPlatformRequestManager extends AbstractControlComponent implements PlatformRequestManager {

    private final static Logger logger = LoggerFactory.getLogger(DefaultPlatformRequestManager.class);
    private Publisher<Message> publisherIPSMRM;
    private Publisher<Message> publisherARM;
    private ParliamentRegistry registry;

    /**
     * @param configuration The configuration for this platform request manager
     */
    public DefaultPlatformRequestManager(Configuration configuration) throws MiddlewareException {
        super();
        logger.debug("DefaultPlatformRequestManager is initializing...");
        registry = initParliamentRegistry(configuration);
        publisherIPSMRM = getPublisher(BrokerTopics.PRM_IPSMRM.getTopicName(), Message.class);
        publisherARM = getPublisher(BrokerTopics.PRM_ARM.getTopicName(), Message.class);
        setUpListeners();
        restoreState();
        logger.debug("DefaultPlatformRequestManager has been initialized successfully.");
    }

    ParliamentRegistry initParliamentRegistry(Configuration configuration) {
        return new ParliamentRegistryExperimental(configuration);
    }

    private void setUpListeners() throws MiddlewareException {
        logger.debug("Setting up PRM listeners...");

        subscribe(BrokerTopics.IPSMRM_PRM.getTopicName(), message -> {
            MessageMetadata metadata = message.getMetadata();
            logger.debug("Received message from the queue {} of type {} with id {} and conversationId {}.",
                    BrokerTopics.IPSMRM_PRM.getTopicName(), metadata.getMessageTypes(), metadata.getMessageID().get(),
                    metadata.getConversationId().get());

            try {
                handleFromIPSMRM(message);

            } catch (Exception e) {
                String description = String.format("PRM failed to handle upstream message %s of type %s received from IPSMRM.",
                        metadata.getMessageID().orElse("N/A"), metadata.getMessageTypes());
                logger.error(description, e);
                getErrorReporter().sendErrorResponseMessage(message, e, description,
                        ErrorCode.ERROR_HANDLING_RECEIVED_MESSAGE, publisherARM);
            }
        }, Message.class);

        subscribe(BrokerTopics.ARM_PRM.getTopicName(), message -> {
            MessageMetadata metadata = message.getMetadata();
            logger.debug("Received message from the queue {} of type {} with id {} and conversationId {}.",
                    BrokerTopics.ARM_PRM.getTopicName(), metadata.getMessageTypes(), metadata.getMessageID().get(),
                    metadata.getConversationId().get());

            try {
                handleFromARM(message);

            } catch (Exception e) {
                String description = String.format("PRM failed to handle downstream message %s of type %s received from ARM.",
                        metadata.getMessageID().orElse("N/A"), metadata.getMessageTypes());
                logger.error(description, e);
                getErrorReporter().sendErrorResponseMessage(message, e, description,
                        ErrorCode.ERROR_HANDLING_RECEIVED_MESSAGE, publisherARM);
            }
        }, Message.class);

        logger.debug("Listeners have been set up successfully.");
    }

    /**
     * Handle message coming from ARM going downstream
     *
     * @param message Action to be performed
     * @return If action is subscription, unique flow id is created for the
     * execution of this action
     * @throws ???
     */
    public void handleFromARM(Message message) throws MiddlewareException {
        MessageMetadata metadata = message.getMetadata();
        String conversationId = metadata.getConversationId().orElse(null);
        Set<MessageTypesEnum> messageTypes = metadata.getMessageTypes();

        logger.debug("Processing downstream message coming from ARM with ID {} and conversationId {} of type {}...",
                metadata.getMessageID().get(), conversationId, messageTypes);

        if (messageTypes.contains(MessageTypesEnum.PLATFORM_REGISTER)) {

            RegisterPlatformReq registerPlatformReq = new RegisterPlatformReq(message);
            registerPlatform(registerPlatformReq);
            publisherIPSMRM.publish(message);

        } else if (messageTypes.contains(MessageTypesEnum.PLATFORM_UPDATE)) {

            UpdatePlatformReq updatePlatformReq = new UpdatePlatformReq(message);
            updatePlatform(updatePlatformReq);
            publisherIPSMRM.publish(message);

        } else if (messageTypes.contains(MessageTypesEnum.PLATFORM_UNREGISTER)) {

            publisherIPSMRM.publish(message);

        } else if (messageTypes.contains(MessageTypesEnum.PLATFORM_CREATE_DEVICE)) {

            PlatformCreateDeviceReq req = new PlatformCreateDeviceReq(message);
            platformCreateDevice(req);
            publisherIPSMRM.publish(message);

        } else if (messageTypes.contains(MessageTypesEnum.PLATFORM_UPDATE_DEVICE)) {

            PlatformUpdateDeviceReq req = new PlatformUpdateDeviceReq(message);
            platformUpdateDevice(req);
            publisherIPSMRM.publish(message);

        } else if (messageTypes.contains(MessageTypesEnum.PLATFORM_DELETE_DEVICE)) {

            PlatformDeleteDeviceReq req = new PlatformDeleteDeviceReq(message);
            platformDeleteDevice(req);
            publisherIPSMRM.publish(message);

        } else if (messageTypes.contains(MessageTypesEnum.SUBSCRIBE)) {

            publisherIPSMRM.publish(message);

        } else if (messageTypes.contains(MessageTypesEnum.UNSUBSCRIBE)) {

            publisherIPSMRM.publish(message);

        } else if (messageTypes.contains(MessageTypesEnum.OBSERVATION)) {

            publisherIPSMRM.publish(message);

        } else if (messageTypes.contains(MessageTypesEnum.LIST_DEVICES)) {

            publisherIPSMRM.publish(message);

        } else if (messageTypes.contains(MessageTypesEnum.ACTUATION)) {

            publisherIPSMRM.publish(message);

        } else if (messageTypes.contains(MessageTypesEnum.LIST_SUPPORTED_PLATFORM_TYPES)) {

            Message responseMsg = listPlatformTypes(message);
            publisherARM.publish(responseMsg);

        } else if (messageTypes.contains(QUERY)) {

            publisherIPSMRM.publish(message);

        } else {
            throw new MiddlewareException("Unexpected message type: " + messageTypes);
        }

        logger.debug("Message {} has been processed successfully.",
                metadata.getMessageID().get(), conversationId, messageTypes);
    }

    private void registerPlatform(RegisterPlatformReq registerPlatformReq) throws MiddlewareException {
        Platform platform = registerPlatformReq.getPlatform();
        Location location = registerPlatformReq.getLocation();
        try {
            logger.debug("Creating bridge for the platform {}...", platform.getPlatformId());
            BridgeContext.createBridge(platform);
            getRegistry().registerPlatform(platform);

            if (location != null) {
                getRegistry().registerLocation(location);
            }
        } catch (Exception e) {
            throw new MiddlewareException(String.format("Failed to create bridge for the platform %s: %s",
                    platform.getPlatformId(), e.getMessage()), e);
        }
    }

    private void restorePlatform(Platform platform) throws MiddlewareException {
        try {
            logger.debug("Creating bridge for the platform {}...", platform.getPlatformId());
            BridgeContext.createBridge(platform);
            RegisterPlatformReq req = new RegisterPlatformReq(platform);
            Message message = req.toMessage();
            message.getMetadata().addMessageType(MessageTypesEnum.SYS_INIT);
            publisherIPSMRM.publish(message);

        } catch (Exception e) {
            throw new MiddlewareException(String.format("Failed to create bridge for the platform %s: %s",
                    platform.getPlatformId(), e.getMessage()), e);
        }
    }

    private void updatePlatform(UpdatePlatformReq updatePlatformReq) throws MiddlewareException {
        Platform platform = updatePlatformReq.getPlatform();
        try {
            getRegistry().updatePlatform(platform);

        } catch (Exception e) {
            throw new MiddlewareException(String.format("Failed to update platform %s: %s",
                    platform.getPlatformId(), e.getMessage()), e);
        }
    }

    private void unregisterPlatform(String platformId) throws MiddlewareException {
        try {
            logger.debug("Removing bridge for the platform {}...", platformId);
            BridgeContext.removeBridge(platformId);
            getRegistry().removePlatform(platformId);

        } catch (Exception e) {
            throw new MiddlewareException(String.format("Failed to remove bridge for the platform %s: %s",
                    platformId, e.getMessage()), e);
        }
    }

    private void platformCreateDevice(PlatformCreateDeviceReq req) throws MiddlewareException {
        getRegistry().registerDevices(req.getDevices());
    }

    private void platformUpdateDevice(PlatformUpdateDeviceReq req) throws MiddlewareException {
        getRegistry().updateDevices(req.getDevices());
    }

    private void platformDeleteDevice(PlatformDeleteDeviceReq req) throws MiddlewareException {
        getRegistry().removeDevices(req.getDeviceIds());
    }

    private Message listPlatformTypes(Message message) throws MiddlewareException {
        try {
            MessagePayload payload = new MessagePayload();

            Collection<String> supportedPlatformTypes = BridgeContext.getSupportedPlatformTypes();
            logger.debug("Supported platform types: {}", supportedPlatformTypes);
            EntityID entityID = new EntityID(URIManagerINTERMW.PREFIX.intermw + "platform-types");
            for (String platformType : supportedPlatformTypes) {
                payload.addTypeToEntity(entityID, new EntityID(platformType));
            }

            Message responseMessage = MessageUtils.createResponseMessage(message);
            responseMessage.setPayload(payload);
            return responseMessage;

        } catch (Exception e) {
            throw new MiddlewareException("Failed to list platform types: " + e.getMessage(), e);
        }
    }

    /**
     * Push IPSM-translated message upstream towards ARM or towards MW2MW (Resource discovery)
     * <p>
     * Based on sequence diagrams: MW02, MW03, MW04, MW08
     *
     * @param message represents IIOTHS{M1,Og}, where M is data and O is a ontology
     * @throws ???
     */
    public void handleFromIPSMRM(Message message) throws MiddlewareException {
        MessageMetadata metadata = message.getMetadata();
        String conversationId = metadata.getConversationId().orElse(null);
        Set<MessageTypesEnum> messageTypes = metadata.getMessageTypes();

        logger.debug("Processing message coming from IPSMRM going upstream with ID {} and conversationId {} of type {}...",
                metadata.getMessageID().get(), conversationId, messageTypes);

        if (messageTypes.contains(MessageTypesEnum.PLATFORM_UNREGISTER)) {
            String platformId = message.getMetadata().asPlatformMessageMetadata().getSenderPlatformId().get().toString();
            unregisterPlatform(platformId);
        } else if (messageTypes.contains(DEVICE_REGISTRY_INITIALIZE)) {
            registerDevices(new DeviceRegistryInitializeRes(message));
            return;
        } else if (messageTypes.contains(DEVICE_ADD_OR_UPDATE)) {
            addOrUpdateDevice(new DeviceAddOrUpdateRes(message));
            return;
        } else if (messageTypes.contains(DEVICE_REMOVE)) {
            removeDevice(new DeviceRemoveRes(message));
            return;
        } else if (messageTypes.contains(LIST_DEVICES)) {
            return;
        }

        publisherARM.publish(message);
    }

    private void removeDevice(DeviceRemoveRes response) throws MiddlewareException {
        List<String> deviceIds = Collections.singletonList(response.getDevice().getDeviceId());
        logger.debug("DeviceDiscovery - Removing " + deviceIds + " devices");
        getRegistry().removeDevices(deviceIds);
    }

    private void addOrUpdateDevice(DeviceAddOrUpdateRes response) throws MiddlewareException {
        List<IoTDevice> iotDeviceList = Collections.singletonList(response.getDevice());
        logger.debug("DeviceDiscovery - Updating " + iotDeviceList.size() + " devices");
        getRegistry().updateDevices(iotDeviceList);
    }

    private void registerDevices(DeviceRegistryInitializeRes response) throws MiddlewareException {
        List<IoTDevice> iotDeviceList = response.getIoTDevices();
        logger.debug("DeviceDiscovery - Registering " + iotDeviceList.size() + " devices");

        List<UpdateRequest> collect = iotDeviceList.stream().map(ioTDevice -> {
            try {
                return QueryBuilder.IoTDevice.update(ioTDevice).asUpdate();
            } catch (MiddlewareException e) {
                logger.error("Failed constructing UpdateRequest from IoTDevice", e);
                return null;
            }
        }).collect(Collectors.toList());

        getRegistry().registerDevicesWithQueryBuilder(collect);
    }

    private void restoreState() throws MiddlewareException {
        logger.debug("Restoring PRM state...");
        List<Platform> platforms = getRegistry().listPlatforms();
        if (platforms.isEmpty()) {
            logger.debug("No platforms registered.");
        }
        for (Platform platform : platforms) {
            logger.debug("Restoring bridge for the platform {}...", platform.getPlatformId());
            try {
                restorePlatform(platform);

            } catch (Exception e) {
                throw new MiddlewareException(String.format("Failed to restore bridge for the platform %s: %s",
                        platform.getPlatformId(), e.getMessage()), e);
            }
        }
        logger.debug("PRM state has been restored successfully.");
    }

    public ParliamentRegistry getRegistry() {
        return registry;
    }
}
