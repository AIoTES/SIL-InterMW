package eu.interiot.intermw.commons.model.enums;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Author: gasper
 * On: 10.9.2018
 */
public class IoTDeviceTypeTest {

    private static final String DEVICE_URI = "http://inter-iot.eu/GOIoTP#IoTDevice";
    private static final String SENSOR_URI = "http://www.w3.org/ns.sosa/Sensor";
    private static final String ACTUATOR_URI = "http://www.w3.org/ns.sosa/Actuator";

    @Test
    public void shouldConvertEnumToUri() {
        assertEquals("DEVICE enum should return correct device type URI", DEVICE_URI, IoTDeviceType.DEVICE.getDeviceTypeUri());
        assertEquals("SENSOR enum should return correct device type URI", SENSOR_URI, IoTDeviceType.SENSOR.getDeviceTypeUri());
        assertEquals("ACTUATOR enum should return correct device type URI", ACTUATOR_URI, IoTDeviceType.ACTUATOR.getDeviceTypeUri());
    }

    @Test
    public void shouldConvertUriToEnum() {
        assertEquals("DEVICE_URI should produce DEVICE IoTDeviceType enum", IoTDeviceType.DEVICE, IoTDeviceType.fromDeviceTypeUri(DEVICE_URI));
        assertEquals("SENSOR_URI should produce SENSOR IoTDeviceType enum", IoTDeviceType.SENSOR, IoTDeviceType.fromDeviceTypeUri(SENSOR_URI));
        assertEquals("ACTUATOR_URI should produce ACTUATOR IoTDeviceType enum", IoTDeviceType.ACTUATOR, IoTDeviceType.fromDeviceTypeUri(ACTUATOR_URI));
    }
}
