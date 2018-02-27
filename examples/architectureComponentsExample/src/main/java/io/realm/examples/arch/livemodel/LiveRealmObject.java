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

import io.realm.ObjectChangeSet;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmObjectChangeListener;

/**
 * This class represents a RealmObject wrapped inside a LiveData.
 *
 * It is expected that the provided RealmObject is a managed object, and exists in the Realm on creation.
 *
 * This allows observing the RealmObject in such a way, that the listener that will be automatically unsubscribed when the enclosing LifecycleOwner is killed.
 *
 * Realm will keep the managed RealmObject up-to-date whenever a change occurs on any thread,
 * and when that happens, the observer will be notified.
 *
 * The object will be observed until it is invalidated - deleted, or all local Realm instances are closed.
 *
 * @param <T> the type of the RealmModel
 */
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

    /**
     * Wraps the provided managed RealmObject as a LiveData.
     *
     * The provided object should not be null, should be managed, and should be valid.
     *
     * @param object the managed RealmModel to wrap as LiveData
     */
    @MainThread
    public LiveRealmObject(@NonNull T object) {
        //noinspection ConstantConditions
        if (object == null) {
            throw new IllegalArgumentException("The object cannot be null!");
        }
        if (!RealmObject.isManaged(object)) {
            throw new IllegalArgumentException("LiveRealmObject only supports managed RealmModel instances!");
        }
        if (!RealmObject.isValid(object)) {
            throw new IllegalArgumentException("The provided RealmObject is no longer valid, and therefore cannot be observed for changes.");
        }
        setValue(object);
    }

    // We should start observing and stop observing, depending on whether we have observers.
    // Deleted objects can no longer be observed.
    // We can also no longer observe the object if all local Realm instances on this thread (the UI thread) are closed.

    /**
     * Starts observing the RealmObject, if it is still valid.
     */
    @Override
    protected void onActive() {
        super.onActive();
        T object = getValue();
        if (object != null && RealmObject.isValid(object)) {
            RealmObject.addChangeListener(object, listener);
        }
    }

    /**
     * Stops observing the RealmObject.
     */
    @Override
    protected void onInactive() {
        super.onInactive();
        T object = getValue();
        if (object != null && RealmObject.isValid(object)) {
            RealmObject.removeChangeListener(object, listener);
        }
    }
}
