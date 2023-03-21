package io.realm.mongodb.sync;

import java.util.Date;

import javax.annotation.Nullable;

import io.realm.RealmQuery;
import io.realm.annotations.Beta;
import io.realm.internal.Util;
import io.realm.internal.annotations.ObjectServer;
import io.realm.internal.UnmanagedSubscription;

/**
 * A subscription defines a specific server query and its metadata. The result of this query
 * is continuously being synchronized with the device as long as the subscription is part of a
 * {@link SubscriptionSet} with a state of {@link SubscriptionSet.State#COMPLETE}.
 *
 * Subscriptions are immutable once created, but they can be updated by using a
 * {@link MutableSubscriptionSet#addOrUpdate(Subscription)}.
 */
@ObjectServer
@Beta
public interface Subscription {

    /**
     * Create an unmanaged named subscription for a flexible sync enabled Realm.
     * The subscription will not take effect until it has been stored using either
     * {@link MutableSubscriptionSet#add(Subscription)} or
     * {@link MutableSubscriptionSet#addOrUpdate(Subscription)}.
     *
     * @param name the name of the subscription
     * @param query the query that is subscribed to. Note, subscription queries have
     *              restrictions compared to normal queries.
     * @return the unmanaged subscription.
     */
    static Subscription create(String name, RealmQuery query) {
        Util.checkEmpty(name, "name");
        return new UnmanagedSubscription(name, query);
    }

    /**
     * Create an unmanaged anonymous subscription for a flexible sync enabled Realm.
     * The subscription will not take effect until it has been stored using either
     * {@link MutableSubscriptionSet#add(Subscription)} or
     * {@link MutableSubscriptionSet#addOrUpdate(Subscription)}.
     *
     * @param query the query that is subscribed to. Note, subscription queries have
     *              restrictions compared to normal queries.
     * @return the unmanaged subscription.
     */
    static Subscription create(RealmQuery query) {
        return new UnmanagedSubscription(null, query);
    }

    /**
     * Returns the timestamp for when this subscription was persisted. This will
     * return {@code null} until the Subscription has been added using either
     * {@link MutableSubscriptionSet#add(Subscription)} or
     * {@link MutableSubscriptionSet#addOrUpdate(Subscription)}.
     *
     * @return the time this subscription was persisted, or {@code null} if the
     * subscription hasn't been persisted yet.
     */
    @Nullable
    Date getCreatedAt();

    /**
     * Returns the timestamp for when a persisted subscription was updated. This will
     * return {@code null} until the Subscription has been added using either
     * {@link MutableSubscriptionSet#add(Subscription)} or
     * {@link MutableSubscriptionSet#addOrUpdate(Subscription)}.
     *
     * @return the time this subscription was updated, or {@code null} if the
     * subscription hasn't been persisted yet.
     */
    @Nullable
    Date getUpdatedAt();

    /**
     * Returns the name of subscription or {@code null} if no name was defined.
     *
     * @return the name of the subscription.
     */
    @Nullable
    String getName();

    /**
     * Returns the type that is being queried.
     *
     * @return the type that is being queried.
     */
    String getObjectType();

    /**
     * Returns the subscription query that is running on objects of type
     * {@link #getObjectType()}.
     *
     * @return the query covered by this subscription.
     */
    String getQuery();
}
