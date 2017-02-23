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
 * A collection class implementing this interface is capable of reporting fine-grained notifications about how the
 * collection is changed. It will report insertions, deletions and changes, but not <i>how</i> an individual element
 * changed. When a change is detected all registered listeners will be triggered.
 * <p>
 * This is often useful when updating UI elements, e.g. {@code RecyclerView.Adapter} can provide nicer animations and
 * work more effectively if it knows exactly which elements changed.
 * @see RealmObservable for information about more coarse-grained notifications.
 * @see <a href="https://realm.io/docs/java/latest/#adapters">Android Adapters</a>
 */
public interface RealmCollectionObservable<T, S extends OrderedRealmCollectionChangeListener>
        extends RealmObservable<T> {
    /**
     * Adds a change listener to this {@link OrderedRealmCollection}.
     *
     * @param listener the change listener to be notified.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException if you try to add a listener from a non-Looper or
     * {@link android.app.IntentService} thread.
     */
    void addChangeListener(S listener);

    /**
     * Removes the specified change listener.
     *
     * @param listener the change listener to be removed.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException if you try to remove a listener from a non-Looper Thread.
     * @see io.realm.RealmChangeListener
     */
    void removeChangeListener(S listener);
}
