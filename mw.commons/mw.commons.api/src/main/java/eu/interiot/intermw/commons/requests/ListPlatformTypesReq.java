package eu.interiot.intermw.commons.requests;

import eu.interiot.message.Message;
import eu.interiot.message.MessageMetadata;
import eu.interiot.message.managers.URI.URIManagerMessageMetadata;

public class ListPlatformTypesReq {

    private String clientId;

    public Message toMessage() {
        Message message = new Message();
        MessageMetadata metadata = message.getMetadata();
        metadata.setMessageType(URIManagerMessageMetadata.MessageTypesEnum.LIST_SUPPORTED_PLATFORM_TYPES);
        metadata.setClientId(clientId);

        return message;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
