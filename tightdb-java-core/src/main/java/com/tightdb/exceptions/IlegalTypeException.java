package com.tightdb.exceptions;

public class IlegalTypeException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID = 9217711732691577905L;
    
    
    
    public IlegalTypeException(String message) {
        super(message);
    }

    public IlegalTypeException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
