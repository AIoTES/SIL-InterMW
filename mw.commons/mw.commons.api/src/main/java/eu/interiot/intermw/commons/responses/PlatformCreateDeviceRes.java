package eu.interiot.intermw.commons.responses;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.interiot.message.Message;

import java.io.IOException;

/**
 * @author Gasper Vrhovsek
 */
public class PlatformCreateDeviceRes extends BaseRes {
    public PlatformCreateDeviceRes() {
    }

    public PlatformCreateDeviceRes(Message message) {
        super(message);
    }

    public static PlatformCreateDeviceRes fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, PlatformCreateDeviceRes.class);
    }
}
