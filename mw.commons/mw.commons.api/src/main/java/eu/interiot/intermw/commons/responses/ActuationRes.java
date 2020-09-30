package eu.interiot.intermw.commons.responses;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.interiot.message.Message;

import java.io.IOException;

/**
 * @author Gasper Vrhovsek
 */
public class ActuationRes extends BaseRes{
    public ActuationRes() {
    }

    public ActuationRes(Message message) {
        super(message);
    }

    public static ActuationRes fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, ActuationRes.class);
    }
}
