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

import javax.annotation.Nullable;

import io.realm.RealmChangeListener;


/**
 * This interface needs to be implemented by Java and pass to Realm Object Store in order to get notifications when
 * other thread/process changes the Realm file.
 */
@Keep
public abstract class RealmNotifier implements Closeable {

// Calling sequences for a remote commit
// |-------------------------------+--------------+-----------------------------------|
// | Thread A                      | Thread B     | Daemon Thread                     |
// |-------------------------------+--------------+-----------------------------------|
// |                               | Make changes |                                   |
// |-------------------------------+--------------+-----------------------------------|
// |                               |              | epoll callback and notify ALooper |
// |-------------------------------+--------------+-----------------------------------|
// | ALooper callback              |              |                                   |
// | BindingContext::before_notify |              |                                   |
// | RealmNotifier.beforeNotify    |              |                                   |
// | BindingContext::did_change    |              |                                   |
// | RealmNotifier.didChange       |              |                                   |
// | process_available_async       |              |                                   |
// | Collection listeners          |              |                                   |
// |-------------------------------+--------------+-----------------------------------|

    private static class RealmObserverPair<T> extends ObserverPairList.ObserverPair<T, RealmChangeListener<T>> {
        public RealmObserverPair(T observer, RealmChangeListener<T> listener) {
            super(observer, listener);
        }

        private void onChange(T observer) {
            //noinspection ConstantConditions
            if (observer != null) {
                listener.onChange(observer);
            }
        }
    }

    private ObserverPairList<RealmObserverPair> realmObserverPairs = new ObserverPairList<RealmObserverPair>();
    private final ObserverPairList.Callback<RealmObserverPair> onChangeCallBack =
            new ObserverPairList.Callback<RealmObserverPair>() {
                @Override
                public void onCalled(RealmObserverPair pair, Object observer) {
                    //noinspection unchecked
                    if (sharedRealm != null && !sharedRealm.isClosed()) {
                        pair.onChange(observer);
                    }
                }
            };

    protected RealmNotifier(@Nullable OsSharedRealm sharedRealm) {
        this.sharedRealm = sharedRealm;
    }

    private OsSharedRealm sharedRealm;
    // TODO: The only reason we have this is that async transactions is not supported by OS yet. And OS is using ALopper
    // which will be using a different message queue from which java is using to deliver remote Realm changes message.
    // We need a way to deliver the async transaction onSuccess callback to the caller thread after the caller Realm
    // advanced. This is implemented by posting the callback by RealmNotifier.post() first, and check the realm version
    // in the posted Runnable. If the Realm version there is still behind the async transaction we committed, the
    // onSuccess callback will be added to this list and be executed later when we get the change event from OS.
    // This list is NOT supposed to be thread safe!
    private List<Runnable> transactionCallbacks = new ArrayList<Runnable>();

    // List of runnables called when Object Store is about to start sending out notifications about
    // a version update for the current thread.
    private List<Runnable> startSendingNotificationsCallbacks = new ArrayList<>();

    // List of runnables called when Object Store has finished sending out notifications for the
    // version of the Realm on this thread.
    private List<Runnable> finishedSendingNotificationsCallbacks = new ArrayList<>();

    // Called from JavaBindingContext::did_change.
    // This will be called in the caller thread when:
    // - A committed remote transaction, called from changed event handler.
    // - A committed remote transaction, called directly from refresh call.
    // - A committed local transaction, called directly from commitTransaction instead of next event.
    //   loop.
    // Package protected to avoid finding class by name in JNI.
    @SuppressWarnings("unused")
    // called from java_binding_context.cpp
    void didChange() {
        realmObserverPairs.foreach(onChangeCallBack);

        if (!transactionCallbacks.isEmpty()) {
            // The callback list needs to be cleared before calling to avoid synchronized transactions in the callback
            // triggers it recursively.
            List<Runnable> callbacks = transactionCallbacks;
            transactionCallbacks = new ArrayList<Runnable>();
            for (Runnable runnable : callbacks) {
                runnable.run();
            }
        }
    }

    // Called from JavaBindingContext::before_notify.
    // This will be called in the caller thread when:
    // 1. Get changed notification by this/other Realm instances.
    // 2. OsSharedRealm::refresh called.
    // In both cases, this will be called before the any other callbacks (changed callbacks, async query callbacks.).
    // Package protected to avoid finding class by name in JNI.
    @SuppressWarnings("unused")
    void beforeNotify() {
        // For the stable iteration.
        sharedRealm.invalidateIterators();
    }

    // Called from JavaBindingContext::will_send_notifications
    // This will be called before any change notifications are delivered when updating a
    // Realm version. This will be triggered even if no change listeners are registered.
    void willSendNotifications() {
        for (int i = 0; i < startSendingNotificationsCallbacks.size(); i++) {
            startSendingNotificationsCallbacks.get(i).run();
        }
    }

    // Called from JavaBindingContext::will_send_notifications
    void didSendNotifications() {
        for (int i = 0; i < startSendingNotificationsCallbacks.size(); i++) {
            finishedSendingNotificationsCallbacks.get(i).run();
        }
    }

    /**
     * Called when close OsSharedRealm to clean up any event left in to queue.
     */
    @Override
    public void close() {
        removeAllChangeListeners();
        startSendingNotificationsCallbacks.clear();
        finishedSendingNotificationsCallbacks.clear();
    }

    public <T> void addChangeListener(T observer, RealmChangeListener<T> realmChangeListener) {
        RealmObserverPair observerPair = new RealmObserverPair<>(observer, realmChangeListener);
        realmObserverPairs.add(observerPair);
    }

    public <E> void removeChangeListener(E observer, RealmChangeListener<E> realmChangeListener) {
        realmObserverPairs.remove(observer, realmChangeListener);
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

    /**
     * For current implementation of async transaction only. See comments for {@link #transactionCallbacks}.
     *
     * @param runnable to be executed in the following event loop.
     */
    public abstract boolean post(Runnable runnable);

    public int getListenersListSize() {
        return realmObserverPairs.size();
    }

    public void addBeginSendingNotificationsCallback(Runnable runnable) {
        startSendingNotificationsCallbacks.add(runnable);
    }

    public void addFinishedSendingNotificationsCallback(Runnable runnable) {
        finishedSendingNotificationsCallbacks.add(runnable);
    }
}
