package io.realm.mongodb.sync;


import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.realm.RealmQuery;
import io.realm.annotations.Beta;
import io.realm.internal.annotations.ObjectServer;
import io.realm.internal.objectstore.OsSubscriptionSet;

@ObjectServer
@Beta
public interface SubscriptionSet extends Iterable<Subscription> {

    /**
     * TODO
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

    @Nullable
    Subscription find(RealmQuery query);

    @Nullable
    Subscription findByName(String name);

    State getState();

    int size();

    @Nullable
    String getErrorMessage();

    boolean waitForSynchronization();

//    boolean waitForSynchronization(Long timeOut, TimeUnit unit);

    SubscriptionSet update(UpdateCallback action);

    void refresh();


    //
//    // Wait for Synchronization to happen. True = Success, false = Error
//    @Throws(AppException::class)
//    suspend fun waitForSynchronization(timeout: Long = Long.MAX_VALUE, unit: TimeUnit = TimeUnit.SECONDS)
//
//    // Update existing subscriptions
//    suspend fun update(action: suspend MutableSubscriptionSet.(Realm) -> Unit): SubscriptionSet

    interface UpdateCallback {
        void update(MutableSubscriptionSet subscriptions);
    }
}
