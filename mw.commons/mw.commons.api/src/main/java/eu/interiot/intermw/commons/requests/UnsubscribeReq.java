package eu.interiot.intermw.commons.requests;

import eu.interiot.message.ID.EntityID;
import eu.interiot.message.Message;
import eu.interiot.message.MessageMetadata;
import eu.interiot.message.managers.URI.URIManagerMessageMetadata;
import eu.interiot.message.payload.types.IoTDevicePayload;
import eu.interiot.message.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UnsubscribeReq {
    private static final String CONVERSATION_ID_PROP = "http://inter-iot.eu/conversationId";
    private String conversationId;
    private String subscriptionId;
    private String platformId;
    private List<String> deviceIds;
    private String clientId;

    public UnsubscribeReq(Message message) {
        conversationId = message.getMetadata().getConversationId().orElse(null);
        clientId = message.getMetadata().getClientId().orElse(null);

        subscriptionId = message.getMetadata().asPlatformMessageMetadata().getSubscriptionId().isPresent() ?
                message.getMetadata().asPlatformMessageMetadata().getSubscriptionId().get() : conversationId;

        IoTDevicePayload ioTDevicePayload = message.getPayloadAsGOIoTPPayload().asIoTDevicePayload();
        if (ioTDevicePayload.getIoTDevices() != null && ioTDevicePayload.getIoTDevices().size() > 0) {
            deviceIds = new ArrayList<>();
            Set<EntityID> deviceEntityIds = ioTDevicePayload.getIoTDevices();
            for (EntityID deviceEntityId : deviceEntityIds) {
                deviceIds.add(deviceEntityId.toString());
            }
        }
    }

    public UnsubscribeReq(String subscriptionId, String platformId, String clientId) {
        this.subscriptionId = subscriptionId;
        this.platformId = platformId;
        this.clientId = clientId;
    }

    public Message toMessage() {
        return toMessage(MessageUtils.generateConversationID());
    }

    public Message toMessage(String conversationId) {
        Message message = new Message();
        MessageMetadata metadata = message.getMetadata();
        metadata.setConversationId(conversationId);
        metadata.setMessageType(URIManagerMessageMetadata.MessageTypesEnum.UNSUBSCRIBE);
        metadata.setClientId(clientId);
        metadata.asPlatformMessageMetadata().addReceivingPlatformID(new EntityID(platformId));
        metadata.asPlatformMessageMetadata().setSubscriptionId(subscriptionId);

        if (deviceIds != null) {
            IoTDevicePayload ioTDevicePayload = new IoTDevicePayload();
            for (String deviceId : deviceIds) {
                EntityID entityID = new EntityID(deviceId);
                ioTDevicePayload.createIoTDevice(entityID);
            }
            message.setPayload(ioTDevicePayload);
        }

        return message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
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

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
