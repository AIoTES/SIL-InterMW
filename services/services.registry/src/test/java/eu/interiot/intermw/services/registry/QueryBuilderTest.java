package eu.interiot.intermw.services.registry;

import eu.interiot.intermw.commons.DefaultConfiguration;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.interfaces.Configuration;
import eu.interiot.intermw.commons.model.IoTDevice;
import eu.interiot.intermw.commons.model.enums.IoTDeviceType;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.update.UpdateRequest;
import org.junit.Before;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Gasper Vrhovsek
 */
public class QueryBuilderTest {
    private static final String DEVICE_ID = "http://inter-iot.eu/device";
    private static final String DEVICE_NAME = "MyDevice";
    private static final String PLATFORM_ID = "http://inter-iot.eu/example-platform";
    private static final String LOCATION = "http://inter-iot.eu/example-location";
    private static final String PROPERTY = "http://inter-iot.eu/example-property";
    private static final String PROCEDURE = "http://inter-iot.eu/example-procedure";
    private static final String MADE_ACTUATION = "http://inter-iot.eu/example-actuation-made";
    private static final String OBSERVES = "http://inter-iot.eu/observes";
    private static final String HOSTS = "http://inter-iot.eu/hosts";
    private static final String FOR_PROPERTY = "http://inter-iot.eu/for-property";
    public static final String UPDATED_POSTFIX = "Updated";

    // just for test
    private ParliamentRegistry registry;

    // TODO this test should become obsolete, as devices are inserted as RDF from discovery
    // mechanism, or as IoTDevice POJOs, which is trivial
    // Hence all test annotation s are commented out here

