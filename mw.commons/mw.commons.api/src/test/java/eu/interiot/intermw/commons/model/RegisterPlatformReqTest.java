package eu.interiot.intermw.commons.model;

import com.google.common.hash.Hashing;
import eu.interiot.intermw.commons.requests.RegisterPlatformReq;
import eu.interiot.message.Message;
import org.junit.Test;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class RegisterPlatformReqTest {

    @Test
    public void testConversion() throws Exception {
        long timeCreated = System.currentTimeMillis();
        Platform platform = new Platform(timeCreated);
        platform.setClientId("myclient");
        platform.setPlatformId("http://test.inter-iot.eu/test-platform1");
        platform.setName("InterMW Test Platform");
        platform.setType("http://inter-iot.eu/MWTestPlatform");
        platform.setBaseEndpoint(new URL("http://localhost:4568"));
        platform.setLocationId("http://test.inter-iot.eu/TestLocation");

        String encryptedPassword = Hashing.sha256()
                .hashString("somepassword", StandardCharsets.UTF_8)
                .toString();
        platform.setUsername("interiotuser");
        platform.setEncryptedPassword(encryptedPassword);
        platform.setEncryptionAlgorithm("SHA-256");

        Location location = new Location(
                "http://test.inter-iot.eu/TestLocation",
                "location description",
                100d,
                70d
        );

        RegisterPlatformReq req = new RegisterPlatformReq(platform, location);
        assertEquals(timeCreated, req.getPlatform().getTimeCreated());

        Message message = req.toMessage();
        System.out.println(message.serializeToJSONLD());
        RegisterPlatformReq req1 = new RegisterPlatformReq(message);
        Platform platform1 = req1.getPlatform();
        Location location1 = req1.getLocation();
        assertEquals(timeCreated, platform1.getTimeCreated());
        assertEquals(platform1.getUsername(), platform.getUsername());
        assertEquals(platform1.getEncryptedPassword(), platform.getEncryptedPassword());
        assertEquals(platform1.getEncryptionAlgorithm(), platform.getEncryptionAlgorithm());
        assertEquals(platform1.getType(), platform.getType());

        assertEquals(location1, location);
    }
}