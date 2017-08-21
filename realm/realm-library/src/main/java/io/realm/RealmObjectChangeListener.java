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

package io.realm;

import javax.annotation.Nullable;

import io.realm.annotations.LinkingObjects;

/**
 * {@code RealmObjectChangeListener} can be registered on a {@link RealmModel} or {@link RealmObject} to receive
 * detailed notifications when an object changes.
 * <p>
 * Realm instances on a thread without an {@link android.os.Looper} cannot register a {@code RealmObjectChangeListener}.
 * <p>
 * Listener cannot be registered inside a transaction.
 *
 * @param <T> The type of {@link RealmModel} on which your listener will be registered.
 * @see Realm#addChangeListener(RealmChangeListener)
 * @see Realm#removeAllChangeListeners()
 * @see Realm#removeChangeListener(RealmChangeListener)
 */
public interface RealmObjectChangeListener<T extends RealmModel> {

    /**
     * When this gets called to return the results of an asynchronous query made by {@link RealmQuery#findFirstAsync()},
     * {@code changeSet} will be {@code null}.
     * <p>
     * When this gets called because the object was deleted, {@code changeSet.isDeleted()} will return {@code true}
     * and {@code changeSet.getFieldChanges()} will return {@code null}.
     * <p>
     * When this gets called because the object was modified, {@code changeSet.isDeleted()} will return {@code false}
     * and {@code changeSet.getFieldChanges()} will return the detailed information about the fields' changes.
     * <p>
     * If a field points to another RealmObject this listener will only be triggered if the field is set to a new object
     * or null. Updating the referenced RealmObject will not trigger this listener.
     * <p>
     * If a field points to a RealmList, this listener will only be triggered if one or multiple objects are inserted,
     * removed or moved within the List. Updating the objects in the RealmList will not trigger this listener.
     * <p>
     * Changes to {@link LinkingObjects} annotated {@link RealmResults} fields will not be monitored, nor reported
     * through this change listener.
     * @param t the {@code RealmObject} this listener is registered to.
     * @param changeSet the detailed information about the changes.
     */
    void onChange(T t, @Nullable ObjectChangeSet changeSet);
}
