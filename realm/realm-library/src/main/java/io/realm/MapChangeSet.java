/*
 * Copyright 2021 Realm Inc.
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

import io.realm.internal.OsMapChangeSet;

/**
 * This interface describes the changes made to a map during the last update.
 * <p>
 * {@link MapChangeSet} is passed to the {@link MapChangeListener} which is registered
 * by {@link RealmMap#addChangeListener(MapChangeListener)}.
 */
public interface MapChangeSet<T> {

    /**
     * The number of entries that have been deleted in the previous version of the map.
     *
     * @return the number of deletions.
     */
    long getDeletionsCount();

    /**
     * Array containing the keys that have been inserted in the previous version of the map.
     *
     * @return array with the keys that have been inserted.
     */
    T[] getInsertions();

    /**
     * Array containing the keys or the values that have been modified in the previous version of
     * the map.
     *
     * @return array with the keys that have been modified.
     */
    T[] getChanges();

    /**
     * Whether the change set is empty or not. This is needed to detect whether a notification has
     * been triggered right after subscription.
     *
     * @return whether the change set contains changes.
     */
    boolean isEmpty();
}

/**
 * Generic implementation of a {@link MapChangeSet}. This class forwards the fetching of the changes
 * to a delegate according to the key type. For now only {@code String} keys are allowed.
 *
 * @param <K> the type of the keys stored in the map
 */
class MapChangeSetImpl<K> implements MapChangeSet<K> {

    private final MapChangeSet<K> delegate;

    MapChangeSetImpl(MapChangeSet<K> delegate) {
        this.delegate = delegate;
    }

    @Override
    public long getDeletionsCount() {
        return delegate.getDeletionsCount();
    }

    @Override
    public K[] getInsertions() {
        return delegate.getInsertions();
    }

    @Override
    public K[] getChanges() {
        return delegate.getChanges();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }
}

/**
 * Delegate for {@code String} keys. Used for changes in {@link RealmDictionary}.
 */
class StringMapChangeSet implements MapChangeSet<String> {

    private final OsMapChangeSet osMapChangeSet;

    StringMapChangeSet(long nativePtr) {
        osMapChangeSet = new OsMapChangeSet(nativePtr);
    }

    @Override
    public long getDeletionsCount() {
        return osMapChangeSet.getDeletionCount();
    }

    @Override
    public String[] getInsertions() {
        return osMapChangeSet.getStringKeyInsertions();
    }

    @Override
    public String[] getChanges() {
        return osMapChangeSet.getStringKeyModifications();
    }

    @Override
    public boolean isEmpty() {
        return osMapChangeSet.isEmpty();
    }
}
