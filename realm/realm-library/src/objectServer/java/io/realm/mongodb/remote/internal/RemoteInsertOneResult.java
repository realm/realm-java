package io.realm.mongodb.remote.internal;

import org.bson.BsonValue;

/**
 * The result of an insert one operation.
 */
public class RemoteInsertOneResult {

    private final BsonValue insertedId;

    /**
     * Constructs a result.
     *
     * @param insertedId the _id of the inserted document.
     */
    public RemoteInsertOneResult(final BsonValue insertedId) {
        this.insertedId = insertedId;
    }

    /**
     * Returns the _id of the inserted document.
     *
     * @return the _id of the inserted document.
     */
    public BsonValue getInsertedId() {
        return insertedId;
    }
}