package eu.interiot.intermw.integrationtests.utils;

import eu.interiot.intermw.comm.control.ControlComponent;
import eu.interiot.intermw.comm.control.abstracts.AbstractControlComponent;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.model.enums.BrokerTopics;
import eu.interiot.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestErrorTopicHandler extends AbstractControlComponent {
    private static final Logger logger = LoggerFactory.getLogger(TestErrorTopicHandler.class);
    private int numberOfErrorMessages;

    /**
     * The constructor. Creates a new instance of {@link ControlComponent}
     *
     * @throws MiddlewareException
     */
    public TestErrorTopicHandler() throws MiddlewareException {
        setUpListener();
    }

    public void setUpListener() throws MiddlewareException {

        subscribe(BrokerTopics.ERROR.getTopicName(), message -> {
            try {
                logger.info("New message received from ERROR topic:\n" + message.serializeToJSONLD());
                numberOfErrorMessages++;

            } catch (Exception e) {
                logger.error("Failed to handle error message: " + e.getMessage(), e);
            }

        }, Message.class);
    }

    public int getNumberOfErrorMessages() {
        return numberOfErrorMessages;
    }
}
