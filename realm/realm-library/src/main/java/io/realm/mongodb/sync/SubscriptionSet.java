package io.realm.mongodb.sync;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmQuery;
import io.realm.annotations.Beta;
import io.realm.internal.annotations.ObjectServer;
import io.realm.internal.objectstore.OsSubscriptionSet;

/**
 * A subscription set is an immutable view of all current {@link Subscription}'s for a given
 * Realm that has been configured for flexible sync.
 * <p>
 * A {@link Subscription} represents a specific query that is running against Realm on the server
 * and being synchronized with the device. The subscription set thus defines all the data that is
 * available to the device.
 * <p>
 * If a subscription is removed, so is the corresponding data, but it is only removed from the
 * device. It isn't deleted on the server.
 * <p>
 * It is possible to modify a subscription set while offline, but the set of subscriptions isn't
 * accepted before {@link #getState()} returns {@link SubscriptionSet.State#COMPLETE}.
 * <p>
 * It is possible to force the subscription set to be synchronized with the server by using
 * {@link #waitForSynchronization()} and its variants.
 *
 */
@ObjectServer
@Beta
public interface SubscriptionSet extends Iterable<Subscription> {

    /**
     * The possible states a subscription set can be in
     * TODO These are internal states, simplify public ones before release?
     */
    public enum State {
        UNCOMMITTED(OsSubscriptionSet.STATE_VALUE_UNCOMMITTED),
        PENDING(OsSubscriptionSet.STATE_VALUE_PENDING),
        BOOTSTRAPPING(OsSubscriptionSet.STATE_VALUE_BOOTSTRAPPING),
        COMPLETE(OsSubscriptionSet.STATE_VALUE_COMPLETE),
        ERROR(OsSubscriptionSet.STATE_VALUE_ERROR),
        SUPERCEDED(OsSubscriptionSet.STATE_VALUE_SUPERCEDED);

        private final byte value;

        State(byte nativeValue) {
            this.value = nativeValue;
        }

        public static State fromNativeValue(long value) {
            for (State state : values()) {
                if (state.value == value) {
                    return state;
                }
            }

            throw new IllegalArgumentException("Unknown SubscriptionSetState code: " + value);
        }
    }

    /**
     * Find the first subscription that matches the given query.
     *
     * @param query query to search for.
     * @return the subscription matching the query or {@code null} if no match was found.
     */
    @Nullable
    Subscription find(RealmQuery query);

    /**
     * Find the subscription with a given name.
     *
     * @param name name of subscription to search for.
     * @return the matching subscription or {@code null} if no subscription with that name was found.
     */
    @Nullable
    Subscription find(String name);

    /**
     * Returns the current state of the SubscriptionSet. See {@link SubscriptionSet.State} for more
     * details about each state.
     *
     * @return current state of the SubscriptionSet.
     */
    State getState();

    /**
     * Returns how many subscriptions are curently in this subscription set.
     *
     * @return the number of of subscriptions in the subscription set.
     */
    int size();

    /**
     * If {@link #getState()} returns {@link State#ERROR}, this method will return the reason.
     * Errors can be fixed by modifying the subscription accordingly and then calling
     * {@link #waitForSynchronization()}.
     *
     * @return the underlying error if the subscription set is in the {@link State#ERROR} state. For
     * all other states {@code null} will be returned.
     */
    @Nullable
    String getErrorMessage();

    /**
     * Wait for the subscription set to synchronize with the server. It will return when the
     * server either accepts the set of queries and have downloaded data for them, or if an
     * error has occurred. Note, that you will either need to manually call {@link Realm#refresh()}
     * or wait for change listeners to trigger to see the downloaded data.
     *
     * If an error occurred, the underlying reason can be found through {@link #getErrorMessage()}.
     *
     * @return {@code true} if all current subscriptions was accepted by the server and data has
     * been downloaded, or {@code false} if an error occurred.
     */
    boolean waitForSynchronization();

