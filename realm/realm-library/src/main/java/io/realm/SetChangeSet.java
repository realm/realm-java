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
 * TODO
 */
public class SetChangeSet {

    private final OsCollectionChangeSet osCollectionChangeSet;

    public SetChangeSet(OsCollectionChangeSet osCollectionChangeSet) {
        this.osCollectionChangeSet = osCollectionChangeSet;
    }

    /**
     * TODO
     *
     * @return
     */
    public int getNumberOfInsertions() {
        return osCollectionChangeSet.getInsertions().length;
    }

    /**
     * TODO
     *
     * @return
     */
    public int getNumberOfModifications() {
        return osCollectionChangeSet.getChanges().length;
    }

    /**
     * TODO
     *
     * @return
     */
    public int getNumberOfDeletions() {
        return osCollectionChangeSet.getDeletions().length;
    }

    public boolean isEmpty() {
        // Since this wrap a Object Store changeset, it will always contains changes if an
        // Object Store changeset exists.
        return osCollectionChangeSet.getNativePtr() == 0;
    }
}
