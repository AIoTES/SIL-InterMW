package eu.interiot.intermw.commons.requests;

import eu.interiot.intermw.commons.exceptions.InvalidMessageException;
import eu.interiot.message.ID.EntityID;
import eu.interiot.message.Message;
import eu.interiot.message.MessageMetadata;
import eu.interiot.message.managers.URI.URIManagerMessageMetadata;
import eu.interiot.message.payload.types.IoTDevicePayload;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PlatformDeleteDeviceReq {

    private String platformId;
    private List<String> deviceIds;
    private String clientId;

    public PlatformDeleteDeviceReq(String platformId, List<String> deviceIds, String clientId) {
        this.platformId = platformId;
        this.deviceIds = deviceIds;
        this.clientId = clientId;
    }

    public PlatformDeleteDeviceReq(Message message) throws InvalidMessageException {
        clientId = message.getMetadata().getClientId().orElse(null);
        platformId = message.getMetadata().asPlatformMessageMetadata().getReceivingPlatformIDs().iterator().next().toString();
        IoTDevicePayload ioTDevicePayload = message.getPayloadAsGOIoTPPayload().asIoTDevicePayload();
        Set<EntityID> deviceEntityIds = ioTDevicePayload.getIoTDevices();
        deviceIds = new ArrayList<>();
        for (EntityID deviceEntityId : deviceEntityIds) {
            String deviceId = deviceEntityId.toString();
            if (!ioTDevicePayload.getIsHostedBy(deviceEntityId).isPresent()) {
                throw new InvalidMessageException("Invalid PLATFORM_UPDATE_DEVICE message: HostedBy attribute is required for all devices.");
            }
            deviceIds.add(deviceId);

        }
    }

    public Message toMessage(String conversationId) {
        IoTDevicePayload ioTDevicePayload = new IoTDevicePayload();

        for (String device : deviceIds) {
            EntityID entityID = new EntityID(device);
            ioTDevicePayload.createIoTDevice(entityID);
            ioTDevicePayload.setIsHostedBy(entityID, new EntityID(platformId));
        }

        Message message = new Message();
        MessageMetadata metadata = message.getMetadata();
        metadata.setMessageType(URIManagerMessageMetadata.MessageTypesEnum.PLATFORM_DELETE_DEVICE);
        metadata.setConversationId(conversationId);
        metadata.setClientId(clientId);
        metadata.asPlatformMessageMetadata().addReceivingPlatformID(new EntityID(platformId));
        message.setPayload(ioTDevicePayload);

        return message;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public List<String> getDeviceIds() {
        return deviceIds;
    }

    public void setDeviceIds(List<String> deviceIds) {
        this.deviceIds = deviceIds;
    }
}
