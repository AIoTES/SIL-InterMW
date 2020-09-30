package eu.interiot.intermw.api;

import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.interfaces.Configuration;
import eu.interiot.intermw.commons.model.IoTDevice;
import eu.interiot.intermw.services.registry.ParliamentRegistry;
import eu.interiot.intermw.services.registry.ParliamentRegistryExperimental;

import java.util.List;

/**
 * @author Gasper Vrhovsek
 */
public class InterMwApiImplExperimental extends InterMwApiImpl{

    private ParliamentRegistryExperimental registry;

    public InterMwApiImplExperimental(Configuration configuration) throws MiddlewareException {
        super(configuration);
        registry = new ParliamentRegistryExperimental(configuration);
    }

    @Override
    public List<IoTDevice> listDevices(String clientId, String platformId) throws MiddlewareException {
        List<String> platformDeviceGraphs = ((ParliamentRegistryExperimental)getRegistry()).getDeviceIds(platformId);
        return getRegistry().getDevices(platformDeviceGraphs);
    }

    @Override
    public ParliamentRegistry getRegistry() {
        return registry;
    }
}
