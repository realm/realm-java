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

import io.realm.internal.OsCollectionChangeSet;

/**
 * This class describes the changes made to a set during the last update.
 * <p>
 * {@link SetChangeSet} is passed to the {@link SetChangeListener} which is registered
 * by {@link RealmSet#addChangeListener(SetChangeListener)}.
 */
public class SetChangeSet {

    // Internally, this change set is like a list change set's but without the indices
    private final OsCollectionChangeSet osCollectionChangeSet;

    public SetChangeSet(OsCollectionChangeSet osCollectionChangeSet) {
        this.osCollectionChangeSet = osCollectionChangeSet;
    }

    /**
     * The number of entries that have been inserted.
     *
     * @return the number of insertions.
     */
    public int getNumberOfInsertions() {
        return osCollectionChangeSet.getInsertions().length;
    }

    /**
     * The number of entries that have been deleted
     *
     * @return the number of deletions
     */
    public int getNumberOfDeletions() {
        return osCollectionChangeSet.getDeletions().length;
    }

    /**
     * Whether the change set is empty or not. This is needed to detect whether a notification has
     * been triggered right after subscription.
     *
     * @return whether the change set contains changes.
     */
    public boolean isEmpty() {
        // Since this wraps an Object Store change set, it will always contains changes if an
        // Object Store change set exists.
        return osCollectionChangeSet.getNativePtr() == 0;
    }
}
