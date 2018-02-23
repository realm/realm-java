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

package io.realm.examples.arch.livemodel;

import android.arch.lifecycle.LiveData;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import java.util.List;

import javax.annotation.Nullable;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmModel;
import io.realm.RealmResults;

public class LiveRealmResults<T extends RealmModel> extends LiveData<List<T>> {
    private final RealmResults<T> results;

    // The listener will notify the observers whenever a change occurs.
    // The results are modified in change. This could be expanded to also return the change set in a pair.
    private OrderedRealmCollectionChangeListener<RealmResults<T>> listener = new OrderedRealmCollectionChangeListener<RealmResults<T>>() {
        @Override
        public void onChange(@NonNull RealmResults<T> results, @Nullable OrderedCollectionChangeSet changeSet) {
            LiveRealmResults.this.setValue(results);
        }
    };

    @MainThread
    public LiveRealmResults(@NonNull RealmResults<T> results) {
        //noinspection ConstantConditions
        if (results == null) {
            throw new IllegalArgumentException("Results cannot be null!");
        }
        this.results = results;
        if (results.isLoaded()) { // we should not notify observers when results aren't ready yet (async query).
            setValue(results);
        }
    }

    // We should start observing and stop observing, depending on whether we have observers.
    @Override
    protected void onActive() {
        super.onActive();
        results.addChangeListener(listener);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        results.removeChangeListener(listener);
    }
}
