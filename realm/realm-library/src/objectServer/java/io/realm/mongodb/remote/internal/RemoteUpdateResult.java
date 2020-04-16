package io.realm.mongodb.remote.internal;

import javax.annotation.Nullable;

import org.bson.BsonValue;

/**
 * The result of an update operation.
 */
public class RemoteUpdateResult {

    private final long matchedCount;
    private final long modifiedCount;
    private final BsonValue upsertedId;

    /**
     * Constructs a result.
     *
     * @param matchedCount  the number of documents matched by the query.
     * @param modifiedCount the number of documents modified.
     * @param upsertedId    the _id of the inserted document if the replace resulted in an inserted
     *                      document, otherwise null.
     */
    public RemoteUpdateResult(
            final long matchedCount,
            final long modifiedCount,
            final BsonValue upsertedId
    ) {
        this.matchedCount = matchedCount;
        this.modifiedCount = modifiedCount;
        this.upsertedId = upsertedId;
    }

    /**
     * Returns the number of documents matched by the query.
     *
     * @return the number of documents matched.
     */
    public long getMatchedCount() {
        return matchedCount;
    }

    /**
     * Returns the number of documents modified.
     *
     * @return the number of documents modified.
     */
    public long getModifiedCount() {
        return modifiedCount;
    }

    /**
     * If the replace resulted in an inserted document, gets the _id of the inserted document,
     * otherwise null.
     *
     * @return if the replace resulted in an inserted document, the _id of the inserted document,
     * otherwise null.
     */
    @Nullable
    public BsonValue getUpsertedId() {
        return upsertedId;
    }
}