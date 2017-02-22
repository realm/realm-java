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
 * A class implementing this interface is capable of reporting when the data stored by the class have changed. When that
 * happens all registered {@link RealmChangeListener}'s will be triggered.
 * <p>
 * This class will only report that <i>something</i> changed, not what changed.
 * @see RealmCollectionObservable for information about more fine-grained collection notifications.
 */
public interface RealmObservable<T> {
    /**
     * Adds a change listener to this {@link RealmResults}, {@link RealmList}, {@link Realm}, {@link DynamicRealm} or
     * {@link RealmObject}.
     *
     * @param listener the change listener to be notified.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException if you try to add a listener from a non-Looper or
     * {@link android.app.IntentService} thread.
     */
    void addChangeListener(RealmChangeListener<T> listener);

    /**
     * Removes the specified change listener.
     *
     * @param listener the change listener to be removed.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException if you try to remove a listener from a non-Looper Thread.
     * @see io.realm.RealmChangeListener
     */
    void removeChangeListener(RealmChangeListener<T> listener);

    /**
     * Removes all user-defined change listeners.
     *
     * @throws IllegalStateException if you try to remove listeners from a non-Looper Thread.
     * @see io.realm.RealmChangeListener
     */
    void removeAllChangeListeners();
}
