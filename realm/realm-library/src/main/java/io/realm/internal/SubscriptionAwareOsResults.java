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

package io.realm.internal;

import javax.annotation.Nullable;

import io.realm.RealmChangeListener;
import io.realm.internal.sync.OsSubscription;

/**
 * Wrapper around Object Stores Results class that is capable of combining partial sync Subscription
 * state updates and collection change updates.
 */
public class SubscriptionAwareOsResults extends OsResults {

    private final String subscriptionName;
    // The native ptr to a delayed notification. Since Java group all notifications for each each
    // RealmResults, only one change from OS will ever be sent.
    private long delayedNotificationPtr = 0;
    // If true, the subscription somehow changed during this round of notifications being sent
    private boolean subscriptionChanged;
    // Reference to a (potential) underlying subscription
    private OsSubscription subscription = null;
    private boolean collectionChanged = false;

    public static SubscriptionAwareOsResults createFromQuery(OsSharedRealm sharedRealm, TableQuery query,
                                                             @Nullable SortDescriptor sortDescriptor,
                                                             @Nullable SortDescriptor distinctDescriptor,
                                                             String subscriptionName) {
        query.validateQuery();
        long ptr = nativeCreateResults(sharedRealm.getNativePtr(), query.getNativePtr(), sortDescriptor, distinctDescriptor);
        return new SubscriptionAwareOsResults(sharedRealm, query.getTable(), ptr, subscriptionName);
    }

    SubscriptionAwareOsResults(OsSharedRealm sharedRealm, Table table, long nativePtr, String subscriptionName) {
        super(sharedRealm, table, nativePtr);

        this.subscriptionName = subscriptionName;
        this.subscription = new OsSubscription(this, subscriptionName);
        this.subscription.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange(Object o) {
                subscriptionChanged = true;
            }
        });
        RealmNotifier notifier = sharedRealm.realmNotifier;
        notifier.addBeginSendingNotificationsCallback(new Runnable() {
            @Override
            public void run() {
                subscriptionChanged = false;
                collectionChanged = false;
                delayedNotificationPtr = 0;
            }
        });
        notifier.addFinishedSendingNotificationsCallback(new Runnable() {
            @Override
            public void run() {
                if (collectionChanged || subscriptionChanged) {
                    triggerDelayedChangeListener();
                }
            }
        });
    }

    private void triggerDelayedChangeListener() {
        // Object Store compute the change set between the SharedGroup versions when the query created and the latest.
        // So it is possible it deliver a non-empty change set for the first async query returns.
        OsCollectionChangeSet changeset;
        // Only parse on Subscription if it changed
        OsSubscription subscription = (subscriptionChanged) ? this.subscription : null;
        if (delayedNotificationPtr == 0) {
            changeset = new EmptyLoadChangeSet(subscription);
        } else {
            changeset = new OsCollectionChangeSet(delayedNotificationPtr, !isLoaded(), subscription);
        }

        // Happens e.g. if a synchronous query is created, a change listener is added and then
        // a transaction is started on the same thread. This will trigger all notifications
        // and deliver an empty changeset.
        if (changeset.isEmpty() && isLoaded()) {
            return;
        }
        loaded = true;
        observerPairs.foreach(new Callback(changeset));
    }

    @Override
    public void notifyChangeListeners(long nativeChangeSetPtr) {
        collectionChanged = true;
        delayedNotificationPtr = nativeChangeSetPtr;
    }

}


