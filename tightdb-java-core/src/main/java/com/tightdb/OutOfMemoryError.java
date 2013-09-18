package com.tightdb;


/**
 * Can be thrown when tightdb runs out of memory. 
 * A JVM that catches this will be able to cleanup, e.g. release other resources
 * to avoid also running out of memory.
 *
 */
public class OutOfMemoryError extends Error {

    public OutOfMemoryError() { 
        super(); 
    }

    public OutOfMemoryError(String message) {
        super(message); 
    }

    public OutOfMemoryError(String message, Throwable cause) {
        super(message, cause); 
    }

    public OutOfMemoryError(Throwable cause) { 
        super(cause); 
    }
}
