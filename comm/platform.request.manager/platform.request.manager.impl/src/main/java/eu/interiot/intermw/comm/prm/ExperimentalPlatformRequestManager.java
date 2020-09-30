package eu.interiot.intermw.comm.prm;

/**
 * @author Gasper Vrhovsek
 */

import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.interfaces.Configuration;
import eu.interiot.intermw.services.registry.ParliamentRegistry;
import eu.interiot.intermw.services.registry.ParliamentRegistryExperimental;
import eu.interiot.message.Message;
import eu.interiot.message.MessageMetadata;
import eu.interiot.message.managers.URI.URIManagerMessageMetadata.MessageTypesEnum;

import java.util.Set;

import static eu.interiot.message.managers.URI.URIManagerMessageMetadata.MessageTypesEnum.*;

@eu.interiot.intermw.comm.prm.annotations.PlatformRequestManager
public class ExperimentalPlatformRequestManager extends DefaultPlatformRequestManager {
    /**
     * @param configuration The configuration for this platform request manager
     */
    public ExperimentalPlatformRequestManager(Configuration configuration) throws MiddlewareException {
        super(configuration);
    }

    @Override
    public void handleFromIPSMRM(Message message) throws MiddlewareException {
        MessageMetadata metadata = message.getMetadata();
        Set<MessageTypesEnum> messageTypes = metadata.getMessageTypes();
        String platformId = message.getMetadata().asPlatformMessageMetadata().getSenderPlatformId().get().toString();
        if (messageTypes.contains(DEVICE_REGISTRY_INITIALIZE) || messageTypes.contains(DEVICE_ADD_OR_UPDATE)) {
            ((ParliamentRegistryExperimental) getRegistry()).registerDevices(platformId, message);
        } else {
            super.handleFromIPSMRM(message);
        }
    }

    @Override
    ParliamentRegistry initParliamentRegistry(Configuration configuration) {
        return new ParliamentRegistryExperimental(configuration);
    }
}
