package io.realm.mongodb.sync;

import io.realm.RealmModel;
import io.realm.annotations.Beta;
import io.realm.internal.annotations.ObjectServer;

@ObjectServer
@Beta
public interface MutableSubscriptionSet extends SubscriptionSet {

    /**
     *
     * @param subscription
     * @return
     */
     Subscription add(Subscription subscription);

    /**
     * TODO
     * @param subscription
     * @return
     */
    Subscription addOrUpdate(Subscription subscription);

    /**
     *
     * @param subscription
     * @return
     */
    boolean remove(Subscription subscription);

    /**
     *
     * @param name
     * @return
     */
    boolean remove(String name);

    boolean removeAll(String type);
    <T extends RealmModel> boolean removeAll(Class<T> clazz);
    boolean removeAll();



//        fun add(subscription: Subscription): Subscription
//    fun addOrUpdate(subscription: Subscription): Subscription
//    fun remove(subscription: Subscription): Boolean
//    fun remove(name: String): Boolean
//    fun removeAll(pattern: String = "*"): Int

}
