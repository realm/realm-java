/*
 * Copyright 2020 Realm Inc.
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

package io.realm.internal;

import io.realm.RealmConfiguration;

/**
 * The {@code Freezable} interface enable {@link io.realm.RealmCollection}s to be frozen. A frozen collection is a snapshot
 * of the collection itself at the moment of calling {@link #freeze()}. The contents of a collection cannot be modified after
 * freezing it and the collection itself is not bound to any thread anymore.
 *
 * @param <T> the type of content held by the collection
 */
public interface Freezable<T> {

    /**
     * Returns a frozen snapshot for a Realm collection. The frozen copy can be read and queried from any thread without
     * throwing an {@link IllegalStateException}.
     * <p>
     * Freezing a collection also creates a Realm which has its own lifecycle, but if the live Realm that spawned the
     * original collection is fully closed (i.e. all instances across all threads are closed), the frozen Realm and this
     * collection will be closed as well.
     * <p>
     * Frozen collections can be queried as normal, but trying to mutate them in any way or attempting to register a listener
     * will throw an {@link IllegalStateException}.
     * <p>
     * Note: Keeping a large number of frozencollections with different versions alive can have a negative impact on the
     * file size of the Realm. In order to avoid such a situation, it is possible to set
     * {@link RealmConfiguration.Builder#maxNumberOfActiveVersions(long)}.
     *
     * @return a frozen copy of this collection.
     * @throws IllegalStateException if this method is called from inside a write transaction.
     */
    T freeze();
}
