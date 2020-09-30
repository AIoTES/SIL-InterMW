/**
 * INTER-IoT. Interoperability of IoT Platforms.
 * INTER-IoT is a R&D project which has received funding from the European
 * Union�s Horizon 2020 research and innovation programme under grant
 * agreement No 687283.
 * <p>
 * Copyright (C) 2016-2018, by (Author's company of this file):
 * - Prodevelop S.L.
 * <p>
 * <p>
 * For more information, contact:
 * - @author <a href="mailto:mllorente@prodevelop.es">Miguel A. Llorente</a>
 * - Project coordinator:  <a href="mailto:coordinator@inter-iot.eu"></a>
 * <p>
 * <p>
 * This code is licensed under the EPL license, available at the root
 * application directory.
 */
package eu.interiot.intermw.api;

import eu.interiot.intermw.api.exception.BadRequestException;
import eu.interiot.intermw.api.exception.ConflictException;
import eu.interiot.intermw.api.exception.NotFoundException;
import eu.interiot.intermw.api.model.ActuationInput;
import eu.interiot.intermw.api.model.UpdateClientInput;
import eu.interiot.intermw.api.model.UpdatePlatformInput;
import eu.interiot.intermw.comm.arm.ARMContext;
import eu.interiot.intermw.comm.arm.ApiRequestManager;
import eu.interiot.intermw.comm.arm.HttpPushApiCallback;
import eu.interiot.intermw.comm.arm.RabbitMQApiCallback;
import eu.interiot.intermw.comm.broker.rabbitmq.QueueImpl;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.interfaces.ApiCallback;
import eu.interiot.intermw.commons.interfaces.Configuration;
import eu.interiot.intermw.commons.model.*;
import eu.interiot.intermw.commons.model.enums.IoTDeviceType;
import eu.interiot.intermw.commons.requests.*;
import eu.interiot.intermw.services.registry.ParliamentRegistry;
import eu.interiot.intermw.services.registry.ParliamentRegistryExperimental;
import eu.interiot.intermw.services.registry.QueryBuilder;
import eu.interiot.message.Message;
import eu.interiot.message.utils.MessageUtils;
import org.apache.jena.query.ParameterizedSparqlString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * FIXME add javadoc
 *
 * @author aromeu
 */
public class InterMwApiImpl implements InterMwApi {
    private static final Logger logger = LoggerFactory.getLogger(InterMwApiImpl.class);
    private Configuration configuration;
    private static ApiRequestManager apiRequestManager;
    private static QueueImpl queue;
    private ParliamentRegistry registry;

    public InterMwApiImpl(Configuration configuration) throws MiddlewareException {
        this.configuration = configuration;
        InterMWInitializer.initialize();
        apiRequestManager = ARMContext.getApiRequestManager();
        queue = new QueueImpl();
        registry = new ParliamentRegistryExperimental(configuration);
    }

    @Override
    public Client getClient(String clientId) throws MiddlewareException {
        return getRegistry().getClientById(clientId);
    }

    @Override
    public List<Client> listClients() throws MiddlewareException {
        return getRegistry().listClients();
    }

    @Override
    public void registerClient(Client client) throws MiddlewareException, ConflictException, BadRequestException {
        registerClient(client, null);
    }

