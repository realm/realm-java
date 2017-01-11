/*
 * Copyright 2016 Realm Inc.
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

package io.realm.internal;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmChangeListener;

/**
 * This interface needs to be implemented by Java and pass to Realm Object Store in order to get notifications when
 * other thread/process changes the Realm file.
 */
@Keep
public abstract class RealmNotifier implements Closeable {

    private SharedRealm sharedRealm;

    private static class RealmObserverPair<T> extends ObserverPairList.ObserverPair<T, RealmChangeListener<T>> {
        public RealmObserverPair(T observer, RealmChangeListener<T> listener) {
            super(observer, listener);
        }

        private void onChange(T observer) {
            if (observer != null) {
                listener.onChange(observer);
            }
        }
    }

    private ObserverPairList<RealmObserverPair> realmObserverPairs = new ObserverPairList<RealmObserverPair>();
    private final static ObserverPairList.Callback<RealmObserverPair> onChangeCallBack =
            new ObserverPairList.Callback<RealmObserverPair>() {
                @Override
                public void onCalled(RealmObserverPair pair, Object observer) {
                    //noinspection unchecked
                    pair.onChange(observer);
                }
            };

    // TODO: The only reason we have this is that async transactions is not supported by OS yet. And OS is using ALopper
    // which will be using a different message queue from which java is using to deliver remote Realm changes message.
    // We need a way to deliver the async transaction onSuccess callback to the caller thread after the caller Realm
    // advanced. This is implemented by posting the callback by RealmNotifier.post() first, and check the realm version
    // in the posted Runnable. If the Realm version there is still behind the async transaction we committed, the
    // onSuccess callback will be added to this list and be executed later when we get the change event from OS.
    // This list is NOT supposed to be thread safe!
    private List<Runnable> transactionCallbacks = new ArrayList<Runnable>();

    // This is called by OS when other thread/process changes the Realm.
    // This is getting called on the same thread which created the Realm.
    // |---------------------------------------------------------------+--------------+------------------------------------------------|
    // | Thread A                                                      | Thread B     | Daemon Thread                                  |
    // |---------------------------------------------------------------+--------------+------------------------------------------------|
    // |                                                               | Make changes |                                                |
    // |                                                               |              | Detect and notify thread A through JNI ALooper |
    // | Call OS's Realm::notify() from OS's ALooper callback          |              |                                                |
    // | Realm::notify() calls JavaBindingContext:change_available()   |              |                                                |
    // | change_available calls into this method to send REALM_CHANGED |              |                                                |
    // |---------------------------------------------------------------+--------------+------------------------------------------------|
    /**
     * This is called in Realm Object Store's JavaBindingContext::changes_available.
     * This is getting called on the same thread which created this Realm when the same Realm file has been changed by
     * other thread. The changes on the same thread should not trigger this call.
     */
    // Package protected to avoid finding class by name in JNI.
    @SuppressWarnings("unused") // called from java_binding_context.cpp
    void didChange() {
        realmObserverPairs.foreach(onChangeCallBack);
        for (Runnable runnable : transactionCallbacks) {
            runnable.run();
        }
        transactionCallbacks.clear();
    }

    @SuppressWarnings("unused") // called from java_binding_context.cpp
    // Package protected to avoid finding class by name in JNI.
    void changesAvailable() {
        // For the stable iteration.
        sharedRealm.reattachCollections();
    }

    void setSharedRealm(SharedRealm sharedRealm) {
        this.sharedRealm = sharedRealm;
    }

    /**
     * Called when close SharedRealm to clean up any event left in to queue.
     */
    @Override
    public void close() {
        removeAllChangeListeners();
    }

    public <T> void addChangeListener(T observer, RealmChangeListener<T> realmChangeListener) {
        RealmObserverPair observerPair = new RealmObserverPair<T>(observer, realmChangeListener);
        realmObserverPairs.add(observerPair);
    }

    public <E> void removeChangeListener(E observer, RealmChangeListener<E> realmChangeListener) {
        RealmObserverPair observerPair = new RealmObserverPair<E>(observer, realmChangeListener);
        realmObserverPairs.remove(observerPair);
    }

    public <E> void removeChangeListeners(E observer) {
       realmObserverPairs.removeByObserver(observer);
    }

    // Since RealmObject is using this notifier as well, use removeChangeListeners to remove all listeners by the given
    // observer.
    private void removeAllChangeListeners() {
        realmObserverPairs.clear();
    }

    public void addTransactionCallback(Runnable runnable) {
        transactionCallbacks.add(runnable);
    }

    public abstract void postAtFrontOfQueue(Runnable runnable);

    /**
     * For current implementation of async transaction only. See comments for {@link #transactionCallbacks}.
     *
     * @param runnable to be executed in the following event loop.
     */
    public abstract void post(Runnable runnable);
}
