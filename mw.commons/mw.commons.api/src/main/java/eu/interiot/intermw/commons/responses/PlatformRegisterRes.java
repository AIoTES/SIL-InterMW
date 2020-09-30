package eu.interiot.intermw.commons.responses;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.interiot.message.Message;

import java.io.IOException;

/**
 * @author Gasper Vrhovsek
 */
public class PlatformRegisterRes extends BaseRes {

    public PlatformRegisterRes() {}

    public PlatformRegisterRes(Message message) {
        super(message);
    }

    public static PlatformRegisterRes fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, PlatformRegisterRes.class);
    }
}
