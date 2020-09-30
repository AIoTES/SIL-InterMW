package eu.interiot.intermw.api.rest.model;

public class MwAsyncResponse {
    private String conversationId;

    public MwAsyncResponse() {
    }

    public MwAsyncResponse(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
