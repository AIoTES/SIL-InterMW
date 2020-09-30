package eu.interiot.intermw.commons.model;

import java.net.URL;

public class Client {
    private String clientId;
    private URL callbackUrl;
    private Integer receivingCapacity;
    private Client.ResponseFormat responseFormat;
    private Client.ResponseDelivery responseDelivery;

    public Client() {
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public URL getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(URL callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public Integer getReceivingCapacity() {
        return receivingCapacity;
    }

    public void setReceivingCapacity(Integer receivingCapacity) {
        this.receivingCapacity = receivingCapacity;
    }

    public ResponseFormat getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(ResponseFormat responseFormat) {
        this.responseFormat = responseFormat;
    }

    public ResponseDelivery getResponseDelivery() {
        return responseDelivery;
    }

    public void setResponseDelivery(ResponseDelivery responseDelivery) {
        this.responseDelivery = responseDelivery;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Client client = (Client) o;

        return clientId.equals(client.clientId);
    }

    @Override
    public int hashCode() {
        return clientId.hashCode();
    }

    public enum ResponseFormat {
        JSON_LD,
        JSON
    }

    public enum ResponseDelivery {
        CLIENT_PULL,
        SERVER_PUSH
    }
}
