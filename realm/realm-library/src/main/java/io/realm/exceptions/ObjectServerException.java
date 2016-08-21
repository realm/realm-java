package io.realm.exceptions;

public class ObjectServerException extends RealmException {


    public ObjectServerException(Error error, String detailMessage) {
        super(error.toString() + " -> " + detailMessage);
    }

    public ObjectServerException(Error error, String detailMessage, Throwable exception) {
        super(error.toString() + " -> " + detailMessage, exception);
    }
}
