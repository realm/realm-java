/*
 * Copyright 2014 Realm Inc.
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
 * RealmChangeListener can be registered with a {@link Realm}, {@link RealmResults} or {@link RealmObject}
 * to receive a notification about updates.
 * <p>
 * When registered against a {@code Realm} you'll get notified when a Realm instance has been updated.
 * Register against a {@code RealmResults} or {@code RealmObject} to only get notified about changes to them.
 * <p>
 * Realm instances on a thread without an {@link android.os.Looper} cannot register a RealmChangeListener.
 * <p>
 * All {@link io.realm.RealmObject} and {@link io.realm.RealmResults} will automatically contain their new values when
 * the {@link #onChange(Object)} method is called. Normally this means that it isn't necessary to query again for those
 * objects, but just invalidate any UI elements that are using them. If there is a chance that a object has been been
 * deleted, it can be verified by using {@link RealmObject#isValid()}.
 *
 * @param <T> The <a href="https://realm.io/docs/java/latest/#auto-updating-objects">live object</a> being returned
 * ({@link Realm}, {@link DynamicRealm}, {@link RealmObject}, {@link RealmResults}, {@link DynamicRealmObject}
 * or your model implementing {@link RealmModel})
 * @see Realm#addChangeListener(RealmChangeListener)
 * @see Realm#removeAllChangeListeners()
 * @see Realm#removeChangeListener(RealmChangeListener)
 */
public interface RealmChangeListener<T> {

    /**
     * Called when a transaction is committed.
     */
    void onChange(T t);
}
