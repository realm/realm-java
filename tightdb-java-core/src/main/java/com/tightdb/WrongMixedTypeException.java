package com.tightdb;

@SuppressWarnings("serial")
public class WrongMixedTypeException extends RuntimeException {

    public WrongMixedTypeException(Throwable cause) {
        super(cause);
    }

    public WrongMixedTypeException() {
    }

    public WrongMixedTypeException(String message) {
        super(message);
    }

    public WrongMixedTypeException(String message, Throwable cause) {
        super(message, cause);
    }

}
