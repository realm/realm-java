package com.tightdb.exceptions;


/**
 * 
 * This exception extends RuntimeException, so the developer avoids handling exception each time a value is added to a table.
 *
 */
public class IllegalTypeException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID = 9217711732691577905L;
    
    
    
    public IllegalTypeException(String message) {
        super(message);
    }

    public IllegalTypeException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
