package eu.interiot.intermw.comm.commons.exceptions;

import eu.interiot.intermw.commons.exceptions.MiddlewareException;

/**
 * Base Middleware Comm Exception class
 *
 * @author <a href="mailto:flavio.fuart@xlab.si">Flavio Fuart</a>
 */
public class CommException extends MiddlewareException {

    private static final long serialVersionUID = -3116732623648886127L;

    /**
     * {@inheritDoc}
     */
    public CommException(Throwable exception) {
        super(exception);
    }

    /**
     * {@inheritDoc}
     */
    public CommException(String cause) {
        super(cause);
    }

    /**
     * {@inheritDoc}
     */
    public CommException(Integer code, Throwable e) {
        super(code, e);
    }

    /**
     * {@inheritDoc}
     */
    public CommException(String cause, Throwable e) {
        super(cause, e);
    }

    /**
     * {@inheritDoc}
     */
    public CommException(Integer code, String cause) {
        super(code, cause);
    }

    /**
     * {@inheritDoc}
     */
    public CommException(Integer code, String cause, Throwable e) {
        super(code, cause, e);
    }
}
