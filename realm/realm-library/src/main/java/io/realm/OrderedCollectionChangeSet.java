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
 * This interface describes the changes made to a collection during the last update.
 * <p>
 * {@link OrderedCollectionChangeSet} is passed to the {@link OrderedRealmCollectionChangeListener} which is registered
 * by {@link RealmResults#addChangeListener(OrderedRealmCollectionChangeListener)}.
 * <p>
 * The change information is available in two formats: a simple array of row indices in the collection for each type of
 * change, and an array of {@link Range}s.
 */
public interface OrderedCollectionChangeSet {
    /**
     * The indices of objects in the previous version of the collection which have been removed from this one.
     *
     * @return the indices array. A zero-sized array will be returned if no deletion in the change set.
     */
    long[] getDeletions();

    /**
     * The indices in the new version of the collection which were newly inserted.
     *
     * @return the indices array. A zero-sized array will be returned if no insertion in the change set.
     */
    long[] getInsertions();

    /**
     * The indices in the new version of the collection which were modified.
     * <p>
     * For {@link RealmResults}, this means that one or more of the properties of the object at that index were
     * modified (or an object linked to by that object was modified).
     *
     * @return the indices array. A zero-sized array will be returned if no modification in the change set.
     */
    long[] getChanges();

    /**
     * The ranges of objects in the previous version of the collection which have been removed from this one.
     *
     * @return the {@link Range} array. A zero-sized array will be returned if no deletion in the change set.
     */
    Range[] getDeletionRanges();

    /**
     * The ranges of objects in the new version of the collection which were newly inserted.
     *
     * @return the {@link Range} array. A zero-sized array will be returned if no insertion in the change set.
     */
    Range[] getInsertionRanges();

    /**
     * The ranges of objects in the new version of the collection which were modified.
     *
     * @return the {@link Range} array. A zero-sized array will be returned if no modification in the change set.
     */
    Range[] getChangeRanges();

    /**
     *
     */
    class Range {
        /**
         * The start index of this change range.
         */
        public final long startIndex;

        /**
         * How many elements are inside this range.
         */
        public final long length;

        /**
         * Creates a {@link Range} with given start index and length.
         *
         * @param startIndex the start index of this change range.
         * @param length how many elements are inside this range.
         */
        public Range(long startIndex, long length) {
            this.startIndex = startIndex;
            this.length = length;
        }
    }
}
