package io.realm.internal.objectstore;

import java.util.concurrent.TimeUnit;

import io.realm.RealmAsyncTask;
import io.realm.RealmModel;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.UnmanagedSubscription;
import io.realm.internal.async.RealmThreadPoolExecutor;
import io.realm.mongodb.sync.MutableSubscriptionSet;
import io.realm.mongodb.sync.Subscription;

public class OsMutableSubscriptionSet extends OsSubscriptionSet implements MutableSubscriptionSet {

    public OsMutableSubscriptionSet(long nativePtr,
                                    RealmProxyMediator schema,
                                    RealmThreadPoolExecutor listenerExecutor,
                                    RealmThreadPoolExecutor updateExecutor) {
        super(nativePtr, schema, listenerExecutor, updateExecutor);
    }

    @Override
    public Subscription add(Subscription subscription) {
        return addSubscription(subscription, true);
    }

    @Override
    public Subscription addOrUpdate(Subscription subscription) {
        return addSubscription(subscription, false);
    }

    private Subscription addSubscription(Subscription subscription, boolean throwOnUpdate) {
        if (subscription instanceof UnmanagedSubscription) {
            UnmanagedSubscription sub = (UnmanagedSubscription) subscription;
            long subscriptionPtr = nativeInsertOrAssign(getNativePtr(), sub.getName(), sub.getQueryPointer(), throwOnUpdate);
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

    private static native long nativeInsertOrAssign(long mutableSubscriptionsPtr, String name, long queryPtr, boolean throwOnUpdate);
    private static native boolean nativeRemoveNamed(long mutableSubscriptionsPtr, String name);
    private static native boolean nativeRemove(long mutableSubscriptionsPtr, long subscriptionPtr);
    private static native long nativeCommit(long mutableSubscriptionsPtr);
    private static native boolean nativeRemoveAll(long mutableSubscriptionsPtr);
    private static native boolean nativeRemoveAllForType(long mutableSubscriptionsPtr, String clazzType);
}
