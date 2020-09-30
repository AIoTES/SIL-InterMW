package eu.interiot.intermw.performancetest;

import eu.interiot.intermw.bridge.BridgeConfiguration;
import eu.interiot.intermw.bridge.test.MWTestBridge;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import eu.interiot.intermw.commons.exceptions.InvalidMessageException;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.model.Platform;
import eu.interiot.intermw.commons.requests.SubscribeReq;
import eu.interiot.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@eu.interiot.intermw.bridge.annotations.Bridge(platformType = "http://inter-iot.eu/MWPerformanceTestPlatform")
public class MWPerformanceTestBridge extends MWTestBridge {
    private static int messageGenerationInterval = 5000;
    private static long generatedMessageCounter = 0;
    private final Logger logger = LoggerFactory.getLogger(MWPerformanceTestBridge.class);

    public MWPerformanceTestBridge(BridgeConfiguration configuration, Platform platform) throws MiddlewareException {
        super(configuration, platform);
    }

    public static void setMessageGenerationInterval(int interval) {
        messageGenerationInterval = interval;
    }

    public static long getGeneratedMessageCounter() {
        return generatedMessageCounter;
    }

    @Override
    public Message subscribe(Message message) throws InvalidMessageException {
        SubscribeReq subscribeReq = new SubscribeReq(message);
        String conversationId = subscribeReq.getConversationId();
        ObservationDispatcher dispatcher = new ObservationDispatcher(subscribeReq.getDeviceIds(), conversationId);
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleAtFixedRate(dispatcher, 1000, messageGenerationInterval, TimeUnit.MILLISECONDS);
        subscriptionTaskMap.put(conversationId, scheduledFuture);
        logger.debug("Subscription {} has been scheduled.", conversationId);
        return createResponseMessage(message);
    }

    private class ObservationDispatcher implements Runnable {
        private List<String> deviceIds;
        private String conversationId;

        public ObservationDispatcher(List<String> deviceIds, String conversationId) {
            this.deviceIds = deviceIds;
            this.conversationId = conversationId;
        }

        @Override
        public void run() {
            logger.debug("Sending observation messages for subscription {}...", conversationId);

            for (String deviceId : deviceIds) {
                Message observationMessage;
                observationMessage = createComplexObservationMessage(conversationId, deviceId, "10");
                try {
                    publisher.publish(observationMessage);
                    logger.debug("Dispatched complex observation message");
                } catch (BrokerException e) {
                    logger.error("Failed to dispatch complex observation message: {}", e);
                }

                synchronized (this) {
                    generatedMessageCounter++;
                }
            }
        }
    }
}
