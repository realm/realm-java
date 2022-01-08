package io.realm.mongodb.sync;

import java.util.Date;

import javax.annotation.Nullable;

import io.realm.RealmQuery;
import io.realm.annotations.Beta;
import io.realm.internal.annotations.ObjectServer;
import io.realm.internal.UnmanagedSubscription;

/**
 * TODO
 */
@ObjectServer
@Beta
public interface Subscription {

    /**
     * TODO
     * @param name
     * @param query
     * @return
     */
    public static Subscription create(String name, RealmQuery query) {
        return new UnmanagedSubscription(name, query);
    }

    /**
     * TODO
     * @param query
     * @return
     */
    public static Subscription create(RealmQuery query) {
        return create(null, query);
    }

    /**
     * TODO
     * @return
     */
    @Nullable
    public Date getCreatedAt();

    /**
     * TODO
     * @return
     */
    @Nullable
    public Date getUpdatedAt();

    /**
     * TODO
     * @return
     */
    @Nullable
    public String getName();

    /**
     * TODO
     * @return
     */
    public String getObjectType();

    /**
     * TODO
     * @return
     */
    public String getQuery();
}
