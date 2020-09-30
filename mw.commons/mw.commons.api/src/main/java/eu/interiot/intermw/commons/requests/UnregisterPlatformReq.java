package eu.interiot.intermw.commons.requests;

import eu.interiot.intermw.commons.exceptions.InvalidMessageException;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.message.ID.EntityID;
import eu.interiot.message.Message;
import eu.interiot.message.MessageMetadata;
import eu.interiot.message.managers.URI.URIManagerMessageMetadata.MessageTypesEnum;

import java.util.Set;

public class UnregisterPlatformReq {
    private String platformId;
    private String clientId;

    public UnregisterPlatformReq() {
    }

    public UnregisterPlatformReq(String clientId, String platformId) {
        this.clientId = clientId;
        this.platformId = platformId;
    }

    public UnregisterPlatformReq(Message message) throws InvalidMessageException {
        this.clientId = message.getMetadata().getClientId().orElse(null);
        Set<EntityID> receivingPlatforms = message.getMetadata().asPlatformMessageMetadata().getReceivingPlatformIDs();
        if (receivingPlatforms.size() > 1) {
            throw new InvalidMessageException("Multiple receiving platforms are not supported.");
        }
        platformId = receivingPlatforms.iterator().next().toString();
    }

    public Message toMessage() throws MiddlewareException {
        Message message = new Message();
        MessageMetadata metadata = message.getMetadata();
        metadata.addMessageType(MessageTypesEnum.PLATFORM_UNREGISTER);
        metadata.setClientId(clientId);
        metadata.asPlatformMessageMetadata().addReceivingPlatformID(new EntityID(platformId));
        return message;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }
}
