package eu.interiot.intermw.commons.responses;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.interiot.message.Message;

import java.io.IOException;

/**
 * @author Gasper Vrhovsek
 */
public class PlatformUpdateDeviceRes extends BaseRes {
    public PlatformUpdateDeviceRes() {
    }

    public PlatformUpdateDeviceRes(Message message) {
        super(message);
    }

    public static PlatformUpdateDeviceRes fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, PlatformUpdateDeviceRes.class);
    }
}
