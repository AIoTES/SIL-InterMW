package eu.interiot.intermw.commons.responses;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.interiot.message.Message;

import java.io.IOException;

/**
 * @author Gasper Vrhovsek
 */
public class SubscribeRes extends BaseRes {
    public SubscribeRes() {
    }

    public SubscribeRes(Message message) {
        super(message);
    }

    public static SubscribeRes fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, SubscribeRes.class);
    }
}
