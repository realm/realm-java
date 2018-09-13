package io.realm.sync;

import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.Index;
import io.realm.annotations.RealmClass;
import io.realm.annotations.RealmField;
import io.realm.annotations.Required;
import io.realm.internal.annotations.ObjectServer;

@ObjectServer
@RealmClass(name = "__ResultSets")
public class Subscription implements RealmModel {

    public enum State {
        /**
         * FIXME
         */
        ERROR,  // An error occurred while creating or processing the partial sync subscription.

        // CREATING(2), // Not supported in Java
        PENDING, // The subscription was created, but has not yet been processed by the sync server.

        /**
         * FIXME
         */
        COMPLETE, // The subscription has been processed by the sync server and data is being synced to the device.

        /**
         * FIXME
         */
        INVALIDATED; // The subscription has been removed.
    }

    @Index
    @Required
    private String name;

    private byte status;

    @Required
    @RealmField(name = "error_message")
    private String errorMessage;

    private String query;

    public String getName() {
        return name;
    }


    public <T extends RealmModel> RealmResults<T> getRemoteQueryResult(Class<T> queryClass) {
        return null;
    }

    public <T extends RealmModel> RealmResults<T> getLocalQueryResult(Class<T> queryClass) {
        return null;
    }

    public <T extends RealmModel> RealmResults<T> getRemoteQueryResultAsync(Class<T> queryClass) {
        return null;
    }

    public <T extends RealmModel> RealmResults<T> getLocalQueryResultAsync(Class<T> queryClass) {
        return null;
    }

    /**
     * FIXME
     *
     * @return
     */
    public State getState () {
        if (RealmObject.isValid(this)) {
            return State.INVALIDATED;
        } else {
            switch (status) {
                case -1:
                    return State.ERROR;
                case 0:
                    return State.PENDING;
                case 1:
                    return State.COMPLETE;
                default:
                    throw new IllegalArgumentException("Unknown subscription state value: " + status);
            }
        }
    }

    /**
     * FIXME
     *
     * @return
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * FIXME
     *
     * @throws IllegalStateException if the Realm is not in a write transaction.
     */
    public void unsubscribe() {
        RealmObject.deleteFromRealm(this);
    }
}
