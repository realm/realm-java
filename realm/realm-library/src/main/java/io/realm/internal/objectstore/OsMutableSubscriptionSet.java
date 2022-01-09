package io.realm.internal.objectstore;

import io.realm.RealmModel;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.UnmanagedSubscription;
import io.realm.mongodb.sync.MutableSubscriptionSet;
import io.realm.mongodb.sync.Subscription;
import io.realm.mongodb.sync.SubscriptionSet;

public class OsMutableSubscriptionSet extends OsSubscriptionSet implements MutableSubscriptionSet {

    public OsMutableSubscriptionSet(long nativePtr, RealmProxyMediator schema) {
        super(nativePtr, schema);
    }

    @Override
    public Subscription addOrUpdate(Subscription subscription) {
        if (subscription instanceof UnmanagedSubscription) {
            UnmanagedSubscription sub = (UnmanagedSubscription) subscription;
            long subscriptionPtr = nativeInsertOrAssign(getNativePtr(), sub.getName(), sub.getQueryPointer());
            return new OsSubscription(subscriptionPtr);
        } else {
            throw new IllegalArgumentException("TODO");
        }
    }

    @Override
    public boolean remove(Subscription subscription) {
        if (subscription instanceof OsSubscription) {
            return nativeRemove(getNativePtr(), ((OsSubscription) subscription).getNativePtr());
        } else {
            throw new IllegalArgumentException("Only managed Subscriptions can be removed.");
        }
    }

    @Override
    public boolean remove(String name) {
        return nativeRemoveNamed(getNativePtr(), name);
    }

    @Override
    public boolean removeAll(String type) {
        return nativeRemoveAllForType(getNativePtr(), type);
    }

    @Override
    public <T extends RealmModel> boolean removeAll(Class<T> clazz) {
        return nativeRemoveAllForType(getNativePtr(), schema.getSimpleClassName(clazz));
    }

    @Override
    public boolean removeAll() {
        return nativeRemoveAll(getNativePtr());
    }

    /**
     * Returns the native pointer for the updated underlying SubscriptionSet
     */
    public long commit() {
        return nativeCommit(getNativePtr());
    }

    private static native long nativeInsertOrAssign(long mutableSubscriptionsPtr, String name, long queryPtr);
    private static native boolean nativeRemoveNamed(long mutableSubscriptionsPtr, String name);
    private static native boolean nativeRemove(long mutableSubscriptionsPtr, long subscriptionPtr);
    private static native long nativeCommit(long mutableSubscriptionsPtr);
    private static native boolean nativeRemoveAll(long mutableSubscriptionsPtr);
    private static native boolean nativeRemoveAllForType(long mutableSubscriptionsPtr, String clazzType);
}
