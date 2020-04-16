package io.realm.mongodb.remote.internal;

import java.util.Map;

import org.bson.BsonValue;

/**
 * The result of an insert many operation.
 */
public class RemoteInsertManyResult {

    private final Map<Long, BsonValue> insertedIds;

    /**
     * Constructs a result.
     *
     * @param insertedIds the _ids of the inserted documents arranged by the index of the document
     *                    from the operation and its corresponding id.
     */
    public RemoteInsertManyResult(final Map<Long, BsonValue> insertedIds) {
        this.insertedIds = insertedIds;
    }

    /**
     * Returns the _ids of the inserted documents arranged by the index of the document from the
     * operation and its corresponding id.
     *
     * @return the _ids of the inserted documents arranged by the index of the document from the
     * operation and its corresponding id.
     */
    public Map<Long, BsonValue> getInsertedIds() {
        return insertedIds;
    }
}