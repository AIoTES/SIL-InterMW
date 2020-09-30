package eu.interiot.intermw.api.exception;

public class NotAuthorizedException extends Exception {

    public NotAuthorizedException() {
    }

    public NotAuthorizedException(String message) {
        super(message);
    }
}
