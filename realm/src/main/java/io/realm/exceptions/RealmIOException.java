package io.realm.exceptions;

/**
 * Realm exception for reporting problems opening or creating Realm files.
 */
public class RealmIOException extends RuntimeException {

    public RealmIOException(Throwable cause) {
        super(cause);
    }

    public RealmIOException() {
    }

    public RealmIOException(String message) {
        super(message);
    }

    public RealmIOException(String message, Throwable cause) {
        super(message, cause);
    }

}
