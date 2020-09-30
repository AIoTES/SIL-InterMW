package eu.interiot.intermw.commons.responses;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.interiot.message.Message;

import java.io.IOException;

/**
 * @author Gasper Vrhovsek
 */
public class PlatformDeleteDeviceRes extends BaseRes{
    public PlatformDeleteDeviceRes() {
    }

    public PlatformDeleteDeviceRes(Message message) {
        super(message);
    }

    public static PlatformDeleteDeviceRes fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, PlatformDeleteDeviceRes.class);
    }
}
