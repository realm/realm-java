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

/**
 * {@link OrderedRealmCollectionChangeListener} can be registered with a {@link RealmResults} to receive a notification
 * with a {@link OrderedCollectionChangeSet} to describe the details of what have been changed in the collection from
 * last time.
 * <p>
 * Realm instances on a thread without an {@link android.os.Looper} cannot register a
 * {@link OrderedRealmCollectionChangeListener}.
 * <p>
 *
 * @see RealmResults#addChangeListener(OrderedRealmCollectionChangeListener)
 */
public interface OrderedRealmCollectionChangeListener<T> {

    /**
     * This will be called when the async query is finished the first time or the collection of objects has changed.
     *
     * @param t the collection this listener is registered to.
     * @param changeSet object with information about the change.
     */
    void onChange(T t, OrderedCollectionChangeSet changeSet);
}