    @Override
    public void registerClient(Client client, ApiCallback<Message> apiCallback) throws MiddlewareException, ConflictException, BadRequestException {
        String clientId = client.getClientId();
        if (getRegistry().isClientRegistered(clientId)) {
            throw new ConflictException(String.format("Client '%s' is already registered.", clientId));
        }

        if (client.getResponseDelivery() == null && apiCallback == null ||
                client.getResponseDelivery() != null && apiCallback != null) {
            throw new BadRequestException("Either response delivery or apiCallback must be given.");
        }

        if (client.getResponseDelivery() == Client.ResponseDelivery.SERVER_PUSH) {
            if (client.getCallbackUrl() == null) {
                throw new BadRequestException("CallbackUrl attribute must be specified when using SERVER_PUSH response delivery.");
            }
        }

        if (client.getReceivingCapacity() == null) {
            client.setReceivingCapacity(configuration.getClientReceivingCapacityDefault());
        }

        if (client.getReceivingCapacity() < 1) {
            throw new BadRequestException("Invalid receivingCapacity: must be greater than or equal to 1.");
        }

        try {
            logger.debug("Registering client {}...", client.getClientId());

            if (apiCallback == null) {
                switch (client.getResponseDelivery()) {
                    case CLIENT_PULL:
                        apiCallback = new RabbitMQApiCallback(clientId, queue);
                        break;
                    case SERVER_PUSH:
                        apiCallback = new HttpPushApiCallback(client, queue, configuration);
                        break;
                    default:
                        throw new BadRequestException("Response delivery " + client.getResponseDelivery() + " is not supported.");
                }
            }

            getRegistry().registerClient(client);
            apiRequestManager.registerCallback(client.getClientId(), apiCallback);
            logger.debug("Client {} has been registered successfully.", client.getClientId());

        } catch (Exception e) {
            throw new MiddlewareException("Failed to register client: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateClient(String clientId, UpdateClientInput input) throws MiddlewareException, NotFoundException, BadRequestException {
        Client client = getRegistry().getClientById(clientId);
        if (client == null) {
            throw new NotFoundException(String.format("Client '%s' does not exist.", clientId));
        }

        if (input.getCallbackUrl() != null) {
            try {
                client.setCallbackUrl(new URL(input.getCallbackUrl()));
            } catch (MalformedURLException e) {
                throw new BadRequestException("Invalid callbackURL: " + input.getCallbackUrl());
            }
        }
        if (input.getReceivingCapacity() != null) {
            client.setReceivingCapacity(input.getReceivingCapacity());
        }
        if (input.getResponseDelivery() != null) {
            client.setResponseDelivery(input.getResponseDelivery());
        }
        if (input.getResponseFormat() != null) {
            client.setResponseFormat(input.getResponseFormat());
        }

        getRegistry().updateClient(client);

        apiRequestManager.updateCallback(client);
    }

    @Override
    public void removeClient(String clientId) throws MiddlewareException, NotFoundException {
        if (!getRegistry().isClientRegistered(clientId)) {
            throw new NotFoundException(String.format("Client '%s' does not exist.", clientId));
        }

        try {
            logger.debug("Removing client {}...", clientId);
            getRegistry().removeClient(clientId);
            apiRequestManager.unregisterCallback(clientId);
            logger.debug("Client {} has been removed successfully.", clientId);
        } catch (Exception e) {
            throw new MiddlewareException("Failed to remove client: " + e.getMessage(), e);
        }
    }

    @Override
    public Message retrieveResponseMessage(String clientId, long timeoutMillis) throws MiddlewareException {
        try {
            return queue.consumeMessage(clientId, timeoutMillis);
        } catch (Exception e) {
            throw new MiddlewareException("Failed to retrieve response messages: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Message> retrieveResponseMessages(String clientId) throws MiddlewareException {
        Client client = getRegistry().getClientById(clientId);
        try {
            List<String> messageStrings = queue.consumeMessages(clientId, client.getReceivingCapacity());
            List<Message> messages = new ArrayList<>();
            for (String messageString : messageStrings) {
                messages.add(new Message(messageString));
            }
            return messages;

        } catch (Exception e) {
            throw new MiddlewareException("Failed to retrieve response messages: " + e.getMessage(), e);
        }
    }

    @Override
    public String listPlatformTypes(String clientId) throws MiddlewareException {
        ListPlatformTypesReq req = new ListPlatformTypesReq();
        req.setClientId(clientId);

        return sendToARM(req.toMessage());
    }

    @Override
    public List<Platform> listPlatforms() throws MiddlewareException {
        List<Location> allLocations = getRegistry().getAllLocations();
        Map<String, Location> locationMap = allLocations.stream().collect(Collectors.toMap(Location::getLocationId, location -> location));

        return getRegistry().listPlatforms().stream().map(platform -> {
            Location location = locationMap.get(platform.getLocationId());
            if (location != null) {
                platform.setLocation(location);
            }
            return platform;
        }).collect(Collectors.toList());
    }

    @Override
    public String registerPlatform(Platform platform, Location location) throws MiddlewareException, ConflictException, BadRequestException {
        if (getRegistry().getPlatformById(platform.getPlatformId()) != null) {
            throw new ConflictException("Platform " + platform.getPlatformId() + " is already registered.");
        }

        Location existingLocation = location != null ? getRegistry().getLocation(location.getLocationId()) : null;
        if (existingLocation != null) {
            if (!existingLocation.equals(location)) {
                throw new ConflictException("Location " + location.getLocationId() + " already exists and differs from provided location");
            }
        }

        validateAlignmentsData(platform);

        List<Message> messageList = new ArrayList<>();
        RegisterPlatformReq registerPlatformReq = new RegisterPlatformReq(platform, location);
        try {
            messageList.add(registerPlatformReq.toMessage());

            // TODO use task executor schedule
            ListDevicesReq listDevicesReq = new ListDevicesReq(platform.getClientId(), platform.getPlatformId());
            messageList.add(listDevicesReq.toMessage());
        } catch (Exception e) {
            throw new BadRequestException("Failed to convert request to a message: " + e.getMessage());
        }

        return sendToARM(messageList).get(0);
    }

    @Override
    public Platform getPlatform(String platformId) throws MiddlewareException {
        Platform platform = getRegistry().getPlatformById(platformId);
        Location location = getRegistry().getLocation(platform.getLocationId());
        platform.setLocation(location);
        return platform;
    }

    @Override
    public String updatePlatform(Platform platform) throws MiddlewareException, BadRequestException, NotFoundException {
        Platform existingPlatform = getRegistry().getPlatformById(platform.getPlatformId());
        if (existingPlatform == null) {
            throw new NotFoundException("Platform with ID " + platform.getPlatformId() + " cannot be found.");
        }
        if (platform.getType() == null) {
            platform.setType(existingPlatform.getType());

        } else if (!existingPlatform.getType().equals(platform.getType())) {
            throw new BadRequestException("Platform type cannot be changed.");
        }
        platform.setTimeCreated(existingPlatform.getTimeCreated());

        validateAlignmentsData(platform);

        UpdatePlatformReq req = new UpdatePlatformReq(platform);

        Message message;
        try {
            message = req.toMessage();
        } catch (Exception e) {
            throw new BadRequestException("Failed to convert request to a message: " + e.getMessage());
        }
        return sendToARM(message);
    }

    @Override
    public String updatePlatform(String clientId, String platformId, UpdatePlatformInput input) throws NotFoundException, MiddlewareException, BadRequestException {
        Platform platform = getRegistry().getPlatformById(platformId);
        if (platform == null) {
            throw new NotFoundException("Platform with ID " + platformId + " does not exist.");
        }

        platform.setClientId(clientId);

        if (input.getBaseEndpoint() != null) {
            try {
                platform.setBaseEndpoint(new URL(input.getBaseEndpoint()));
            } catch (MalformedURLException e) {
                throw new BadRequestException("Invalid baseEndpoint: " + input.getBaseEndpoint());
            }
        }
        if (input.getLocation() != null) {
            platform.setLocationId(input.getLocation());
        }
        if (input.getName() != null) {
            platform.setName(input.getName());
        }
        if (input.getUsername() != null) {
            platform.setUsername(input.getUsername());
        }
        if (input.getEncryptedPassword() != null) {
            platform.setEncryptedPassword(input.getEncryptedPassword());
        }
        if (input.getEncryptionAlgorithm() != null) {
            platform.setEncryptionAlgorithm(input.getEncryptionAlgorithm());
        }
        if (input.getDownstreamInputAlignmentName() != null) {
            platform.setDownstreamInputAlignmentName(input.getDownstreamInputAlignmentName());
        }
        if (input.getDownstreamInputAlignmentVersion() != null) {
            platform.setDownstreamInputAlignmentVersion(input.getDownstreamInputAlignmentVersion());
        }
        if (input.getDownstreamOutputAlignmentName() != null) {
            platform.setDownstreamOutputAlignmentName(input.getDownstreamOutputAlignmentName());
        }
        if (input.getDownstreamOutputAlignmentVersion() != null) {
            platform.setDownstreamOutputAlignmentVersion(input.getDownstreamOutputAlignmentVersion());
        }
        if (input.getUpstreamInputAlignmentName() != null) {
            platform.setUpstreamInputAlignmentName(input.getUpstreamInputAlignmentName());
        }
        if (input.getUpstreamInputAlignmentVersion() != null) {
            platform.setUpstreamInputAlignmentVersion(input.getUpstreamInputAlignmentVersion());
        }
        if (input.getUpstreamOutputAlignmentName() != null) {
            platform.setUpstreamOutputAlignmentName(input.getUpstreamOutputAlignmentName());
        }
        if (input.getUpstreamOutputAlignmentVersion() != null) {
            platform.setUpstreamOutputAlignmentVersion(input.getUpstreamOutputAlignmentVersion());
        }

        validateAlignmentsData(platform);

        UpdatePlatformReq req = new UpdatePlatformReq(platform);

        Message message;
        try {
            message = req.toMessage();
        } catch (Exception e) {
            throw new BadRequestException("Failed to convert request to a message: " + e.getMessage());
        }
        return sendToARM(message);
    }

    @Override
    public String removePlatform(String clientId, String platformId) throws MiddlewareException, NotFoundException, BadRequestException {
        Platform platform = getRegistry().getPlatformById(platformId);
        if (platform == null) {
            throw new NotFoundException("Platform with ID " + platformId + " doesn't exist.");
        }

        UnregisterPlatformReq req = new UnregisterPlatformReq(clientId, platformId);
        Message message;
        try {
            message = req.toMessage();
        } catch (Exception e) {
            throw new BadRequestException("Failed to convert request to a message: " + e.getMessage());
        }

        return sendToARM(message);
    }

    @Override
    public String platformCreateDevices(String clientId, List<IoTDevice> devices) throws MiddlewareException, BadRequestException, ConflictException {
        if (devices.isEmpty()) {
            throw new BadRequestException("At least one device must be specified.");
        }

        // check if any of specified devices is already registered
        List<String> deviceIds = new ArrayList<>();
        for (IoTDevice device : devices) {
            deviceIds.add(device.getDeviceId());
        }
        List<IoTDevice> alreadyRegisteredDevices = getRegistry().getDevices(deviceIds);
        if (!alreadyRegisteredDevices.isEmpty()) {
            List<String> alreadyRegisteredDeviceIds = new ArrayList<>();
            for (IoTDevice device : alreadyRegisteredDevices) {
                alreadyRegisteredDeviceIds.add(device.getDeviceId());
            }
            throw new ConflictException("Following devices are already registered: " + alreadyRegisteredDeviceIds);
        }

        Map<String, List<IoTDevice>> devicesPerPlatform = new HashMap<>();
        for (IoTDevice device : devices) {
            if (!devicesPerPlatform.containsKey(device.getHostedBy())) {
                devicesPerPlatform.put(device.getHostedBy(), new ArrayList<>());
            }
            devicesPerPlatform.get(device.getHostedBy()).add(device);
        }

        String conversationId = MessageUtils.generateConversationID();

        List<Message> messages = new ArrayList<>();
        for (String platformId : devicesPerPlatform.keySet()) {
            PlatformCreateDeviceReq req = new PlatformCreateDeviceReq(platformId, devicesPerPlatform.get(platformId), clientId);
            Message message;
            try {
                message = req.toMessage(conversationId);
            } catch (Exception e) {
                throw new BadRequestException("Failed to convert request to a message: " + e.getMessage());
            }

            messages.add(message);
        }

        for (Message message : messages) {
            sendToARM(message);
        }

        return conversationId;
    }


    @Override
    public String platformUpdateDevice(String clientId, IoTDevice device) throws MiddlewareException, BadRequestException {
        return platformUpdateDevices(clientId, Collections.singletonList(device));
    }

    @Override
    public String platformUpdateDevices(String clientId, List<IoTDevice> devices) throws MiddlewareException, BadRequestException {
        if (devices.isEmpty()) {
            throw new BadRequestException("At least one device must be specified.");
        }

        List<String> deviceIds = new ArrayList<>();
        for (IoTDevice device : devices) {
            deviceIds.add(device.getDeviceId());
        }

        List<IoTDevice> existingDevices = getRegistry().getDevices(deviceIds);
        if (existingDevices.size() < devices.size()) {
            throw new BadRequestException("One or more devices are not registered within INTER-MW.");
        }

        Map<String, List<IoTDevice>> devicesPerPlatform = new HashMap<>();
        for (IoTDevice device : devices) {
            if (!devicesPerPlatform.containsKey(device.getHostedBy())) {
                devicesPerPlatform.put(device.getHostedBy(), new ArrayList<>());
            }
            devicesPerPlatform.get(device.getHostedBy()).add(device);
        }

        String conversationId = MessageUtils.generateConversationID();

        for (String platformId : devicesPerPlatform.keySet()) {
            PlatformUpdateDeviceReq req = new PlatformUpdateDeviceReq(platformId, devicesPerPlatform.get(platformId), clientId);
            sendToARM(req.toMessage(conversationId));
        }

        return conversationId;
    }

    @Override
    public String platformDeleteDevices(String clientId, List<String> deviceIds) throws MiddlewareException, BadRequestException {
        if (deviceIds.isEmpty()) {
            throw new BadRequestException("At least one device must be specified.");
        }

        // check if all devices are registered
        List<IoTDevice> devices = getRegistry().getDevices(deviceIds);
        if (devices.size() < deviceIds.size()) {
            throw new BadRequestException("One or more devices are not registered within INTER-MW.");
        }

        String conversationId = MessageUtils.generateConversationID();

        Map<String, List<String>> devicesPerPlatform = new HashMap<>();
        for (IoTDevice device : devices) {
            if (!devicesPerPlatform.containsKey(device.getHostedBy())) {
                devicesPerPlatform.put(device.getHostedBy(), new ArrayList<>());
            }
            devicesPerPlatform.get(device.getHostedBy()).add(device.getDeviceId());
        }

        for (String platformId : devicesPerPlatform.keySet()) {
            PlatformDeleteDeviceReq req = new PlatformDeleteDeviceReq(platformId, devicesPerPlatform.get(platformId), clientId);
            sendToARM(req.toMessage(conversationId));
        }

        return conversationId;
    }

    @Override
    public List<IoTDevice> listDevices(String clientId, String platformId) throws MiddlewareException {
        IoTDevice deviceFilter = new IoTDevice();
        deviceFilter.setHostedBy(platformId);

        ParameterizedSparqlString listPlatformDevicesQuery = QueryBuilder.IoTDevice.get(deviceFilter);
        return getRegistry().getDevicesWithQueryBuilder(listPlatformDevicesQuery);
    }

    @Override
    public String syncDevices(String clientId, String platformId) throws MiddlewareException {
        String conversationId = MessageUtils.generateConversationID();
        SyncDevicesReq req = new SyncDevicesReq(platformId);
        req.setClientId(clientId);
        sendToARM(req.toMessage(conversationId));

        return conversationId;
    }

    @Override
    public String subscribe(String clientId, List<String> deviceIds) throws MiddlewareException, BadRequestException {
        List<IoTDevice> devices = getRegistry().getDevices(deviceIds);
        if (devices.size() == 0) {
            throw new BadRequestException("Specified devices are not registered with Inter MW.");
        } else if (devices.size() < deviceIds.size()) {
            throw new BadRequestException("Not all devices are registered with Inter MW.");
        }

        String conversationId = MessageUtils.generateConversationID();
        String subscriptionId = conversationId;

        Subscription subscription = new Subscription();
        subscription.setConversationId(subscriptionId);
        subscription.setClientId(clientId);
        subscription.setDeviceIds(deviceIds);

        getRegistry().subscribe(subscription);

        Map<String, List<IoTDevice>> devicesPerPlatform = new HashMap<>();
        for (IoTDevice device : devices) {
            if (!devicesPerPlatform.containsKey(device.getHostedBy())) {
                devicesPerPlatform.put(device.getHostedBy(), new ArrayList<>());
            }
            devicesPerPlatform.get(device.getHostedBy()).add(device);
        }

        List<Message> messages = new ArrayList<>();
        for (String platformId : devicesPerPlatform.keySet()) {
            SubscribeReq req = new SubscribeReq(subscriptionId, clientId,
                    platformId, devicesPerPlatform.get(platformId));
            Message message;
            try {
                message = req.toMessage(conversationId);
            } catch (Exception e) {
                throw new BadRequestException("Failed to convert request to a message: " + e.getMessage());
            }

            messages.add(message);
        }

        for (Message message : messages) {
            sendToARM(message);
        }

        return conversationId;
    }

    @Override
    public List<Subscription> listSubscriptions() throws MiddlewareException {
        return getRegistry().listSubcriptions();
    }

    @Override
    public List<Subscription> listSubscriptions(String clientId) throws MiddlewareException {
        return getRegistry().listSubcriptions(clientId);
    }

    @Override
    public String unsubscribe(String clientId, String subscriptionId) throws MiddlewareException, NotFoundException, BadRequestException {
        Subscription subscription = getRegistry().findSubscription(clientId, subscriptionId);

        if (subscription == null) {
            throw new NotFoundException("Subscription with ID " + subscriptionId + " doesn't exist.");
        }

        getRegistry().deleteSubscription(subscriptionId);

        List<IoTDevice> devices = getRegistry().getDevices(subscription.getDeviceIds());

        Map<String, List<String>> deviceIdsPerPlatform = new HashMap<>();
        for (IoTDevice device : devices) {
            if (!deviceIdsPerPlatform.containsKey(device.getHostedBy())) {
                deviceIdsPerPlatform.put(device.getHostedBy(), new ArrayList<>());
            }
            deviceIdsPerPlatform.get(device.getHostedBy()).add(device.getDeviceId());
        }

        String conversationId = MessageUtils.generateConversationID();
        List<Message> messages = new ArrayList<>();
        for (String platformId : deviceIdsPerPlatform.keySet()) {
            UnsubscribeReq req = new UnsubscribeReq(subscriptionId, platformId, clientId);
            req.setDeviceIds(deviceIdsPerPlatform.get(platformId));
            Message message;
            try {
                message = req.toMessage(conversationId);
            } catch (Exception e) {
                throw new BadRequestException("Failed to convert request to a message: " + e.getMessage());
            }

            messages.add(message);
        }

        for (Message message : messages) {
            sendToARM(message);
        }

        return conversationId;
    }

    @Override
    public String actuate(String clientId, String deviceId, ActuationInput input) throws MiddlewareException, NotFoundException {
        String conversationId = MessageUtils.generateConversationID();

        List<IoTDevice> devices = getRegistry().getDevices(Collections.singletonList(deviceId));

        if (devices.isEmpty()) {
            throw new NotFoundException(String.format("Device with ID %s does not exist", deviceId));
        }
        IoTDevice actuatorDevice = devices.iterator().next();

        Actuation actuation = new Actuation();
        actuation.setClientId(clientId);
        actuation.setPlatformId(actuatorDevice.getHostedBy());
        // If deviceId hosts actuator, then we use madeByActuator, if the device IS the actuator, then we use deviceId
        actuation.setMadeByActuator(input.getActuatorId());
        actuation.setMadeByActuatorLocalId(input.getActuatorLocalId());
        actuation.setDeviceId(deviceId);
        actuation.setActuationResults(input.getActuationResultSet());

        ActuationReq request = new ActuationReq(actuation);
        Message message = request.toMessage(conversationId);

        sendToARM(message);

        return conversationId;
    }

    @Override
    public Message getAllSensorData(String clientId, String platformId) throws MiddlewareException, ExecutionException, InterruptedException, TimeoutException {
        return getSensorDataForDevices(clientId, platformId, null);
    }

    @Override
    public Message getSensorDataForDevices(String clientId, String platformId, @Nullable List<String> deviceIds) throws MiddlewareException, InterruptedException, ExecutionException, TimeoutException {
        List<IoTDevice> sensors;
        if (deviceIds == null) {
            sensors = getRegistry().getDevicesByType(IoTDeviceType.SENSOR, platformId);
        } else {
            sensors = new ArrayList<>();
            for (String deviceId : deviceIds) {
                sensors.add(new IoTDevice(deviceId));
            }
        }
        return querySensorValues(clientId, platformId, sensors);
    }

    @Override
    public String sendMessage(String clientId, String content) throws BadRequestException, MiddlewareException {
        Message message;
        try {
            message = new Message(content);
        } catch (Exception e) {
            throw new BadRequestException("Invalid JSON-LD message: " + e.getMessage());
        }
        return sendToARM(message);
    }

    private String sendToARM(Message message) throws MiddlewareException {
        return sendToARM(Collections.singletonList(message)).get(0);
    }

    private List<String> sendToARM(List<Message> messages) throws MiddlewareException {
        List<String> conversationIds = new ArrayList<>();
        for (Message message : messages) {
            String conversationId = apiRequestManager.process(message);
            conversationIds.add(conversationId);
        }
        return conversationIds;
    }

    private void validateAlignmentsData(Platform platform) throws BadRequestException {
        if (platform.getDownstreamInputAlignmentName() == null && platform.getDownstreamInputAlignmentVersion() == null &&
                platform.getDownstreamOutputAlignmentName() == null && platform.getDownstreamOutputAlignmentVersion() == null &&
                platform.getUpstreamInputAlignmentName() == null && platform.getUpstreamInputAlignmentVersion() == null &&
                platform.getUpstreamOutputAlignmentName() == null && platform.getUpstreamOutputAlignmentVersion() == null) {

            platform.setDownstreamInputAlignmentName("");
            platform.setDownstreamInputAlignmentVersion("");
            platform.setDownstreamOutputAlignmentName("");
            platform.setDownstreamOutputAlignmentVersion("");

            platform.setUpstreamInputAlignmentName("");
            platform.setUpstreamInputAlignmentVersion("");
            platform.setUpstreamOutputAlignmentName("");
            platform.setUpstreamOutputAlignmentVersion("");
        }

        if (platform.getDownstreamInputAlignmentName() == null || platform.getDownstreamInputAlignmentVersion() == null ||
                platform.getDownstreamOutputAlignmentName() == null || platform.getDownstreamOutputAlignmentVersion() == null ||
                platform.getUpstreamInputAlignmentName() == null || platform.getUpstreamInputAlignmentVersion() == null ||
                platform.getUpstreamOutputAlignmentName() == null || platform.getUpstreamOutputAlignmentVersion() == null) {

            throw new BadRequestException("Alignments are not set correctly: one or more fields are null.");
        }

        checkIfOnlyOneSpecified(platform.getDownstreamInputAlignmentName(), platform.getDownstreamInputAlignmentVersion());
        checkIfOnlyOneSpecified(platform.getDownstreamOutputAlignmentName(), platform.getDownstreamOutputAlignmentVersion());
        checkIfOnlyOneSpecified(platform.getUpstreamInputAlignmentName(), platform.getUpstreamInputAlignmentVersion());
        checkIfOnlyOneSpecified(platform.getUpstreamOutputAlignmentName(), platform.getUpstreamOutputAlignmentVersion());
    }

    private void checkIfOnlyOneSpecified(String a, String b) throws BadRequestException {
        if (a.isEmpty() && !b.isEmpty() ||
                !a.isEmpty() && b.isEmpty()) {
            throw new BadRequestException("Alignment name and version must be specified both or none.");
        }
    }

    private Message querySensorValues(String clientId, String platformId, List<IoTDevice> ioTDevicesFilter) throws MiddlewareException, InterruptedException, ExecutionException, TimeoutException {
        SensorQueryReq request = new SensorQueryReq(ioTDevicesFilter, clientId, platformId);

        Message requestMessage = request.toMessage();
        String conversationIdFromArm = sendToARM(requestMessage);

        QueryMessageFetcher messageFetcher = new QueryMessageFetcher(conversationIdFromArm);
        ExecutorService service = Executors.newSingleThreadExecutor();
        Future<Message> futureMessage = service.submit(messageFetcher);

        return futureMessage.get(configuration.getQueryResponseTimeout(), TimeUnit.SECONDS);
    }

    public ParliamentRegistry getRegistry() {
        return registry;
    }

    private class QueryMessageFetcher implements Callable<Message> {
        private String conversationId;

        QueryMessageFetcher(String conversationId) {
            this.conversationId = conversationId;
        }

        @Override
        public Message call() throws Exception {
            Message message = null;
            while (message == null) {
                message = apiRequestManager.getQueryResponseMessage(conversationId);
                Thread.sleep(500);
            }

            return message;
        }
    }
}
