/*
 * Copyright 2018 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.realm.sync;

import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.annotations.Beta;
import io.realm.annotations.Index;
import io.realm.annotations.RealmClass;
import io.realm.annotations.RealmField;
import io.realm.annotations.Required;
import io.realm.internal.annotations.ObjectServer;

/**
 * Subscriptions represents the data from the server that a device is interested in when using
 * Query-based Realms.
 * <p>
 * They are created automatically when using {@link RealmQuery#findAllAsync()} or {@link RealmQuery#findAllAsync(String)}
 * on those Realms, but can also be created manually using {@link RealmQuery#subscribe()} and {@link RealmQuery#subscribe(String)}.
 * <p>
 * As long as any subscription exist that include an object, that object will be present on the
 * device. If an object is not covered by an active subscription it will be removed from the device,
 * but not the server.
 * <p>
 * Subscriptions are Realm objects, so deleting them e.g. by calling {@link RealmObject#deleteFromRealm()},
 * is the same as calling {@link #unsubscribe()}.
 * <p>
 * <b>Warning:</b> Instances of this class should never be created directly through
 * {@link io.realm.Realm#createObject(Class)} but only by using {@link RealmQuery#subscribe()} or
 * {@link RealmQuery#subscribe(String)}.
 */
@ObjectServer
@RealmClass(name = "__ResultSets")
@Beta
public class Subscription extends RealmObject {

    /**
     * The different states a Subscription can be in.
     */
    public enum State {
        /**
         * An error occurred while creating or processing the subscription.
         * See {@link #getErrorMessage()} for details on what went wrong.
         */
        ERROR((byte) -1),

        /**
         * The subscription has been created, but has not yet been processed by the sync
         * server.
         */
        PENDING((byte) 0),

        /**
         * The subscription has been processed by the Realm Object Server and data is being synced
         * to the device.
         */
        ACTIVE((byte) 1),

        /**
         * The subscription has been removed. Data is no longer being synchronized from the Realm
         * Object Server, and the objects covered by this subscription might be deleted from the
         * device if no other subscriptions include them.
         */
        INVALIDATED(null);


        private final Byte nativeValue;

        State(Byte nativeValue) {
            this.nativeValue = nativeValue;
        }

        /**
         * Returns the native value representing this state.
         *
         * @return the native value representing this state.
         */
        public Byte getValue() {
            return nativeValue;
        }
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
        this.status = 0;
        this.errorMessage = "";
        this.matchesProperty = "";
    }

    @Index
    @Required
    private String name;

    /**
     * The underlying representation of the State
     */
    private byte status;

    @Required
    @RealmField("error_message")
    private String errorMessage;

    @Required
    @RealmField("matches_property")
    private String matchesProperty;

    @Required
    private String query;

    @RealmField("query_parse_counter")
    private int queryParseCounter;

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
     *
     * @return a textual description of the query.
     */
    public String getQueryDescription() {
        return query;
    }

    /**
     * Returns the internal name of the Class being queried.
     *
     * @return the internal name of the of the class being queried.
     */
    public String getQueryClassName() {
        // Strip the __matches suffix to end up with the class being queried.
        String classQueried = matchesProperty;
        return classQueried.substring(0, classQueried.length() - "_matches".length());
    }

    /**
     * Returns the state of the subscription
     *
     * @return the state of the subscription.
     * @see State
     */
    public State getState () {
        if (!RealmObject.isValid(this)) {
            return State.INVALIDATED;
        } else {
            switch (status) {
                case -1:
                    return State.ERROR;
                case 0:
                    return State.PENDING;
                case 1:
                    return State.ACTIVE;
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
     * Cancels the subscription. After this, if the objects covered by the subscription are not
     * part of any other subscription, they will be removed locally from the device (but not on the
     * server).
     * <p>
     * The effect of unsubscribing is not immediate. The local Realm must coordinate with the Realm
     * Object Server before it can happen. When it happens, any objects removed will trigger a standard
     * change notification, and from the perspective of the device it will look like they where
     * deleted.
     * <p>
     * Calling this method is the equivalent of calling {@link RealmObject#deleteFromRealm()}.
     *
     * @throws IllegalStateException if the Realm is not in a write transaction.
     */
    public void unsubscribe() {
        RealmObject.deleteFromRealm(this);
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "name='" + name + '\'' +
                ", status=" + getState().toString() +
                ", errorMessage='" + errorMessage + '\'' +
                ", className='" + getQueryClassName() + '\'' +
                ", query='" + query + '\'' +
                '}';
    }
}
