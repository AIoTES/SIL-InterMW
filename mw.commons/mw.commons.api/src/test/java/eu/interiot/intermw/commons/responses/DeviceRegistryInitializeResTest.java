package eu.interiot.intermw.commons.responses;

import eu.interiot.intermw.commons.model.IoTDevice;
import eu.interiot.intermw.commons.model.enums.IoTDeviceType;
import eu.interiot.message.Message;
import eu.interiot.message.exceptions.MessageException;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Gasper Vrhovsek
 */
public class DeviceRegistryInitializeResTest {

    private static String deviceInitJson;

    @BeforeClass
    public static void beforeClass() throws IOException {
        ClassLoader classLoader = ObservationResTest.class.getClassLoader();
        deviceInitJson = IOUtils.toString(classLoader.getResource("device_init_response.json"), "UTF-8");
    }

    @Test
    public void test() throws IOException, MessageException {

        Message message = new Message(deviceInitJson);

        DeviceRegistryInitializeRes response = new DeviceRegistryInitializeRes(message);
        List<IoTDevice> ioTDevices = response.getIoTDevices();
        Map<String, IoTDevice> ioTDeviceMap = ioTDevices.stream().collect(Collectors.toMap(IoTDevice::getDeviceId, x -> x));

        IoTDevice ioTDeviceEmission1 = ioTDeviceMap.get("http://inter-iot.eu/wso2port/emission/1");
        IoTDevice ioTDeviceLightL01 = ioTDeviceMap.get("http://inter-iot.eu/wso2port/light/L01");

        assertEquals("", 37, ioTDevices.size());

        // Assert sensor iotDeviceEmission1
        assertEquals("", "CABINA PUERTO", ioTDeviceEmission1.getName());
        assertEquals("", "http://www.inter-iot.eu/wso2port", ioTDeviceEmission1.getHostedBy());
        assertTrue("", ioTDeviceEmission1.getDeviceTypes().contains(IoTDeviceType.SENSOR));
        assertTrue("", ioTDeviceEmission1.getDeviceTypes().contains(IoTDeviceType.DEVICE));
        assertTrue("", ioTDeviceEmission1.getObserves().contains("http://inter-iot.eu/LogVPmod#Emissions"));
        assertTrue("", ioTDeviceEmission1.getObserves().contains("http://www.w3.org/ns/sosa/ObservableProperty"));
        assertTrue("", ioTDeviceEmission1.getObserves().contains("http://inter-iot.eu/GOIoTP#MeasurementKind"));

        // Assert actuator ioTDeviceLight01
        assertEquals("", "Noatum access road - E3TCity managed", ioTDeviceLightL01.getName());
        assertEquals("", "http://www.inter-iot.eu/wso2port", ioTDeviceLightL01.getHostedBy());
        assertTrue("", ioTDeviceLightL01.getDeviceTypes().contains(IoTDeviceType.ACTUATOR));
        assertTrue("", ioTDeviceLightL01.getDeviceTypes().contains(IoTDeviceType.DEVICE));
        assertTrue("", ioTDeviceLightL01.getForProperty().contains("http://inter-iot.eu/LogVPmod#Light"));
        assertTrue("", ioTDeviceLightL01.getForProperty().contains("http://www.w3.org/ns/sosa/ActuableProperty"));
        assertTrue("", ioTDeviceLightL01.getForProperty().contains("http://inter-iot.eu/GOIoTP#MeasurementKind"));
    }
}
