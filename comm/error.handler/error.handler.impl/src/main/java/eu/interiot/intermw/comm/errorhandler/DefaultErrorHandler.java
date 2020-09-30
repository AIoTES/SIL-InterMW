package eu.interiot.intermw.comm.errorhandler;

import eu.interiot.intermw.comm.control.abstracts.AbstractControlComponent;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.interfaces.Configuration;
import eu.interiot.intermw.commons.model.enums.BrokerTopics;
import eu.interiot.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * A default {@link ErrorHandler} implementation
 *
 * @author <a href="mailto:flavio.fuart@xlab.si">Flavio Fuart</a>
 */
@eu.interiot.intermw.comm.errorhandler.annotations.ErrorHandler
public class DefaultErrorHandler extends AbstractControlComponent implements ErrorHandler {

    private final static Logger logger = LoggerFactory.getLogger(DefaultErrorHandler.class);

    private final Configuration configuration;

    /**
     * Constructor for the DefaultErrorHandler
     *
     * @param configuration Configuration for this instance of DefaultErrorHandler
     */
    public DefaultErrorHandler(Configuration configuration) throws MiddlewareException {
        this.configuration = configuration;
        setUpListeners();

    }

    /**
     */
    public void setUpListeners() throws MiddlewareException {
        subscribe(BrokerTopics.ERROR.getTopicName(), message -> {

            try {
                handleError((Message) message);
            } catch (IOException e) {
                logger.error("Error handling error message: ", e);
                //HAHA what to do now?
            }

        }, Message.class);
    }

    private void handleError(Message message) throws IOException {

        //For now log the message
        logger.info("Received an error message:" + message.serializeToJSONLD());
    }


}
