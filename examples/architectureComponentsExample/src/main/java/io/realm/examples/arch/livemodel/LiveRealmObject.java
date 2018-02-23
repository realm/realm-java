/*
 * Copyright 2017 Realm Inc.
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

import io.realm.ObjectChangeSet;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmObjectChangeListener;


public class LiveRealmObject<T extends RealmModel> extends LiveData<T> {
    // The listener will listen until the object is deleted.
    // An invalidated object shouldn't be set in LiveData, null is set instead.
    private RealmObjectChangeListener<T> listener = new RealmObjectChangeListener<T>() {
        @Override
        public void onChange(@NonNull T object, ObjectChangeSet objectChangeSet) {
            if (!objectChangeSet.isDeleted()) {
                setValue(object);
            } else {
                setValue(null);
            }
        }
    };

    @MainThread
    public LiveRealmObject(@NonNull T object) {
        //noinspection ConstantConditions
        if (object == null) {
            throw new IllegalArgumentException("The object cannot be null!");
        }
        setValue(object);
    }

    // We should start observing and stop observing, depending on whether we have observers.
    @Override
    protected void onActive() {
        super.onActive();
        T object = getValue();
        if (object != null && RealmObject.isValid(object)) {
            RealmObject.addChangeListener(object, listener);
        }
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        T object = getValue();
        if (object != null && RealmObject.isValid(object)) {
            RealmObject.removeChangeListener(object, listener);
        }
    }
}
