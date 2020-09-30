package eu.interiot.intermw.commons.dto;

import eu.interiot.intermw.commons.model.Client;
import eu.interiot.intermw.commons.responses.BaseRes;
import eu.interiot.message.Message;

/**
 * @author Gasper Vrhovsek
 */
public class ResponseMessage {
    private Message messageJSON_LD;
    private BaseRes messageJSON;
    private Client.ResponseFormat responseFormat;

    public ResponseMessage() {
    }

    public ResponseMessage(Message message, Client.ResponseFormat responseFormat) {
        this(message, null, responseFormat);
    }

    public ResponseMessage(BaseRes messageJSON, Client.ResponseFormat responseFormat) {
        this(null, messageJSON, responseFormat);
    }

    private ResponseMessage(Message messageJSON_LD, BaseRes messageJSON, Client.ResponseFormat responseFormat) {
        this.messageJSON_LD = messageJSON_LD;
        this.messageJSON = messageJSON;
        this.responseFormat = responseFormat;
    }

    public Message getMessageJSON_LD() {
        return messageJSON_LD;
    }

    public BaseRes getMessageJSON() {
        return messageJSON;
    }

    public Client.ResponseFormat getResponseFormat() {
        return responseFormat;
    }
}
