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

import android.os.Handler;
import android.os.Looper;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

import io.realm.RealmAsyncTask;
import io.realm.RealmQuery;
import io.realm.internal.NativeObject;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.annotations.ObjectServer;
import io.realm.internal.async.RealmAsyncTaskImpl;
import io.realm.internal.async.RealmThreadPoolExecutor;
import io.realm.mongodb.sync.Subscription;
import io.realm.mongodb.sync.SubscriptionSet;

// TODO Adding @ObjectServer here seems to break the Realm Build Transformer. Investigate why.
@ObjectServer
public class OsSubscriptionSet implements NativeObject, SubscriptionSet {

    public static final byte STATE_VALUE_UNCOMMITTED = 0;
    public static final byte STATE_VALUE_PENDING = 1;
    public static final byte STATE_VALUE_BOOTSTRAPPING = 2;
    public static final byte STATE_VALUE_COMPLETE = 3;
    public static final byte STATE_VALUE_ERROR = 4;
    public static final byte STATE_VALUE_SUPERSEDED = 5;

    private static final long nativeFinalizerPtr = nativeGetFinalizerMethodPtr();
    protected final RealmProxyMediator schema;
    private final RealmThreadPoolExecutor stateListenerExecutor;
    private final RealmThreadPoolExecutor updateExecutor;
    private long nativePtr;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public OsSubscriptionSet(long nativePtr, RealmProxyMediator schema, RealmThreadPoolExecutor listenerExecutor, RealmThreadPoolExecutor writeExecutor) {
        this.nativePtr = nativePtr;
        this.schema = schema;
        this.stateListenerExecutor = listenerExecutor;
        this.updateExecutor= writeExecutor;
    }

    @Override
    public long getNativePtr() {
        return this.nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    @Nullable
    @Override
    public Subscription find(RealmQuery query) {
        long subscriptionPtr = nativeFindByQuery(nativePtr, query.getQueryPointer());
        if (subscriptionPtr != -1) {
            return new OsSubscription(subscriptionPtr);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public Subscription find(String name) {
        long subscriptionPtr = nativeFindByName(nativePtr, name);
        if (subscriptionPtr != -1) {
            return new OsSubscription(subscriptionPtr);
        } else {
            return null;
        }
    }

    @Override
    public SubscriptionSet.State getState() {
        byte value = nativeState(nativePtr);
        return SubscriptionSet.State.fromNativeValue(value);
    }

    @Override
    public int size() {
        return (int) nativeSize(nativePtr);
    }

    @Override
    public String getErrorMessage() {
        return nativeErrorMessage(nativePtr);
    }

    @Override
    public boolean waitForSynchronization() {
        return waitForSynchronization(Long.MAX_VALUE, TimeUnit.SECONDS);
    }

    @Override
    public boolean waitForSynchronization(Long timeOut, TimeUnit unit) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean success = new AtomicBoolean(false);
        nativeWaitForSynchronization(nativePtr, new StateChangeCallback() {
            @Override
            public void onChange(byte state) {
                success.set(State.fromNativeValue(state) == State.COMPLETE);
                latch.countDown();
            }
        });
        try {
            if (!latch.await(timeOut, unit)) {
                throw new RuntimeException("Waiting for waitForSynchronization() timed out.");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Waiting for waitForSynchronization() was interrupted.");
        }
        refresh();
        return success.get();
    }

    @Override
    public RealmAsyncTask waitForSynchronizationAsync(SubscriptionSet.StateChangeCallback callback) {
        return waitForSynchronizationAsync(Long.MAX_VALUE, TimeUnit.SECONDS, callback);
    }

    @Override
    public RealmAsyncTask waitForSynchronizationAsync(Long timeOut, TimeUnit unit, SubscriptionSet.StateChangeCallback callback) {
        Future<?> future = stateListenerExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    waitForSynchronization(timeOut, unit);
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onStateChange(OsSubscriptionSet.this);
                        }
                    });
                } catch (Exception e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(e);
                        }
                    });
                }
            }
        });
        return new RealmAsyncTaskImpl(future, stateListenerExecutor);
    }

    @Override
    public SubscriptionSet update(UpdateCallback action) {
        OsMutableSubscriptionSet mutableSubs = new OsMutableSubscriptionSet(
                nativeCreateMutableSubscriptionSet(nativePtr),
                schema,
                stateListenerExecutor,
                updateExecutor
        );
        action.update(mutableSubs);
        long newSubscriptionsSet = mutableSubs.commit();
        // Once commit succeed, replace the current SubscriptionSet pointer with
        // the new one. If the commit fails, the MutableSubscriptionSet will be
        // GC'ed and released and the SubscriptionSet will keep the state before
        // the UpdateCallback was called.
        long oldPointer = nativePtr;
        nativePtr = newSubscriptionsSet;
        nativeRelease(oldPointer);
        return this;
    }

    @Override
    public RealmAsyncTask updateAsync(UpdateAsyncCallback callback) {
        Future<?> future = updateExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    SubscriptionSet updatedSubscriptions = update(callback);
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(updatedSubscriptions);
                        }
                    });
                } catch (Throwable exception) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(exception);
                        }
                    });
                }
            }
        });
        return new RealmAsyncTaskImpl(future, updateExecutor);
    }

    public void refresh() {
        nativeRefresh(nativePtr);
    }

    @Override
    public Iterator<Subscription> iterator() {
        // This iterator does not support removals.
        return new Iterator<Subscription>() {
            private int cursor = 0;
            private final int size = size();

            @Override
            public boolean hasNext() {
                return (cursor < size);
            }

            @Override
            public Subscription next() {
                if (cursor >= size) {
                    throw new NoSuchElementException("Iterator has no more elements. " +
                            "Tried index " + cursor + ". Size is " + size + ".");
                }
                long subscriptionPtr = nativeSubscriptionAt(nativePtr, cursor);
                cursor++;
                return new OsSubscription(subscriptionPtr);
            }
        };
    }

    private interface StateChangeCallback {
        void onChange(byte state); // Must not throw
    }

    private static native long nativeGetFinalizerMethodPtr();
    private static native void nativeRelease(long nativePtr);
    private static native long nativeSize(long nativePtr);
    private static native byte nativeState(long nativePtr);
    private static native String nativeErrorMessage(long nativePtr);
    private static native long nativeCreateMutableSubscriptionSet(long nativePtr);
    private static native long nativeSubscriptionAt(long nativePtr, int index);
    private static native void nativeWaitForSynchronization(long nativePtr, StateChangeCallback callback);
    private static native long nativeFindByName(long nativePtr, String name);
    private static native long nativeFindByQuery(long nativePtr, long queryPtr);
    private static native void nativeRefresh(long nativePtr);
}
