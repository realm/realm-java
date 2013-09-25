package com.tightdb;

@SuppressWarnings("serial")
public class IOException extends RuntimeException {

    public IOException(Throwable cause) {
        super(cause);
    }

    public IOException() {
    }

    public IOException(String message) {
        super(message);
    }

    public IOException(String message, Throwable cause) {
        super(message, cause);
    }

}
