package io.realm;

@SuppressWarnings("serial")
public class IllegalMixedTypeException extends RuntimeException {

    public IllegalMixedTypeException(Throwable cause) {
        super(cause);
    }

    public IllegalMixedTypeException() {
    }

    public IllegalMixedTypeException(String message) {
        super(message);
    }

    public IllegalMixedTypeException(String message, Throwable cause) {
        super(message, cause);
    }

}
