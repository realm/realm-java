package io.realm.internal;

import java.util.Date;

import javax.annotation.Nullable;

import io.realm.RealmQuery;
import io.realm.mongodb.sync.Subscription;

public class UnmanagedSubscription implements Subscription {

    private final Date createdAt;
    private final Date updatedAt;
    @Nullable
    private final String name;
    private final String objectType;
    private final RealmQuery query;

    public UnmanagedSubscription(@Nullable String name, RealmQuery query) {
        Date now = new Date();
        this.createdAt = now;
        this.updatedAt = now;
        this.name = name;
        this.objectType = query.getTypeQueried();
        this.query = query;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public Date getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getObjectType() {
        return objectType;
    }

    @Override
    public String getQuery() {
        return query.getDescription();
    }

    public long getQueryPointer() {
        return query.query.getNativePtr();
    }
}
