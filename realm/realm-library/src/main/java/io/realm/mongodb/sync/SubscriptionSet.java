package io.realm.mongodb.sync;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmQuery;
import io.realm.annotations.Beta;
import io.realm.internal.Keep;
import io.realm.internal.annotations.ObjectServer;
import io.realm.internal.objectstore.OsSubscriptionSet;

/**
 * A subscription set is an immutable view of all current {@link Subscription}s for a given
 * Realm that has been configured for flexible sync.
 * <p>
 * Flexible Sync is a way of defining which data gets synchronized to and from the device using
 * {@link RealmQuery}s. The query and its metadata are represented by a {@link Subscription}.
 * <p>
 * A subscription set thus defines all the data that is available to the device and being
 * synchronized with the server. If the subscription set encounters an error, e.g. by containing an
 * invalid query, the entire subscription set will enter an {@link SubscriptionSet.State#ERROR}
 * state, and no synchronization will happen until the error has been fixed.
 * <p>
 * If a subscription is removed, so is the corresponding data, but it is only removed from the
 * device. It isn't deleted on the server.
 * <p>
 * It is possible to modify a subscription set while offline, but modification isn't
 * accepted by the server before {@link #getState()} returns {@link SubscriptionSet.State#COMPLETE}.
 * <p>
 * It is possible to force the subscription set to be synchronized with the server by using
 * {@link #waitForSynchronization()} and its variants.
 *
 */
@Beta
@Keep
public interface SubscriptionSet extends Iterable<Subscription> {

    /**
     * The possible states a subscription set can be in.
     */
    public enum State {
        /**
         * The initial state of subscriptions when opening a new Realm or when entering a
         * {@link #update(UpdateCallback)}.
         */
        UNCOMMITTED(OsSubscriptionSet.STATE_VALUE_UNCOMMITTED),

        /**
         * A subscription set has been modified locally, but is still waiting to be sent to the
         * server.
         */
        PENDING(OsSubscriptionSet.STATE_VALUE_PENDING),

        /**
         * A subscription set was accepted by the server and initial data is being sent to the
         * device.
         */
        BOOTSTRAPPING(OsSubscriptionSet.STATE_VALUE_BOOTSTRAPPING),

        /**
         * A subscription set is actively synchronizing data between the server and the device.
         */
        COMPLETE(OsSubscriptionSet.STATE_VALUE_COMPLETE),

        /**
         * An error occurred in the subscription set or one of the subscriptions. The cause is
         * found in {@link #getErrorMessage()}.
         */
        ERROR(OsSubscriptionSet.STATE_VALUE_ERROR),

        /**
         * Another subscription set was stored before this one, the changes made to this set
         * are ignorde by the server. Get the latest subscription set by calling
         * {@link Realm#getSubscriptions()}.
         */
        SUPERSEDED(OsSubscriptionSet.STATE_VALUE_SUPERSEDED),

        /**
         * The last initialization message containing the initial state for this subscription set
         * has been received. The client is awaiting a mark message to mark this subscription as
         * fully caught up to history.
         */
        AWAITING_MARK(OsSubscriptionSet.STATE_VALUE_AWAITING_MARK);

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
     * Find the first subscription that contains the given query. It is possible for multiple
     * named subscriptions to contain the same query.
     *
     * @param query query to search for.
     * @return the first subscription containing the query or {@code null} if no match was found.
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
     * Returns how many subscriptions are currently in this subscription set.
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
     * server either accepts the set of queries and has downloaded data for them, or if an
     * error has occurred. Note, that you will either need to manually call {@link Realm#refresh()}
     * or wait for change listeners to trigger to see the downloaded data.
     *
     * If an error occurred, the underlying reason can be found through {@link #getErrorMessage()}.
     *
     * @return {@code true} if all current subscriptions were accepted by the server and data has
     * been downloaded, or {@code false} if an error occurred.
     */
    boolean waitForSynchronization();

    /**
     * Wait for the subscription set to synchronize with the server. It will return when the
     * server either accepts the set of queries and has downloaded data for them, or if an
     * error has occurred. Note, that you will either need to manually call {@link Realm#refresh()}
     * or wait for change listeners to trigger to see the downloaded data.
     *
     * If an error occurred, the underlying reason can be found through {@link #getErrorMessage()}.
     *
     * @param timeOut how long to wait for the synchronization to either succeed or fail.
     * @param unit unit of time used for the timeout.
     * @return {@code true} if all current subscriptions were accepted by the server and data has
     * been downloaded, or {@code false} if an error occurred.
     * @throws RuntimeException if the timeout is exceeded.
     */
    boolean waitForSynchronization(Long timeOut, TimeUnit unit);

    /**
     * Asynchronously wait for the subscription set to synchronize with the server. It will invoke
     * the callback when the server either accepts the set of queries and has downloaded data for
     * them, or if an error has occurred. Note, that you will either need to manually call
     * {@link Realm#refresh()} or wait for change listeners to trigger to see the downloaded data.
     *
     * If an error occurred, the underlying reason can be found through {@link #getErrorMessage()}.
     *
     * @param callback callback to trigger when the synchronization either succeed or fail. Results
     *                 will be reported on the UI thread.
     * @return {@code true} if all current subscriptions were accepted by the server and data has
     * been downloaded, or {@code false} if an error occurred.
     */
    RealmAsyncTask waitForSynchronizationAsync(StateChangeCallback callback);

    /**
     * Asynchronously wait for the subscription set to synchronize with the server. The callback is
     * invoked when the server either accepts the set of queries and has downloaded data for them,
     * or if an error has occurred. Note, that you will either need to manually call
     * {@link Realm#refresh()} or wait for change listeners to trigger to see the downloaded data.
     *
     * If an error occurred, the underlying reason can be found through {@link #getErrorMessage()}.
     *
     * @param timeOut how long to wait for the synchronization to either succeed or fail.
     * @param unit unit of time used for the timeout.
     * @param callback callback to trigger when the synchronization either succeed or fail. Results
     *                 will be reported on the UI thread.
     * @return {@code true} if all current subscriptions were accepted by the server and data has
     * been downloaded, or {@code false} if an error occurred.
     */
    RealmAsyncTask waitForSynchronizationAsync(Long timeOut, TimeUnit unit, StateChangeCallback callback);

    /**
     * Modify the subscription set. If an exception is thrown during the update, no changes will be
     * applied. If the update succeeds, this subscription set is updated with the modified state.
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
    @Keep
    interface StateChangeCallback {
        void onStateChange(SubscriptionSet subscriptions);
        void onError(Throwable e);
    }
}
