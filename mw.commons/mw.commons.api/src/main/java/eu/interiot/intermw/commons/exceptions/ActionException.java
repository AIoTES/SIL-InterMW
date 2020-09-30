package eu.interiot.intermw.commons.exceptions;

/**
 * Exception to handle generic failures on the Actions requested in the internal
 * messaging. It has specializations {@link UnsupportedActionException} and {@link IllegalActionException}
 *
 * @author mllorente
 */
public class ActionException extends MiddlewareException {

    private static final long serialVersionUID = 6489671263500673229L;

    public ActionException(Integer code, String cause) {
        super(code, cause);
    }

    public ActionException(String cause) {
        super(cause);
    }

}
