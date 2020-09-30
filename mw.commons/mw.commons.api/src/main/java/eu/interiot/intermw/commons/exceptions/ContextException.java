package eu.interiot.intermw.commons.exceptions;


/**
 * A {@link MiddlewareException} thrown when a MW component cannot be created
 *
 * @author <a href="mailto:flavio.fuart@xlab.si">Flavio Fuart</a>
 */
public class ContextException extends MiddlewareException {

    private static final long serialVersionUID = -3485859645458834245L;

    public ContextException(String message, Throwable e) {
        super(ErrorCode.CONTEXT_EXCEPTION.getErrorCode(), message, e);
    }

    /**
     * Creates a new instance
     *
     * @param message The error description
     * @see ErrorCode#CONTEXT_EXCEPTION
     */
    public ContextException(String message) {
        super(ErrorCode.CONTEXT_EXCEPTION.getErrorCode(), message);
    }
}
