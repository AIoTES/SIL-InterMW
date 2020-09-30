package eu.interiot.intermw.commons.requests;

import eu.interiot.message.Message;
import eu.interiot.message.managers.URI.URIManagerMessageMetadata;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Gasper Vrhovsek
 */
public class ListDevicesReqTest {

    private static final String CLIENT_ID = "myclient";
    private static final String PLATFORM_ID = "http://test.inter-iot.eu/test-platform1";

    @Test
    public void testPlatformListDevicesReq() throws IOException {
        ListDevicesReq req = new ListDevicesReq(CLIENT_ID, PLATFORM_ID);
        Message message = req.toMessage();

        ListDevicesReq reqConverted = new ListDevicesReq(message);

        // assert message
        assertTrue("Message metadata types should include LIST_DEVICES type",
                message.getMetadata().getMessageTypes().contains(URIManagerMessageMetadata.MessageTypesEnum.LIST_DEVICES));
        assertEquals("Message metadata receiving platform should equal PLATFORM_ID",
                PLATFORM_ID,
                message.getMetadata().asPlatformMessageMetadata().getReceivingPlatformIDs().iterator().next().toString());
        assertEquals("Message metadata client ID should equal CLIENT_ID", CLIENT_ID, message.getMetadata().getClientId().get());

        // assert converted back into req
        assertEquals("Converted ListDevicesReq client ID should equal CLIENT_ID", CLIENT_ID, reqConverted.getClientId());
        assertEquals("Converted ListDevicesReq platform ID should equal CLIENT_ID", PLATFORM_ID, reqConverted.getPlatformId());
    }
}