    /**
     * Wait for the subscription set to synchronize with the server. It will return when the
     * server either accepts the set of queries and have downloaded data for them, or if an
     * error has occurred. Note, that you will either need to manually call {@link Realm#refresh()}
     * or wait for change listeners to trigger to see the downloaded data.
     *
     * If an error occurred, the underlying reason can be found through {@link #getErrorMessage()}.
     *
     * @param timeOut how long to wait for the synchronization to either succeed or fail.
     * @param unit unit of time used for the timeout.
     * @return {@code true} if all current subscriptions was accepted by the server and data has
     * been downloaded, or {@code false} if an error occurred.
     * @throws RuntimeException if the timeout is exceeded.
     */
    boolean waitForSynchronization(Long timeOut, TimeUnit unit);

    /**
     * Asynchronously wait for the subscription set to synchronize with the server. It will return when the
     * server either accepts the set of queries and have downloaded data for them, or if an
     * error has occurred. Note, that you will either need to manually call {@link Realm#refresh()}
     * or wait for change listeners to trigger to see the downloaded data.
     *
     * If an error occurred, the underlying reason can be found through {@link #getErrorMessage()}.
     *
     * @param callback callback to trigger when the synchronization either succeed or fail. Results
     *                 will be reported on the UI thread.
     * @return {@code true} if all current subscriptions was accepted by the server and data has
     * been downloaded, or {@code false} if an error occurred.
     */
    RealmAsyncTask waitForSynchronizationAsync(Callback callback);

    /**
     * Asynchronously wait for the subscription set to synchronize with the server. It will return when the
     * server either accepts the set of queries and have downloaded data for them, or if an
     * error has occurred. Note, that you will either need to manually call {@link Realm#refresh()}
     * or wait for change listeners to trigger to see the downloaded data.
     *
     * If an error occurred, the underlying reason can be found through {@link #getErrorMessage()}.
     *
     * @param timeOut how long to wait for the synchronization to either succeed or fail.
     * @param unit unit of time used for the timeout.
     * @param callback callback to trigger when the synchronization either succeed or fail. Results
     *                 will be reported on the UI thread.
     * @return {@code true} if all current subscriptions was accepted by the server and data has
     * been downloaded, or {@code false} if an error occurred.
     */
    RealmAsyncTask waitForSynchronizationAsync(Long timeOut, TimeUnit unit, Callback callback);

    /**
     * Modify the subscription set. If an exception is thrown during the update, no changes will be
     * applied. If the update succeed, this subscription set is updated with the modified state.
     *
     * @param action the block that modifies the subscription set. It will run on the caller thread.
     * @return this subscription set, that now has been updated.
     * @throws Exception any exception thrown during the update, will propagate back.
     */
    SubscriptionSet update(UpdateCallback action);

    /**
     * Asynchronously modify the subscription set. If an exception is thrown during the update, no
     * changes will be applied.
     * *
     * @param callback callback that controls the asynct ask. Succces or failure will be reported here.
     * @return task controlling the async execution.
     */
    RealmAsyncTask updateAsync(UpdateAsyncCallback callback);

    /**
     * Refresh the subscription set and its state.
     */
    void refresh();

    /**
     * Interface used when modifying a subscription set. See {@link #update(UpdateCallback)} and
     * {@link #updateAsync(UpdateAsyncCallback)}.
     */
    interface UpdateCallback {
        /**
         * Updates the current subscription set by passing in a version of it that can be modified.
         *
         * If an exception is throwing during the update, all changes will be rolled back.
         *
         * @param subscriptions a modifiable version of the subscription set.
         */
        void update(MutableSubscriptionSet subscriptions);
    }

    /**
     * Callback used when asynchronously updating a subscription set.
     *
     * If an exception is throwing during the update, all changes will be rolled back and the
     * exception will be reported in {@code onError()}.
     */
    interface UpdateAsyncCallback extends UpdateCallback {
        void onSuccess(SubscriptionSet subscriptions);
        void onError(Throwable exception);
    }

    /**
     * Callback used when asynchronously waiting for the server to process the subscription set.
     * When the server either succeed or fail to apply the subscription set, the result is returned
     * in {@code onStateChange}. This include errors from the server.
     *
     * If a local exception is thrown, it is reported through on {@code onError()}.
     */
    interface Callback {
        void onStateChange(SubscriptionSet subscriptions);
        void onError(Throwable e);
    }
}
