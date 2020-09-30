package eu.interiot.intermw.commons.responses;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.interiot.message.Message;

import java.io.IOException;

/**
 * @author Gasper Vrhovsek
 */
public class UnsubscribeRes extends BaseRes {
    public UnsubscribeRes() {
    }

    public UnsubscribeRes(Message message) {
        super(message);
    }

    public static UnsubscribeRes fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, UnsubscribeRes.class);
    }
}
