package eu.interiot.intermw.commons.exceptions;

public class IllegalActionException extends ActionException {

    private static final long serialVersionUID = 4812742205575272690L;

    public IllegalActionException(String cause) {
        super(ErrorCode.ILLEGAL_ACTION_EXCEPTION.getErrorCode(), cause);
    }


}
