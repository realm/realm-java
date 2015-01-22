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
 * Using RealmChangeListener, it is possible to be notified when a Realm instance has been updated.
 *
 * Realm instances on a thread without a {@link android.os.Looper} (almost all background threads)
 * doesn't get updated automatically, but have to call {@link Realm#refresh()} manually. This will
 * in turn trigger the RealmChangeListener for that background thread.
 *
 * All {@link io.realm.RealmObject} and {@link io.realm.RealmResults} will automatically contain
 * their new values when the {@link #onChange()} method is called. Normally this means that it
 * isn't necessary to query again for those objects, but just invalidate any UI elements that are
 * using them. If there is a chance that a object has been been deleted, it can be verified
 * by using {@link RealmObject#isValid()}.
 *
 * @see Realm#addChangeListener(RealmChangeListener)
 * @see Realm#removeAllChangeListeners()
 * @see Realm#removeChangeListener(RealmChangeListener)
 */
public interface RealmChangeListener {

    /**
     * Called when a transaction is committed
     */
    public void onChange();

}
