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

/**
 * {@code RealmObjectChangeListener} can be registered with a {@link RealmModel} or {@link RealmObject} to receive
 * detailed notification about object changes.
 * <p>
 * Realm instances on a thread without an {@link android.os.Looper} cannot register a {@code RealmObjectChangeListener}.
 * <p>
 *
 * @param <T> The <a href="https://realm.io/docs/java/latest/#auto-updating-objects">live object</a> being returned
 *         ({@link Realm}, {@link DynamicRealm}, {@link RealmObject}, {@link RealmResults}, {@link DynamicRealmObject}
 *          or your model implementing {@link RealmModel})
 *
 * @see Realm#addChangeListener(RealmChangeListener)
 * @see Realm#removeAllChangeListeners()
 * @see Realm#removeChangeListener(RealmChangeListener)
 */
public interface RealmObjectChangeListener<T> {

    /**
     * When this gets called to return the results of an asynchronous query made by {@link RealmQuery#findFirstAsync()},
     * {@code changeSet} will be {@code null}.
     * <p>
     * When this gets called when the object is deleted, {@code changeSet.isDeleted()} will return {@code true} and
     * {@code changeSet.getFieldChanges()} will return {@code null}.
     * <p>
     * When this gets called when the object is modified, {@code changeSet.isDeleted()} will return {@code false} and
     * {@code changeSet.getFieldChanges()} will return the detailed information about the fields' changes.
     * <p>
     * This will be called if the {@link RealmObject} field gets set or removed, but it won't be called if the
     * {@link RealmObject} itself changes.
     *
     * @param object the {@code RealmObject} this listener is registered to.
     * @param changeSet the detailed information about the changes.
     */
    void onChange(T object, ObjectChangeSet changeSet);
}
