package eu.interiot.intermw.commons.requests;

import eu.interiot.intermw.commons.exceptions.InvalidMessageException;
import eu.interiot.intermw.commons.model.IoTDevice;
import eu.interiot.intermw.commons.model.extractors.IoTDeviceExtractor;
import eu.interiot.message.ID.EntityID;
import eu.interiot.message.Message;
import eu.interiot.message.MessageMetadata;
import eu.interiot.message.managers.URI.URIManagerMessageMetadata;
import eu.interiot.message.payload.types.IoTDevicePayload;

import java.util.List;

public class PlatformUpdateDeviceReq {

    private String platformId;
    private List<IoTDevice> devices;
    private String clientId;

    public PlatformUpdateDeviceReq(String platformId, List<IoTDevice> devices, String clientId) {
        this.platformId = platformId;
        this.devices = devices;
        this.clientId = clientId;
    }

    public PlatformUpdateDeviceReq(Message message) throws InvalidMessageException {
        clientId = message.getMetadata().getClientId().orElse(null);
        platformId = message.getMetadata().asPlatformMessageMetadata().getReceivingPlatformIDs().iterator().next().toString();
        IoTDevicePayload ioTDevicePayload = message.getPayloadAsGOIoTPPayload().asIoTDevicePayload();

        devices = IoTDeviceExtractor.fromIoTDevicePayload(ioTDevicePayload);
    }

    public Message toMessage(String conversationId) {
        IoTDevicePayload ioTDevicePayload = IoTDeviceExtractor.toIoTDevicePayload(devices);

        Message message = new Message();
        MessageMetadata metadata = message.getMetadata();
        metadata.setMessageType(URIManagerMessageMetadata.MessageTypesEnum.PLATFORM_UPDATE_DEVICE);
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

    public List<IoTDevice> getDevices() {
        return devices;
    }

    public void setDevices(List<IoTDevice> devices) {
        this.devices = devices;
    }
}
