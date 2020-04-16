package io.realm.mongodb.remote;

/**
 * The options to apply when updating documents.
 */
public class RemoteUpdateOptions {
    private boolean upsert;

    /**
     * Returns true if a new document should be inserted if there are no matches to the query filter.
     * The default is false.
     *
     * @return true if a new document should be inserted if there are no matches to the query filter
     */
    public boolean isUpsert() {
        return upsert;
    }

    /**
     * Set to true if a new document should be inserted if there are no matches to the query filter.
     *
     * @param upsert true if a new document should be inserted if there are no matches to the query
     *               filter.
     * @return this
     */
    public RemoteUpdateOptions upsert(final boolean upsert) {
        this.upsert = upsert;
        return this;
    }

    @Override
    public String toString() {
        return "RemoteUpdateOptions{"
                + "upsert=" + upsert
                + '}';
    }
}