    @Before
    public void setUp() throws MiddlewareException {
        Configuration conf = new DefaultConfiguration("intermw-test.properties");
        try (RDFConnection conn = RDFConnectionFactory.connect(conf.getParliamentUrl(), "sparql", "sparql", "sparql")) {
            UpdateRequest updateRequest = new UpdateRequest();
            updateRequest.add("DROP GRAPH <http://clients>;");
            updateRequest.add("DROP GRAPH <http://platforms>;");
            updateRequest.add("DROP GRAPH <http://devices>;");
            updateRequest.add("DROP GRAPH <http://subscriptions>;");
            conn.update(updateRequest);
        }

        registry = new ParliamentRegistry(conf);
    }

//    @Test
    public void testInsertDeviceWithQueryBuilder() throws MiddlewareException {
        IoTDevice deviceToInsert = getIoTDevice(1);
        registry.registerDeviceWithQueryBuilder(QueryBuilder.IoTDevice.insert(deviceToInsert).asUpdate());
        List<IoTDevice> devices = registry.getDevices(Collections.singletonList(deviceToInsert.getDeviceId()));

        assertEquals("Retrieved devices list size should be 1", 1, devices.size());

        IoTDevice ioTDevice = devices.get(0);

        System.out.println("deviceToInsert: " + deviceToInsert.toString());
        System.out.println("ioTDevice: " + ioTDevice.toString());

        assertTrue("Retrieved device should equal the inserted device", deviceToInsert.equals(ioTDevice));
    }

//    @Test
    public void testGetDeviceWithQueryBuilder() throws MiddlewareException {
        IoTDevice deviceToInsert1 = getIoTDevice(1);
        IoTDevice deviceToInsert2 = getIoTDevice(2);

        registry.registerDeviceWithQueryBuilder(QueryBuilder.IoTDevice.insert(deviceToInsert1).asUpdate());
        registry.registerDeviceWithQueryBuilder(QueryBuilder.IoTDevice.insert(deviceToInsert2).asUpdate());

        ArrayList<IoTDevice> devices = registry.getDevicesWithQueryBuilder(QueryBuilder.IoTDevice.get(deviceToInsert1));

        assertEquals("Retrieved device list size should equal inserted device list size", 1, devices.size());
    }

//    @Test
    public void testUpdateDeviceWithQueryBuilder() throws MiddlewareException {
        IoTDevice deviceToInsert = getIoTDevice(1);

        String deviceId = deviceToInsert.getDeviceId();
        IoTDevice queryDevice = new IoTDevice(deviceId);
        // Insert device for later update
        registry.registerDeviceWithQueryBuilder(QueryBuilder.IoTDevice.insert(deviceToInsert).asUpdate());

        // Set updated values
        deviceToInsert.setHostedBy(PLATFORM_ID + UPDATED_POSTFIX);
        deviceToInsert.setDetects(PROPERTY + UPDATED_POSTFIX);
        deviceToInsert.setImplementsProcedure(PROCEDURE + UPDATED_POSTFIX);
        deviceToInsert.setLocation(LOCATION + UPDATED_POSTFIX);

        // Change device types to SENSOR + DEVICE
        deviceToInsert.setDeviceTypes(EnumSet.of(IoTDeviceType.DEVICE, IoTDeviceType.SENSOR));

        Set<String> observes = deviceToInsert.getObserves();
        observes.add(OBSERVES + UPDATED_POSTFIX);
        deviceToInsert.setObserves(observes);

        List<String> hosts = deviceToInsert.getHosts();
        hosts.add(HOSTS + UPDATED_POSTFIX);
        deviceToInsert.setHosts(hosts);

        Set<String> forProperty = deviceToInsert.getForProperty();
        forProperty.add(FOR_PROPERTY + UPDATED_POSTFIX);
        deviceToInsert.setForProperty(forProperty);

        // Update the device
        registry.updateDeviceWithQueryBuilder(QueryBuilder.IoTDevice.update(deviceToInsert).asUpdate());
        ArrayList<IoTDevice> deviceListAfterUpdate = registry.getDevicesWithQueryBuilder(QueryBuilder.IoTDevice.get(queryDevice));

        // Assert updated device
        IoTDevice ioTDeviceUpdated = deviceListAfterUpdate.get(0);

        System.out.println("Expected device = ");
        System.out.println(deviceToInsert);
        System.out.println("Retrieved device = ");
        System.out.println(ioTDeviceUpdated);

        assertEquals("Query device should return 1 device", 1, deviceListAfterUpdate.size());
        assertEquals("Updated IoTDevice deviceId should not change", deviceToInsert.getDeviceId(), ioTDeviceUpdated.getDeviceId());
        assertEquals("Updated IoTDevice hostedBy should change", PLATFORM_ID + UPDATED_POSTFIX, ioTDeviceUpdated.getHostedBy());
        assertEquals("Updated IoTDevice detects should change", PROPERTY + UPDATED_POSTFIX, ioTDeviceUpdated.getDetects());
        assertEquals("Updated IoTDevice implementsProcedure should change", PROCEDURE + UPDATED_POSTFIX, ioTDeviceUpdated.getImplementsProcedure());
        assertEquals("Updated IoTDevice location should change", LOCATION + UPDATED_POSTFIX, ioTDeviceUpdated.getLocation());

        assertEquals("Updated IoTDevice types should change", deviceToInsert.getDeviceTypes(), ioTDeviceUpdated.getDeviceTypes());

        assertEquals("Updated IoTDevice observes should contain one element more", deviceToInsert.getObserves().size(), ioTDeviceUpdated.getObserves().size());
        assertTrue("Updated IoTDevice observes should contain updated element", ioTDeviceUpdated.getObserves().contains(OBSERVES + UPDATED_POSTFIX));

        assertEquals("Updated IoTDevice hosts should contain one element more", deviceToInsert.getHosts().size(), ioTDeviceUpdated.getHosts().size());
        assertTrue("Updated IoTDevice hosts should contain updated element", ioTDeviceUpdated.getHosts().contains(HOSTS + UPDATED_POSTFIX));

        assertEquals("Updated IoTDevice forProperty should contain one element more", deviceToInsert.getForProperty().size(), ioTDeviceUpdated.getForProperty().size());
        assertTrue("Updated IoTDevice forProperty should contain updated element", ioTDeviceUpdated.getForProperty().contains(FOR_PROPERTY + UPDATED_POSTFIX));
    }

    private IoTDevice getIoTDevice(int numeration) {
        IoTDevice deviceToInsert = new IoTDevice(DEVICE_ID + numeration);
        deviceToInsert.setName(DEVICE_NAME + numeration);
        deviceToInsert.setHostedBy(PLATFORM_ID);
        deviceToInsert.setLocation(LOCATION);
        deviceToInsert.setDeviceTypes(EnumSet.of(IoTDeviceType.DEVICE, IoTDeviceType.ACTUATOR));
        deviceToInsert.addForProperty(PROPERTY + numeration);
        deviceToInsert.setMadeActuation(MADE_ACTUATION + numeration);
        deviceToInsert.setImplementsProcedure(PROCEDURE + numeration);
        return deviceToInsert;
    }
}
