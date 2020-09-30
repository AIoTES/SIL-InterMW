package eu.interiot.intermw.bridge;

import eu.interiot.intermw.commons.DefaultConfiguration;
import eu.interiot.intermw.commons.interfaces.Configuration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BridgeConfigurationTest {

    @Test
    public void testBridgeConfigurationLoading() throws Exception {
        Configuration intermwConf = new DefaultConfiguration("intermw-test.properties");

        BridgeConfiguration conf = new BridgeConfiguration("MWTestBridge.properties", "http://test.inter-iot.eu/test-platform1", intermwConf);
        assertEquals(conf.getProperties().size(), 7);
        assertEquals(conf.getProperty("bridge.callback.url"), "http://localhost:8980");
        assertEquals(conf.getProperty("bridge.testproperty"), "testproperty-value");
        assertEquals(conf.getProperty("testproperty1"), "instanceA-value1");
        assertEquals(conf.getProperty("testproperty2"), "instanceA-value2");
        assertEquals(conf.getProperty("testproperty3"), "value3");
        assertEquals(conf.getProperty("testproperty5"), "value5");
        assertEquals(conf.getProperty("testproperty6"), "value6");

        conf = new BridgeConfiguration("MWTestBridge.properties", "http://test.inter-iot.eu/test-platform2", intermwConf);
        assertEquals(conf.getProperty("bridge.callback.url"), "http://localhost:8980");
        assertEquals(conf.getProperty("bridge.testproperty"), "testproperty-value");
        assertEquals(conf.getProperties().size(), 8);
        assertEquals(conf.getProperty("testproperty1"), "value1");
        assertEquals(conf.getProperty("testproperty2"), "value2");
        assertEquals(conf.getProperty("testproperty3"), "value3");
        assertEquals(conf.getProperty("testproperty5"), "value5");
        assertEquals(conf.getProperty("testproperty6"), "value6");
        assertEquals(conf.getProperty("testproperty7"), "value7");
    }
}