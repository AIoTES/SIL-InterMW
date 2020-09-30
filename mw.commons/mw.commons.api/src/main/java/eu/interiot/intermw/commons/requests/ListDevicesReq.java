package eu.interiot.intermw.commons.requests;

import eu.interiot.message.ID.EntityID;
import eu.interiot.message.Message;
import eu.interiot.message.MessageMetadata;
import eu.interiot.message.managers.URI.URIManagerMessageMetadata;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Gasper Vrhovsek
 */
public class ListDevicesReq {

    private String clientId;
    private String platformId;

    public ListDevicesReq(String clientId, String platformId) {
        this.clientId = clientId;
        this.platformId = platformId;
    }

    public ListDevicesReq(Message message) {
        this.clientId = message.getMetadata().getClientId().orElse(null);
        this.platformId = message.getMetadata().asPlatformMessageMetadata().getReceivingPlatformIDs()
                .iterator().next().toString();
    }

    public Message toMessage() {
        Message message = new Message();
        MessageMetadata metadata = message.getMetadata();
        metadata.setMessageType(URIManagerMessageMetadata.MessageTypesEnum.LIST_DEVICES);
        metadata.setClientId(clientId);
        metadata.asPlatformMessageMetadata().addReceivingPlatformID(new EntityID(platformId));
        return message;
    }

    public String getClientId() {
        return clientId;
    }

    public String getPlatformId() {
        return platformId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ListDevicesReq)) return false;

        ListDevicesReq that = (ListDevicesReq) o;

        return new EqualsBuilder()
                .append(clientId, that.clientId)
                .append(platformId, that.platformId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(clientId)
                .append(platformId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("clientId", clientId)
                .append("platformId", platformId)
                .toString();
    }
}
