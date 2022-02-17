package io.realm.mongodb.sync;

import io.realm.RealmModel;
import io.realm.RealmQuery;
import io.realm.annotations.Beta;
import io.realm.internal.annotations.ObjectServer;

/**
 * A mutable subscription set is available when calling {@link SubscriptionSet#update(UpdateCallback)}
 *
 * This is the only way to modify a {@link SubscriptionSet}.
 *
 * {@link Subscription}s can be either <i>managed</i> or <i>unmanaged</i>. Unmanaged subscriptions
 * are those created by using {@link Subscription#create(RealmQuery)} or
 * {@link Subscription#create(String, RealmQuery)}, while managed subscriptions are the ones being
 * returned from the subscription set.
 *
 * @see SubscriptionSet for more information about subscription sets and flexible sync.
 */
@ObjectServer
@Beta
public interface MutableSubscriptionSet extends SubscriptionSet {

    /**
     * Adds a new unmanaged subscription to the subscription set.
     *
     * @param subscription unmanaged subscription to add.
     * @return the newly added managed subscription.
     * @throws IllegalArgumentException if a subscription matching the provided one already exists.
     */
     Subscription add(Subscription subscription);

    /**
     * Add a new subscription or update an existing named subscription. It isn't possible to update
     * an anonymous subscription. These must removed and re-inserted.
     *
     * @param subscription anonymous or named subscription created via {@code Subscription.create(...)},
     *                     used to update a matching one within a specific set. It creates a new
     *                     one in case there is no match..
     * @return the updated or inserted managed subscription.
     */
    Subscription addOrUpdate(Subscription subscription);

    /**
     * Remove a managed subscription.
     *
     * @param subscription managed subscription to remove
     * @return {@code true} if the subscription was removed, {@code false} if not.
     * @throws IllegalArgumentException if the subscription provided is unmanaged. Only managed
     * subscriptions can be used as input.
     */
    boolean remove(Subscription subscription);

    /**
     * Remove a named subscription.
     *
     * @param name name of managed subscription to remove.
     * @return {@code true} if the subscription was removed, {@code false} if not.
     */
    boolean remove(String name);

    /**
     * Remove all subscriptions on a given {@link Subscription#getObjectType()}.
     *
     * @param objectType subscriptions on this object type will be removed.
     * @return {@code true} if 1 or more subscriptions were removed, {@code false} if no
     * subscriptions were removed.
     */
    boolean removeAll(String objectType);

    /**
     * Remove all subscriptions with queries on a given given model class.
     *
     * @param clazz subscriptions on this type will be removed.
     * @return {@code true} if 1 or more subscriptions were removed, {@code false} if no
     * subscriptions were removed.
     */
    <T extends RealmModel> boolean removeAll(Class<T> clazz);

    /**
     * Remove all current managed subscriptions.
     *
     * @return {@code true} if 1 or more subscriptions were removed, {@code false} if no
     * subscriptions were removed.
     */
    boolean removeAll();
}
