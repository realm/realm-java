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

package io.realm.internal.sync;

import javax.annotation.Nullable;

import io.realm.RealmChangeListener;
import io.realm.internal.KeepMember;
import io.realm.internal.NativeObject;
import io.realm.internal.ObserverPairList;
import io.realm.internal.OsResults;

@KeepMember
public class OsSubscription implements NativeObject {

    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    // Mirrors the values in https://github.com/realm/realm-object-store/blob/master/src/sync/subscription_state.hpp
    public enum SubscriptionState {
        ERROR(-1),  // An error occurred while creating or processing the partial sync subscription.
        CREATING(2), // The subscription is being created.
        PENDING(0), // The subscription was created, but has not yet been processed by the sync server.
        COMPLETE(1), // The subscription has been processed by the sync server and data is being synced to the device.
        INVALIDATED(3); // The subscription has been removed.

        private final int val;

        SubscriptionState(int val) {
            this.val = val;
        }

        public static SubscriptionState fromInternalValue(int val) {
            for (SubscriptionState subscriptionState : values()) {
                if (subscriptionState.val == val) {
                    return subscriptionState;
                }
            }
            throw new IllegalArgumentException("Unknown value: " + val);
        }
    }

    private static class SubscriptionObserverPair
            extends ObserverPairList.ObserverPair<OsSubscription, RealmChangeListener<OsSubscription>> {
        public SubscriptionObserverPair(OsSubscription observer, RealmChangeListener<OsSubscription> listener) {
            super(observer, listener);
        }

        public void onChange(OsSubscription observer) {
            listener.onChange(observer);
        }
    }

    private static class Callback implements ObserverPairList.Callback<SubscriptionObserverPair> {
        @Override
        public void onCalled(SubscriptionObserverPair pair, Object observer) {
            pair.onChange((OsSubscription) observer);
        }
    }

    private final long nativePtr;
    protected final ObserverPairList<SubscriptionObserverPair> observerPairs = new ObserverPairList<>();

    public OsSubscription(OsResults results, String subscriptionName) {
        this.nativePtr = nativeCreate(results.getNativePtr(), subscriptionName);
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    public SubscriptionState getState() {
        return SubscriptionState.fromInternalValue(nativeGetState(nativePtr));
    }

    @Nullable
    public Throwable getError() {
        return (Throwable) nativeGetError(nativePtr);
    }

    public void addChangeListener(RealmChangeListener<OsSubscription> listener) {
        if (observerPairs.isEmpty()) {
            nativeStartListening(nativePtr);
        }
        observerPairs.add(new SubscriptionObserverPair(this, listener));
    }

    public void removeChangeListener(RealmChangeListener<OsSubscription> listener) {
        observerPairs.remove(this, listener);
        if (observerPairs.isEmpty()) {
            nativeStopListening(nativePtr);
        }
    }

    // Called from JNI
    @KeepMember
    private void notifyChangeListeners() {
        observerPairs.foreach(new Callback());
    }

    private static native long nativeCreate(long resultsNativePtr, String subscriptionName);

    private static native long nativeGetFinalizerPtr();

    private static native int nativeGetState(long nativePtr);

    private static native Object nativeGetError(long nativePtr);

    private native void nativeStartListening(long nativePtr);

    private native void nativeStopListening(long nativePtr);

}
