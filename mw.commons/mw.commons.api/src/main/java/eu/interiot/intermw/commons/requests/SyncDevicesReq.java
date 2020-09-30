package eu.interiot.intermw.commons.requests;

import eu.interiot.intermw.commons.exceptions.InvalidMessageException;
import eu.interiot.intermw.commons.model.IoTDevice;
import eu.interiot.message.ID.EntityID;
import eu.interiot.message.Message;
import eu.interiot.message.MessageMetadata;
import eu.interiot.message.managers.URI.URIManagerMessageMetadata;

import java.util.List;

public class SyncDevicesReq {

    private String platformId;
    private String clientId;
    private List<IoTDevice> devices;

    public SyncDevicesReq(String platformId) {
        this.platformId = platformId;
    }

    public SyncDevicesReq(Message message) throws InvalidMessageException {
        clientId = message.getMetadata().getClientId().orElse(null);
        platformId = message.getMetadata().asPlatformMessageMetadata().getReceivingPlatformIDs().iterator().next().toString();
    }

    public Message toMessage(String conversationId) {
        Message message = new Message();
        MessageMetadata metadata = message.getMetadata();
        metadata.setMessageType(URIManagerMessageMetadata.MessageTypesEnum.LIST_DEVICES);
        metadata.setConversationId(conversationId);
        metadata.asPlatformMessageMetadata().addReceivingPlatformID(new EntityID(platformId));

        return message;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public List<IoTDevice> getDevices() {
        return devices;
    }

    public void setDevices(List<IoTDevice> devices) {
        this.devices = devices;
    }
}
