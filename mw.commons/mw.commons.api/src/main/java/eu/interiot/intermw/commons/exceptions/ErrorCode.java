package eu.interiot.intermw.commons.exceptions;


/**
 * An {@link Enum} for error codes used in {@link MiddlewareException} instances
 * and subclasses
 * <p>
 * Reserved {@link ErrorCode} for the comm API, excluding broker, go from 1000 to 1999
 *
 * @author <a href="mailto:flavio.fuart@xlab.si">Flavio Fuart</a>
 */
public enum ErrorCode {
    NO_CONFIG(1, "No configuration registered"),
    NO_CONFIG_PROPERTY(2, "A mandatory configuration property is missing: "),
    CONTEXT_EXCEPTION(4,
            "There is an error while creating the context. Please review your client configuration. Error description: "),
    UNSUPPORTED_ACTION_EXCEPTION(5, "The requested action is legal but currently not supported"),
    ILLEGAL_ACTION_EXCEPTION(6, "The requested action is not legal for the component adressed"),
    UNKNOWN_ACTION_EXCEPTION(7, "The action requested is labelled as unknown and thus is not processable by the component"),

    CANNOT_CREATE_COMPONENT(8, "Cannot create the desired component"),
    CANNOT_DESTROY_COMPONENT(9, "Cannot destroy the desired component"),

    CANNOT_PUBLISH_MESSAGE_UPSTREAM(10, "Cannot publish the message to the desired component going upstream"),
    CANNOT_PUBLISH_MESSAGE_DOWNSTREAM(11, "Cannot publish the message to the desired component going downstream"),
    CANNOT_PUBLISH_MESSAGE_TO_SERVICES(12, "Cannot publish the message to MW2MWM services"),

    PLATFORMID_URI_NOT_VALID(13, "Cannot publish the message to the desired component going right"),

    ERROR_HANDLING_RECEIVED_MESSAGE(14, "Error while handling received message");

    /**
     * The unique ID for the error code
     */
    private Integer errorCode;

    /**
     * A description for the error code
     */
    private String errorDescription;

    private ErrorCode(Integer errorCode, String errorDescription) {
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

}
