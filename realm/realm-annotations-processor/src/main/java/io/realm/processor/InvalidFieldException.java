package io.realm.processor;

/**
 * Created by larsgrefer on 28.11.16.
 */
public class InvalidFieldException extends Exception {

    public InvalidFieldException(String s) {
        super(s);
    }

    public InvalidFieldException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
