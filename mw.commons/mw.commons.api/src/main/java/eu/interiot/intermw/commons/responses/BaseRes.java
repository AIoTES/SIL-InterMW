package eu.interiot.intermw.commons.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.interiot.message.Message;
import eu.interiot.message.managers.URI.URIManagerMessageMetadata;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;
import java.util.Set;

/**
 * @author Gasper Vrhovsek
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class BaseRes {
    private Set<URIManagerMessageMetadata.MessageTypesEnum> messageTypes;
    private String senderPlatform;
    private String conversationId;

    public BaseRes() {
    }

    public BaseRes(Message message) {
        this.messageTypes = message.getMetadata().getMessageTypes();
        this.conversationId = message.getMetadata().printConversationId();
        this.senderPlatform = message.getMetadata().asPlatformMessageMetadata().getSenderPlatformId().isPresent() ?
                message.getMetadata().asPlatformMessageMetadata().getSenderPlatformId().get().toString() :
                null;
    }

    public Set<URIManagerMessageMetadata.MessageTypesEnum> getMessageTypes() {
        return messageTypes;
    }

    public String getSenderPlatform() {
        return senderPlatform;
    }

    public String getConversationId() {
        return conversationId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("messageTypes", messageTypes)
                .append("senderPlatform", senderPlatform)
                .append("conversationId", conversationId)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        BaseRes baseRes = (BaseRes) o;

        return new EqualsBuilder()
                .append(messageTypes, baseRes.messageTypes)
                .append(senderPlatform, baseRes.senderPlatform)
                .append(conversationId, baseRes.conversationId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(messageTypes)
                .append(senderPlatform)
                .append(conversationId)
                .toHashCode();
    }

    public static BaseRes fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, BaseRes.class);
    }
}
