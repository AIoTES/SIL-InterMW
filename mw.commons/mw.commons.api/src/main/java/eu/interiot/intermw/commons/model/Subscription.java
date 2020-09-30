package eu.interiot.intermw.commons.model;

import java.util.ArrayList;
import java.util.List;

public class Subscription {

    private String conversationId;
    private List<String> deviceIds;
    private String clientId;

    public Subscription() {
        deviceIds = new ArrayList<>();
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public List<String> getDeviceIds() {
        return deviceIds;
    }

    public void setDeviceIds(List<String> deviceIds) {
        this.deviceIds = deviceIds;
    }

    public void addDeviceId(String deviceId) {
        this.deviceIds.add(deviceId);
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
