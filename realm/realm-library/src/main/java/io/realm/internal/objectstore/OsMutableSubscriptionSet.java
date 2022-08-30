/*
 * Copyright 2022 Realm Inc.
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
package io.realm.internal.objectstore;

import io.realm.RealmModel;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.UnmanagedSubscription;
import io.realm.internal.annotations.ObjectServer;
import io.realm.internal.async.RealmThreadPoolExecutor;
import io.realm.mongodb.sync.MutableSubscriptionSet;
import io.realm.mongodb.sync.Subscription;

// TODO Adding @ObjectServer here seems to break the Realm Build Transformer. Investigate why.
@ObjectServer
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
            throw new IllegalArgumentException("Only unmanaged subscriptions are allowed as input. This subscription was managed.");
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
