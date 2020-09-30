package eu.interiot.intermw.api.model;

import eu.interiot.intermw.commons.model.Client;

public class UpdateClientInput {
    private String callbackUrl;
    private Integer receivingCapacity;
    private Client.ResponseFormat responseFormat;
    private Client.ResponseDelivery responseDelivery;

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public Integer getReceivingCapacity() {
        return receivingCapacity;
    }

    public void setReceivingCapacity(Integer receivingCapacity) {
        this.receivingCapacity = receivingCapacity;
    }

    public Client.ResponseFormat getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(Client.ResponseFormat responseFormat) {
        this.responseFormat = responseFormat;
    }

    public Client.ResponseDelivery getResponseDelivery() {
        return responseDelivery;
    }

    public void setResponseDelivery(Client.ResponseDelivery responseDelivery) {
        this.responseDelivery = responseDelivery;
    }
}
