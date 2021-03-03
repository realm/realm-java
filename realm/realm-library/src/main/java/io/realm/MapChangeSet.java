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

package io.realm;

import io.realm.internal.OsMapChangeSet;

/**
 * TODO
 */
public interface MapChangeSet<T> {
    long getDeletionCount();
    T[] getInsertions();
    T[] getModifications();
    boolean isEmpty();
}

/**
 * TODO
 *
 * @param <T>
 */
class MapChangeSetImpl<T> implements MapChangeSet<T> {

    private final MapChangeSet<T> delegate;

    public MapChangeSetImpl(MapChangeSet<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public long getDeletionCount() {
        return delegate.getDeletionCount();
    }

    @Override
    public T[] getInsertions() {
        return delegate.getInsertions();
    }

    @Override
    public T[] getModifications() {
        return delegate.getModifications();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }
}

/**
 * TODO
 */
class StringMapChangeSet implements MapChangeSet<String> {

    private final OsMapChangeSet osMapChangeSet;

    public StringMapChangeSet(long nativePtr) {
        osMapChangeSet = new OsMapChangeSet(nativePtr);
    }

    @Override
    public long getDeletionCount() {
        return osMapChangeSet.getDeletionCount();
    }

    @Override
    public String[] getInsertions() {
        return osMapChangeSet.getStringKeyInsertions();
    }

    @Override
    public String[] getModifications() {
        return osMapChangeSet.getStringKeyModifications();
    }

    @Override
    public boolean isEmpty() {
        return osMapChangeSet.isEmpty();
    }
}
