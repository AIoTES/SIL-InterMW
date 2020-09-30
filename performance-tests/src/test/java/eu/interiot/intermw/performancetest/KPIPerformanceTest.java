package eu.interiot.intermw.performancetest;

import com.google.common.hash.Hashing;
import eu.interiot.intermw.api.InterMWInitializer;
import eu.interiot.intermw.api.model.PlatformCreateDeviceInput;
import eu.interiot.intermw.api.model.RegisterClientInput;
import eu.interiot.intermw.api.model.RegisterPlatformInput;
import eu.interiot.intermw.api.model.SubscribeInput;
import eu.interiot.intermw.api.rest.model.MwAsyncResponse;
import eu.interiot.intermw.api.rest.resource.InterMwApiREST;
import eu.interiot.intermw.api.rest.resource.InterMwExceptionMapper;
import eu.interiot.intermw.commons.DefaultConfiguration;
import eu.interiot.intermw.commons.dto.ResponseMessage;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.model.Client;
import eu.interiot.intermw.commons.model.IoTDevice;
import eu.interiot.message.ID.EntityID;
import eu.interiot.message.Message;
import eu.interiot.message.exceptions.MessageException;
import eu.interiot.message.managers.URI.URIManagerMessageMetadata;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.update.UpdateRequest;
import org.apache.poi.ss.usermodel.*;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KPIPerformanceTest extends JerseyTest {
    private static final String CONFIG_FILE = "intermw.properties";
    private static final String PLATFORM_ID = "http://test.inter-iot.eu/test-platform1";
    private static final String CLIENT_ID = "myclient";
    private static final String DEVICE_ID_PREFIX = "http://test.inter-iot.eu/device";
    private static final int TIMEOUT = 30;
    private static final Logger logger = LoggerFactory.getLogger(KPIPerformanceTest.class);
    private static final int RESPONSE_CODE_202 = 202;
    private final String INSTRUCTIONS_FILE = System.getProperty("user.dir") + "/src/test/resources/instructions.xls";
    private final String CSV_DIRECTORY = System.getProperty("user.dir") + "/target/";
    private Map<String, BlockingQueue<Message>> conversations = new HashMap<>();
    private static DefaultConfiguration conf;

    @BeforeClass
    public static void beforeClass() throws MiddlewareException {
        conf = new DefaultConfiguration(CONFIG_FILE);
        CachingConnectionFactory cf = new CachingConnectionFactory(conf.getRabbitmqHostname(), conf.getRabbitmqPort());
        cf.setUsername(conf.getRabbitmqUsername());
        cf.setPassword(conf.getRabbitmqPassword());
        RabbitAdmin rabbitAdmin = new RabbitAdmin(cf);
        String[] queuesToDelete = {"arm_prm_queue", "prm_ipsmrm_queue", "prm_srm_queue", "ipsmrm_prm_queue",
                "srm_prm_queue", "prm_arm_queue", "error_queue",
                "ipsmrm_bridge_http_test.inter-iot.eu_test-platform11_queue",
                "bridge_ipsmrm_http_test.inter-iot.eu_test-platform11_queue",
                "ipsmrm_bridge_http_test.inter-iot.eu_test-platform12_queue",
                "bridge_ipsmrm_http_test.inter-iot.eu_test-platform12_queue",
                "ipsmrm_bridge_http_test.inter-iot.eu_test-platform13_queue",
                "bridge_ipsmrm_http_test.inter-iot.eu_test-platform13_queue",
                "client-myclient"};
        for (String queueToDelete : queuesToDelete) {
            rabbitAdmin.deleteQueue(queueToDelete);
        }
    }

    @Before
    public void clearParliament() throws Exception {
        DefaultConfiguration conf = new DefaultConfiguration(CONFIG_FILE);
        String parliamentUrl = conf.getProperty("parliament.url");

        try (RDFConnection conn = RDFConnectionFactory.connect(parliamentUrl, "sparql", "sparql", "sparql")) {
            UpdateRequest updateRequest = new UpdateRequest();
            updateRequest.add("DROP GRAPH <http://clients>;");
            updateRequest.add("DROP GRAPH <http://platforms>;");
            updateRequest.add("DROP GRAPH <http://devices>;");
            updateRequest.add("DROP GRAPH <http://subscriptions>;");
            conn.update(updateRequest);
        }
        InterMWInitializer.initialize();
    }

    @Override
    protected Application configure() {
        final SecurityContext securityContextMock = mock(SecurityContext.class);
        when(securityContextMock.getUserPrincipal()).thenReturn(() -> CLIENT_ID);

        return new ResourceConfig()
                .register((ContainerRequestFilter) requestContext -> requestContext.setSecurityContext(securityContextMock))
                .register(InterMwApiREST.class)
                .register(InterMwExceptionMapper.class);
    }

    @Test
    public void test() throws Exception {
        // Number of experiment you want to test. Test 10 is just a testing config.
        int experimentNumber = 10;

        Workbook workbook = WorkbookFactory.create(new BufferedInputStream(new FileInputStream(INSTRUCTIONS_FILE)));
        Sheet sheet = workbook.getSheet("Instructions");
        DataFormatter dataFormatter = new DataFormatter();
        Row row = sheet.getRow(experimentNumber);

        int instructionsExperimentNumber = Integer.parseInt(dataFormatter.formatCellValue(row.getCell(0)));
        int testDuration = Integer.parseInt(dataFormatter.formatCellValue(row.getCell(1)));
        int platformNumber = Integer.parseInt(dataFormatter.formatCellValue(row.getCell(2)));
        MWPerformanceTestBridge.setMessageGenerationInterval(1000 / Integer.parseInt(dataFormatter.formatCellValue(row.getCell(3))));
        String method = dataFormatter.formatCellValue(row.getCell(4));
        int receivingCapacity = Integer.parseInt(dataFormatter.formatCellValue(row.getCell(5)));

        assertEquals("SERVER_PUSH", method);
        assertEquals(experimentNumber, instructionsExperimentNumber);

        startPullingResponseMessagesServerPush();
        registerClient(receivingCapacity);
        for (int i = 1; i <= platformNumber; i++) {
            registerPlatform(i);
        }
        registerDevices(platformNumber);
        String subscriptionId = subscribe(platformNumber);
        checkObservations(subscriptionId, testDuration, experimentNumber);
    }

    private void registerClient(int receivingCapacity) throws Exception {
        RegisterClientInput input = new RegisterClientInput();
        input.setClientId(CLIENT_ID);
        input.setResponseDelivery(Client.ResponseDelivery.SERVER_PUSH);
        input.setCallbackUrl("http://localhost:5000");
        input.setResponseFormat(Client.ResponseFormat.JSON_LD);
        input.setReceivingCapacity(receivingCapacity);
        Entity<RegisterClientInput> entity = Entity.json(input);

        Response response = request("mw2mw/clients").post(entity);
        URI locationUri = new URI(response.getHeaderString("Location"));
        Client client = response.readEntity(Client.class);

        assertEquals("Register client response code should be 201", 201, response.getStatus());
        assertEquals("Register client response should return correct location", "/mw2mw/clients/myclient", locationUri.getPath());
        assertEquals("Created clientId should equal input clientId", CLIENT_ID, client.getClientId());
    }

    private void registerPlatform(int platformNumber) throws Exception {
        RegisterPlatformInput input = new RegisterPlatformInput();
        input.setPlatformId(PLATFORM_ID + platformNumber);
        input.setName("InterMW Test Platform " + platformNumber);
        input.setType("http://inter-iot.eu/MWPerformanceTestPlatform");
        input.setBaseEndpoint("http://localhost:4568");
        input.setLocation("http://test.inter-iot.eu/TestLocation");

        String encryptedPassword = Hashing.sha256()
                .hashString("somepassword", StandardCharsets.UTF_8)
                .toString();
        input.setUsername("interiotuser");
        input.setEncryptedPassword(encryptedPassword);
        input.setEncryptionAlgorithm("SHA-256");
        Entity<RegisterPlatformInput> entity = Entity.json(input);

        Response response = request("mw2mw/platforms").post(entity);
        MwAsyncResponse mwAsyncResponse = response.readEntity(MwAsyncResponse.class);
        String conversationId = mwAsyncResponse.getConversationId();

        ResponseMessage responseMessageWrapper = waitForResponseMessage(conversationId);
        Set<URIManagerMessageMetadata.MessageTypesEnum> messageTypes;
        String senderPlatformId = null;

        Message responseMessage = responseMessageWrapper.getMessageJSON_LD();
        messageTypes = responseMessage.getMetadata().getMessageTypes();
        Optional<EntityID> senderPlatformEntityId = responseMessage.getMetadata().asPlatformMessageMetadata().getSenderPlatformId();
        if (senderPlatformEntityId.isPresent()) {
            senderPlatformId = senderPlatformEntityId.get().toString();
        }

        assertEquals("Register platform response should be 202", RESPONSE_CODE_202, response.getStatus());
        assertTrue("RegisterPlatform response messageTypes should contain RESPONSE type",
                messageTypes.contains(URIManagerMessageMetadata.MessageTypesEnum.RESPONSE));
        assertTrue("RegisterPlatform response messageTypes should contain PLATFORM_REGISTER type",
                messageTypes.contains(URIManagerMessageMetadata.MessageTypesEnum.PLATFORM_REGISTER));
        assertEquals("RegisterPlatform response should contain correct PLATFORM_ID",
                PLATFORM_ID + platformNumber, senderPlatformId);
    }

    private void registerDevices(int platformNumber) throws Exception {
        List<IoTDevice> devices = new ArrayList<>();
        for (int i = 1; i <= platformNumber; i++) {
            IoTDevice ioTDevice = getIoTDevice(i);
            devices.add(ioTDevice);
        }
        PlatformCreateDeviceInput input = new PlatformCreateDeviceInput(devices);

        Entity<PlatformCreateDeviceInput> entity = Entity.json(input);
        Response response = request("mw2mw/devices").post(entity);
        MwAsyncResponse mwAsyncResponse = response.readEntity(MwAsyncResponse.class);
        String conversationId = mwAsyncResponse.getConversationId();

        ResponseMessage responseMessageWrapper = waitForResponseMessage(conversationId);
        Set<URIManagerMessageMetadata.MessageTypesEnum> messageTypes;

        Message responseMessage = responseMessageWrapper.getMessageJSON_LD();
        messageTypes = responseMessage.getMetadata().getMessageTypes();

        assertEquals("Register device response code should be 202", RESPONSE_CODE_202, response.getStatus());
        assertTrue("PlatformCreateDevice response messageTypes should contain RESPONSE type",
                messageTypes.contains(URIManagerMessageMetadata.MessageTypesEnum.RESPONSE));
        assertTrue("PlatformCreateDevice response messageTypes should contain PLATFORM_CREATE_DEVICE type",
                messageTypes.contains(URIManagerMessageMetadata.MessageTypesEnum.PLATFORM_CREATE_DEVICE));
    }

    private String subscribe(int platformNumber) throws Exception {
        SubscribeInput input = new SubscribeInput();
        for (int i = 1; i <= platformNumber; i++) {
            input.addIoTDevice(DEVICE_ID_PREFIX + i);
        }
        Entity<SubscribeInput> entity = Entity.json(input);
        Response response = request("mw2mw/subscriptions").post(entity);
        MwAsyncResponse mwAsyncResponse = response.readEntity(MwAsyncResponse.class);
        String conversationId = mwAsyncResponse.getConversationId();

        for (int i = 1; i <= input.getDeviceIds().size(); i++) {
            ResponseMessage responseMessageWrapper = waitForResponseMessage(conversationId);
            Set<URIManagerMessageMetadata.MessageTypesEnum> messageTypes;

            Message message = responseMessageWrapper.getMessageJSON_LD();
            messageTypes = message.getMetadata().getMessageTypes();

            assertEquals("Subscribe response code should be 202", RESPONSE_CODE_202, response.getStatus());
            assertTrue("Subscribe response messageTypes should contain RESPONSE type", messageTypes.contains(URIManagerMessageMetadata.MessageTypesEnum.RESPONSE));
            assertTrue("Subscribe response messageTypes should contain SUBSCRIBE type", messageTypes.contains(URIManagerMessageMetadata.MessageTypesEnum.SUBSCRIBE));
        }
        return conversationId;
    }

    private void checkObservations(String conversationId, int duration, int experimentNumber) throws Exception {
        File fileCsv = new File(CSV_DIRECTORY + "/KPITest" + experimentNumber + "Raw.csv");
        boolean deletedFile = true, createdFile;
        if (fileCsv.exists()) {
            deletedFile = fileCsv.delete();
            createdFile = fileCsv.createNewFile();
        } else {
            createdFile = fileCsv.createNewFile();
        }

        assertTrue("The .csv file was not deleted properly. ", deletedFile);
        assertTrue("The new .csv file could not be created. ", createdFile);

        final PrintWriter writer = new PrintWriter(fileCsv);
        writer.println("message datetime, message consumed datetime, duration in millis");
        logger.info("Started writing csv file KPITests" + experimentNumber + "Raw.csv");

        long durationMillis = (duration + 30) * 1000;
        long durationMillisCheck = duration * 1000;
        long timeStart = System.currentTimeMillis();
        long timeEnd = System.currentTimeMillis();
        long consumedMessagesCounter = 0;
        boolean subscribed = true;

        while ((timeEnd - timeStart) < durationMillis) {
            ResponseMessage responseMessage = waitForResponseMessage(conversationId);
            Message message = responseMessage.getMessageJSON_LD();
            String responseConversationId = message.getMetadata().getConversationId().get();
            if (message.getMetadata().getMessageTypes().contains(URIManagerMessageMetadata.MessageTypesEnum.OBSERVATION)) {
                consumedMessagesCounter++;

                Date dateMetadata = message.getMetadata().getDateTimeStamp().get().getTime();
                Date dateNow = new Date();
                long millisString = (dateNow.getTime() - dateMetadata.getTime());
                writer.println(dateMetadata.toString() + ", " + dateNow.toString() + ", " + millisString);

                assertNotNull(message);
                assertEquals("Observation response message conversationId should match expected conversationId", conversationId, responseConversationId);

                timeEnd = System.currentTimeMillis();
                if ((timeEnd - timeStart) > durationMillisCheck) {
                    if (subscribed) {
                        unsubscribe(conversationId);
                        logger.debug("Stopping Platform message generator.");
                        subscribed = false;
                    }
                    if (MWPerformanceTestBridge.getGeneratedMessageCounter() == consumedMessagesCounter) {
                        logger.info("Test lasted for {} ms", (timeEnd - timeStart));
                        break;
                    }
                }
            } else {
                logger.debug("Received message with message type: {}", message.getMetadata().getMessageTypes().toString());
            }
        }
        writer.close();
    }

    private void unsubscribe(String subscriptionId) throws InterruptedException {
        Response response = request("mw2mw/subscriptions/" + subscriptionId).delete();
        MwAsyncResponse mwAsyncResponse = response.readEntity(MwAsyncResponse.class);
        String conversationId = mwAsyncResponse.getConversationId();

        waitForResponseMessage(conversationId);
    }

    private void startPullingResponseMessagesServerPush() throws Exception {
        Server server = new Server(5000);
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
                StringWriter writer = new StringWriter();
                IOUtils.copy(request.getInputStream(), writer);

                Message message;
                try {
                    message = new Message(writer.toString());
                } catch (MessageException e) {
                    throw new IOException("Failed to read input stream.");
                }
                String conversationId = message.getMetadata().getConversationId().get();
                if (!conversations.containsKey(conversationId)) {
                    fail("Unexpected conversationId.");
                }
                logger.debug("New message has been retrieved of type {} with conversationId {}.",
                        message.getMetadata().getMessageTypes(), conversationId);
                conversations.get(conversationId).add(message);
            }
        });
        server.start();
    }

    private ResponseMessage waitForResponseMessage(String conversationId) throws InterruptedException {
        assertNotNull(conversationId);
        return new ResponseMessage(waitForJSONLDResponseMessage(conversationId), Client.ResponseFormat.JSON_LD);
    }

    private Message waitForJSONLDResponseMessage(String conversationId) throws InterruptedException {
        if (!conversations.containsKey(conversationId)) {
            conversations.put(conversationId, new LinkedBlockingQueue<>());
        }

        Message message = conversations.get(conversationId).poll(TIMEOUT, TimeUnit.SECONDS);
        assertNotNull(message);
        assertEquals(message.getMetadata().getConversationId().get(), conversationId);
        return message;
    }

    private Invocation.Builder request(String path) {
        return target(path)
                .request()
                .header("Client-ID", CLIENT_ID);
    }

    private IoTDevice getIoTDevice(int i) {
        IoTDevice ioTDevice = new IoTDevice(DEVICE_ID_PREFIX + i);
        ioTDevice.setName("Device " + i);
        ioTDevice.setHostedBy(PLATFORM_ID + i);
        ioTDevice.setLocation("http://test.inter-iot.eu/TestLocation");
        return ioTDevice;
    }
}