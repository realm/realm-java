package io.realm.mongodb.remote.internal;

/**
 * The result of a delete operation.
 */
public class RemoteDeleteResult {

    private final long deletedCount;

    /**
     * Constructs a result.
     *
     * @param deletedCount the number of documents deleted.
     */
    public RemoteDeleteResult(final long deletedCount) {
        this.deletedCount = deletedCount;
    }

    /**
     * Gets the number of documents deleted.
     *
     * @return the number of documents deleted
     */
    public long getDeletedCount() {
        return deletedCount;
    }
}