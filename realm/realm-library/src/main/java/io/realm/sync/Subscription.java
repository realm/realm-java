package io.realm.sync;

import javax.annotation.Nonnull;

import io.reactivex.annotations.NonNull;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.annotations.RealmClass;
import io.realm.annotations.Required;
import io.realm.internal.annotations.ObjectServer;

@ObjectServer
@RealmClass(name = "__ResultSets")
public class Subscription<T extends RealmModel> implements RealmModel {
    @Required
    private String name;
    private int status;
    @Required
    private String error_message;

    /**
     * FIXME
     */
    public enum State {
        /**
         * FIXME
         */
        PENDING(0),

        /**
         * FIXME
         */
        COMPLETE(1),

        /**
         * FIXME
         */
        ERROR(-1),

        /**
         * FIXME
         */
        INVALIDATED(3);

        private final int nativeValue;

        State(int nativeValue) {
            this.nativeValue = nativeValue;
        }

        State fromNativeValue(int val) {
            for (State state : values()) {
                if (state.nativeValue == val) {
                    return state;
                }
            }
            throw new IllegalArgumentException("Unknown state value: " + val);
        }
    }

    /**
     * FIXME
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * FIXME
     */
    @Nonnull
    public State getState() {
        if (!RealmObject.isValid(this)) {
            return State.INVALIDATED;
        } else {
            switch (status) {
                case 0: return State.PENDING;
                case 1: return State.COMPLETE;
                case -1: return State.ERROR;
                default:
                    throw new IllegalStateException("Unknown state: " + status);
            }
        }
    }

    /**
     *
     * @return
     */
    @Nonnull
    public String getErrorMessage() {
        return error_message;
    }

    public <T extends RealmModel> RealmQuery<T> getQuery(T clazz) {
        return null;
    }

    public RealmResults<T> getRemoteQueryResult(T clazz) {
        return null;
    }

    public RealmResults<T> getLocalQueryResult(T clazz) {
        return null;
    }
}
