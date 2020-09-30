package eu.interiot.intermw.commons.responses;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.interiot.message.Message;

import java.io.IOException;

public class PlatformUpdateRes extends BaseRes {

    public PlatformUpdateRes() {
    }

    public PlatformUpdateRes(Message message) {
        super(message);
    }

    public static PlatformUpdateRes fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, PlatformUpdateRes.class);
    }
}
