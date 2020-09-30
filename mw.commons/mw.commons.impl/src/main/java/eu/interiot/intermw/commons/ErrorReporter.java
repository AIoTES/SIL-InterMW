package eu.interiot.intermw.commons;

import eu.interiot.intermw.comm.broker.Publisher;
import eu.interiot.intermw.commons.exceptions.ErrorCode;
import eu.interiot.message.Message;
import eu.interiot.message.managers.URI.URIManagerMessageMetadata;
import eu.interiot.message.metadata.ErrorMessageMetadata;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ErrorReporter {
    private final static Logger logger = LoggerFactory.getLogger(ErrorReporter.class);
    private Publisher<Message> errorPublisher;

    public ErrorReporter(Publisher<Message> errorPublisher) {
        this.errorPublisher = errorPublisher;
    }

    public void sendErrorResponseMessage(Message originalMessage, Exception exception, ErrorCode errorCode) {
        sendErrorResponseMessage(originalMessage, exception, null, errorCode);
    }

    public void sendErrorResponseMessage(Message originalMessage, Exception exception, String description, ErrorCode errorCode) {
        Message errorMessage = createErrorResponseMessage(originalMessage, exception, description, errorCode);
        sendMessageToErrorTopic(errorMessage);
    }

    public void sendErrorResponseMessage(Message originalMessage, Exception exception, ErrorCode errorCode,
                                         Publisher<Message> publisher) {
        sendErrorResponseMessage(originalMessage, exception, null, errorCode, publisher);
    }

    public void sendErrorResponseMessage(Message originalMessage, Exception exception, String description,
                                         ErrorCode errorCode, Publisher<Message> publisher) {
        Message errorMessage = createErrorResponseMessage(originalMessage, exception, description, errorCode);

        try {
            publisher.publish(errorMessage);

        } catch (Exception e) {
            logger.error(String.format("Failed to publish error message %s to the exchange %s: %s",
                    errorMessage.getMetadata().getMessageID().get(), publisher.getTopic().getExchangeName(), e.getMessage()), e);

            ErrorCode errorCode1 = ErrorCode.CANNOT_PUBLISH_MESSAGE_UPSTREAM;
            Message errorMessage1 = new Message();
            ErrorMessageMetadata metadata1 = errorMessage.getMetadata().asErrorMessageMetadata();
            errorMessage1.setMetadata(metadata1);
            metadata1.addMessageType(URIManagerMessageMetadata.MessageTypesEnum.ERROR);
            metadata1.setErrorCategory(errorCode1.name());
            metadata1.setErrorDescription(errorCode1.getErrorDescription());
            sendMessageToErrorTopic(errorMessage1);
        }

        // send a copy of error message to ERROR stream
        sendMessageToErrorTopic(errorMessage);
    }

    private Message createErrorResponseMessage(Message originalMessage, Exception exception, String description, ErrorCode errorCode) {
        String conversationID = originalMessage.getMetadata().getConversationId().get();
        logger.debug("Creating error response message based on the message {} for conversation {}...",
                originalMessage.getMetadata().getMessageID().get(), conversationID);

        Message errorMessage = new Message(originalMessage.getMessageConfig());
        ErrorMessageMetadata metadata = errorMessage.getMetadata().asErrorMessageMetadata();
        errorMessage.setMetadata(metadata);
        metadata.setMessageTypes(originalMessage.getMetadata().getMessageTypes());
        metadata.addMessageType(URIManagerMessageMetadata.MessageTypesEnum.RESPONSE);
        metadata.addMessageType(URIManagerMessageMetadata.MessageTypesEnum.ERROR);
        metadata.setConversationId(conversationID);
        metadata.setExceptionStackTrace(exception);
        metadata.setErrorCategory(errorCode != null ? errorCode.name() : "N/A");

        List<String> descriptions = new ArrayList<>();
        if (errorCode != null) {
            descriptions.add(errorCode.getErrorDescription());
        }
        if (description != null) {
            descriptions.add(description);
        }
        if (exception.getMessage() != null) {
            descriptions.add(exception.getMessage());
        }
        if (descriptions.isEmpty()) {
            descriptions.add("No error description available.");
        }
        metadata.setErrorDescription(StringUtils.join(descriptions, "\n"));

        try {
            metadata.setOriginalMessage(originalMessage);

        } catch (IOException e) {
            logger.warn(String.format(
                    "Failed to serialize message %s: %s", originalMessage.getMetadata().getMessageID().get(), e.getMessage()), e);
            metadata.setOriginalMessage("Malformed message.");
        }

        return errorMessage;
    }

    private void sendMessageToErrorTopic(Message errorMessage) {
        try {
            logger.debug("Publishing error message {} to ERROR stream...", errorMessage.getMetadata().getMessageID().get());
            errorPublisher.publish(errorMessage);

        } catch (Exception e) {
            logger.error(String.format("Failed to publish error message %s to ERROR stream: %s",
                    errorMessage.getMetadata().getMessageID().get(), e.getMessage()), e);
        }
    }
}
