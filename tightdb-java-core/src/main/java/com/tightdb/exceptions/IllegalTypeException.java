package com.tightdb.exceptions;


/**
 * 
 * This exception extends RuntimeException, so the developer avoids handling exception each time a value is added to a table.
 *
 */
@SuppressWarnings("serial")
public class IllegalTypeException extends RuntimeException{

    
    
    public IllegalTypeException(String message) {
        super(message);
    }

    public IllegalTypeException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
