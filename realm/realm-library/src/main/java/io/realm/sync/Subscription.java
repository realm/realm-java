package io.realm.sync;

import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.annotations.Index;
import io.realm.annotations.RealmClass;
import io.realm.annotations.RealmField;
import io.realm.annotations.Required;
import io.realm.internal.annotations.ObjectServer;

/**
 * Subscriptions represent the data that a device is interested in from the server when using
 * Query-based Realms.
 * <p>
 * They are created automatically when using {@link RealmQuery#findAllAsync()} or {@link RealmQuery#findAllAsync(String)}
 * on those Realms, but can also be created manually.
 * <p>
 * As long as a any subscription exists that include a Object, that object will be present on the
 * device. If an object is not covered by any subscription it will be removed from the device,
 * but not the server.
 * <p>
 * Subscriptions are Realm objects, so deleting them by e.g. calling {@link RealmObject#deleteFromRealm()},
 * is the same as calling {@link #unsubscribe()}.
 */
@ObjectServer
@RealmClass(name = "__ResultSets")
public class Subscription implements RealmModel {

    /**
     * The different states a Subscription can be in.
     */
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

    public Subscription() {
        // Required by Realm.
    }

    /**
     * Creates a unmanaged named subscription from a {@link RealmQuery}.
     * This will not take effect until it has been added to the Realm.
     *
     * @param name name of the query.
     * @param query the query to turn into a subscription.
     */
    public Subscription(String name, RealmQuery<?> query) {
        this.name = name;
        this.query = query.getDescription();
    }

    @Index
    @Required
    private String name;

    private byte status;

    @Required
    @RealmField(name = "error_message")
    private String errorMessage;

    private String query;

    /**
     * Returns the name of the subscription.
     *
     * @return the name of the subscription.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a textual description of the query that created this subscription.
     * @return
     */
    public String getQueryDescription() {
        return query;
    }

    /**
     * Returns the state of the subscription
     *
     * @return the state of the subscription.
     * @see State
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
     * Returns the error message if {@link #getState()} returned {@link State#ERROR}, otherwise
     * the empty string is returned.
     *
     * @return the error string if the subscription encountered an error.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Cancels the subscription. If after this, some objects in the Realm are no longer part of any
     * active subscription they will be removed locally from the device (but not on the server).
     * <p>
     * The effect of unsubscribing is not immediate. The local Realm must coordinate with the Realm
     * Object Server before it can happen. When it happens, any objects removed due to unsubscribing
     * will trigger a standard change notification, and from the perspective of the device it will
     * look like the data was deleted.
     *
     * @throws IllegalStateException if the Realm is not in a write transaction.
     */
    public void unsubscribe() {
        RealmObject.deleteFromRealm(this);
    }
}
