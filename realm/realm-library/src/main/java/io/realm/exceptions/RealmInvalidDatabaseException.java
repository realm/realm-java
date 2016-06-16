package io.realm.exceptions;

import io.realm.internal.Keep;

/**
 * {@code RealmInvalidDatabaseException} is thrown when opening a corrupted Realm file, a non-Realm file, a Realm file
 * created by a newer version of Realm, or an encrypted Realm file with a wrong key.
 */
@Keep
public class RealmInvalidDatabaseException extends RuntimeException {
    public RealmInvalidDatabaseException(String detailMessage) {
        super(detailMessage);
    }
}
