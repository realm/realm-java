package io.realm;

import java.util.concurrent.Future;

/**
 * Represents the result of an asynchronous Realm query.
 *
 * Users are responsible of maintaining a reference to {@code AsyncRealmQueryResult} in order
 * to call #cancel in case of a configuration change for example (to avoid memory leak, as the
 * query will post the result to the caller's thread callback)
 */
public class AsyncRealmQueryResult {
    final Future<?> pendingQuery;

    public AsyncRealmQueryResult(Future<?> pendingQuery) {
        this.pendingQuery = pendingQuery;
    }

    /**
     * Attempts to cancel execution of this queries.
     */
    public void cancel () {
        pendingQuery.cancel(true);
    }
}
