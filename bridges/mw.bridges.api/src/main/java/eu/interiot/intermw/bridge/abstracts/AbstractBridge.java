/**
 * INTER-IoT. Interoperability of IoT Platforms.
 * INTER-IoT is a R&D project which has received funding from the European
 * Union’s Horizon 2020 research and innovation programme under grant
 * agreement No 687283.
 * <p>
 * Copyright (C) 2016-2018, by (Author's company of this file):
 * - XLAB d.o.o.
 * <p>
 * For more information, contact:
 * - @author <a href="mailto:flavio.fuart@xlab.si">Flavio Fuart</a>
 * - Project coordinator:  <a href="mailto:coordinator@inter-iot.eu"></a>
 * <p>
 * This code is licensed under the EPL license, available at the root
 * application directory.
 */
package eu.interiot.intermw.bridge.abstracts;

import eu.interiot.intermw.bridge.BridgeConfiguration;
import eu.interiot.intermw.bridge.BridgeContext;
import eu.interiot.intermw.bridge.exceptions.BridgeException;
import eu.interiot.intermw.bridge.model.Bridge;
import eu.interiot.intermw.comm.broker.Publisher;
import eu.interiot.intermw.commons.ErrorReporter;
import eu.interiot.intermw.commons.exceptions.ErrorCode;
import eu.interiot.intermw.commons.exceptions.UnknownActionException;
import eu.interiot.intermw.commons.model.Platform;
import eu.interiot.message.ID.EntityID;
import eu.interiot.message.Message;
import eu.interiot.message.managers.URI.URIManagerMessageMetadata.MessageTypesEnum;
import eu.interiot.message.utils.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Set;

public abstract class AbstractBridge implements Bridge {
    private static final Logger log = LoggerFactory.getLogger(BridgeContext.class);
    protected final BridgeConfiguration configuration;
    protected Platform platform;
    protected Publisher<Message> publisher;
    protected ErrorReporter errorReporter;
    protected URL bridgeCallbackUrl;
    private boolean isPlatformRegistered = false;

    public AbstractBridge(BridgeConfiguration configuration, Platform platform) throws BridgeException {
        log.debug("Creating bridge instance for the platform {}...", platform.getPlatformId());
        this.configuration = configuration;
        this.platform = platform;

        try {
            bridgeCallbackUrl = new URL(configuration.getProperty("bridge.callback.url"));
        } catch (Exception e) {
            throw new BridgeException("Invalid bridge callback URL specified with 'bridge.callback.url' property: " +
                    configuration.getProperty("bridge.callback.url"), e);
        }
    }

    @Override
    public void setPublisher(Publisher<Message> publisher) {
        this.publisher = publisher;
    }

    @Override
    public void setErrorReporter(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }

    @Override
    public void process(Message message) throws BridgeException {

        Set<MessageTypesEnum> messageTypes = message.getMetadata().getMessageTypes();

        if (!isPlatformRegistered && !messageTypes.contains(MessageTypesEnum.PLATFORM_REGISTER)) {
            throw new BridgeException("No platform has been registered for this bridge instance.");

        } else if (isPlatformRegistered && messageTypes.contains(MessageTypesEnum.PLATFORM_REGISTER)) {
            throw new BridgeException("Platform has already been registered for this bridge instance.");
        }

        Message responseMessage;

        try {
            if (messageTypes.contains(MessageTypesEnum.PLATFORM_REGISTER)) {

                responseMessage = registerPlatform(message);
                isPlatformRegistered = true;

            } else if (messageTypes.contains(MessageTypesEnum.PLATFORM_UPDATE)) {

                responseMessage = updatePlatform(message);

            } else if (messageTypes.contains(MessageTypesEnum.PLATFORM_UNREGISTER)) {

                responseMessage = unregisterPlatform(message);
                isPlatformRegistered = false;

            } else if (messageTypes.contains(MessageTypesEnum.SUBSCRIBE)) {

                responseMessage = subscribe(message);

            } else if (messageTypes.contains(MessageTypesEnum.UNSUBSCRIBE)) {

                responseMessage = unsubscribe(message);

            } else if (messageTypes.contains(MessageTypesEnum.QUERY)) {

                responseMessage = query(message);

            } else if (messageTypes.contains(MessageTypesEnum.LIST_DEVICES)) {

                responseMessage = listDevices(message);

            } else if (messageTypes.contains(MessageTypesEnum.PLATFORM_CREATE_DEVICE)) {

                responseMessage = platformCreateDevices(message);

            } else if (messageTypes.contains(MessageTypesEnum.PLATFORM_UPDATE_DEVICE)) {

                responseMessage = platformUpdateDevices(message);

            } else if (messageTypes.contains(MessageTypesEnum.PLATFORM_DELETE_DEVICE)) {

                responseMessage = platformDeleteDevices(message);

            } else if (messageTypes.contains(MessageTypesEnum.OBSERVATION)) {

                responseMessage = observe(message);

            } else if (messageTypes.contains(MessageTypesEnum.ACTUATION)) {

                responseMessage = actuate(message);

            } else if (messageTypes.contains(MessageTypesEnum.ERROR)) {

                responseMessage = error(message);

            } else if (messageTypes.contains(MessageTypesEnum.UNRECOGNIZED)) {

                responseMessage = unrecognized(message);

            } else {

                throw new UnknownActionException("Unsupported message type: " + messageTypes);
            }

            if (responseMessage == null) {
                throw new BridgeException("Null response received from the bridge.");
            }

            responseMessage.getMetadata().asPlatformMessageMetadata().setSenderPlatformId(new EntityID(platform.getPlatformId()));
            publisher.publish(responseMessage);

        } catch (Exception e) {
            String desc = String.format("Bridge failed to process message %s of type %s for the platform %s.",
                    message.getMetadata().getMessageID().orElse("N/A"), messageTypes, platform.getPlatformId());
            log.error(desc, e);
            errorReporter.sendErrorResponseMessage(message, e, desc, ErrorCode.ERROR_HANDLING_RECEIVED_MESSAGE, publisher);
        }
    }

    protected Message createResponseMessage(Message message) {
        Message responseMessage = MessageUtils.createResponseMessage(message);
        responseMessage.getMetadata().asPlatformMessageMetadata().setSenderPlatformId(new EntityID(platform.getPlatformId()));
        return responseMessage;
    }

    public abstract Message registerPlatform(Message message) throws Exception;

    public abstract Message updatePlatform(Message message) throws Exception;

    public abstract Message unregisterPlatform(Message message) throws Exception;

    public abstract Message subscribe(Message message) throws Exception;

    public abstract Message unsubscribe(Message message) throws Exception;

    public abstract Message query(Message message) throws Exception;

    public abstract Message listDevices(Message message) throws Exception;

    public abstract Message platformCreateDevices(Message message) throws Exception;

    public abstract Message platformUpdateDevices(Message message) throws Exception;

    public abstract Message platformDeleteDevices(Message message) throws Exception;

    public abstract Message observe(Message message) throws Exception;

    public abstract Message actuate(Message message) throws Exception;

    public abstract Message error(Message message) throws Exception;

    public abstract Message unrecognized(Message message) throws Exception;

}
