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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.realm.RealmChangeListener;

/**
 * This interface needs to be implemented by Java and pass to Realm Object Store in order to get notifications when
 * other thread/process changes the Realm file.
 */
@Keep
public class RealmNotifier {

    private static class RealmObserverPair extends ObserverPair<RealmChangeListener> {

        public RealmObserverPair(Object observer, RealmChangeListener listener) {
            super(listener, observer);
        }

        private void onChange() {
            Object observer = observerRef.get();
            if (observer != null) {
                listener.onChange(observer);
            }
        }
    }

    private List<RealmObserverPair> realmObserverPairs = new CopyOnWriteArrayList<RealmObserverPair>();

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
    @SuppressWarnings("unused") // called from java_binding_context.cpp
    void notifyCommitByOtherThread() {
        for (RealmObserverPair observerPair : realmObserverPairs) {
            Object observer = observerPair.observerRef.get();
            if (observer == null) {
                realmObserverPairs.remove(observerPair);
            } else {
                observerPair.onChange();
            }
        }
    }

    /**
     * Called when close SharedRealm to clean up any event left in to queue.
     */
    public void close() {
        removeAllChangeListeners();
    }

    public void addChangeListener(Object observer, RealmChangeListener realmChangeListener) {
        RealmObserverPair observerPair = new RealmObserverPair(observer, realmChangeListener);
        if (!realmObserverPairs.contains(observerPair)) {
            realmObserverPairs.add(observerPair);
        }
    }

    public void removeChangeListener(Object observer, RealmChangeListener realmChangeListener) {
        RealmObserverPair observerPair = new RealmObserverPair(observer, realmChangeListener);
        realmObserverPairs.remove(observerPair);
    }

    public void removeAllChangeListeners() {
        realmObserverPairs.clear();
    }
}
