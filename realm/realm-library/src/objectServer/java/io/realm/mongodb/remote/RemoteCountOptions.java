package io.realm.mongodb.remote;

/**
 * The options for a count operation.
 */
public class RemoteCountOptions {
    private int limit;

    /**
     * Gets the limit to apply.  The default is 0, which means there is no limit.
     *
     * @return the limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Sets the limit to apply.
     *
     * @param limit the limit
     * @return this
     */
    public RemoteCountOptions limit(final int limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public String toString() {
        return "RemoteCountOptions{"
                + "limit=" + limit
                + '}';
    }
}