package eu.interiot.intermw.commons.responses;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.interiot.message.Message;

import java.io.IOException;

/**
 * @author Gasper Vrhovsek
 */
public class PlatformUnregisterRes extends BaseRes {
    public PlatformUnregisterRes() {
    }

    public PlatformUnregisterRes(Message message) {
        super(message);
    }

    public static PlatformUnregisterRes fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, PlatformUnregisterRes.class);
    }
}
