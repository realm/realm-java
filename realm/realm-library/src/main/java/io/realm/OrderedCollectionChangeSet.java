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

import java.util.Locale;

import javax.annotation.Nullable;

/**
 * This interface describes the changes made to a collection during the last update.
 * <p>
 * {@link OrderedCollectionChangeSet} is passed to the {@link OrderedRealmCollectionChangeListener} which is registered
 * by {@link RealmResults#addChangeListener(OrderedRealmCollectionChangeListener)}.
 * <p>
 * The change information is available in two formats: a simple array of row indices in the collection for each type of
 * change, or an array of {@link Range}s.
 */
public interface OrderedCollectionChangeSet {

    /**
     * State describing the nature of the changeset.
     */
    enum State {
        /**
         * This state is used first time the callback is invoked. The query will have completed and
         * data is ready for the UI.
         */
        INITIAL,
        /**
         * This state is used for every subsequent update after the first.
         */
        UPDATE,
        /**
         * This state is used if some error occurred on the background evaluating the query.
         * <p>
         * For local and fully synchronized Realms, this state should only be encountered if the
         * Realm could not be succesfully opened in the background,.
         */
        ERROR
    }

    /**
     * Returns the state represented by this change. See {@link State} for a description of the
     * different states a changeset can be in.
     *
     * @return what kind of state is represented by this changeset.
     * @see State
     */
    State getState();

    /**
     * The deleted indices in the previous version of the collection.
     *
     * @return the indices array. A zero-sized array will be returned if no objects were deleted.
     */
    int[] getDeletions();

    /**
     * The inserted indices in the new version of the collection.
     *
     * @return the indices array. A zero-sized array will be returned if no objects were inserted.
     */
    int[] getInsertions();

    /**
     * The modified indices in the new version of the collection.
     * <p>
     * For {@link RealmResults}, this means that one or more of the properties of the object at the given index were
     * modified (or an object linked to by that object was modified).
     *
     * @return the indices array. A zero-sized array will be returned if objects were modified.
     */
    int[] getChanges();

    /**
     * The deleted ranges of objects in the previous version of the collection.
     *
     * @return the {@link Range} array. A zero-sized array will be returned if no objects were deleted.
     */
    Range[] getDeletionRanges();

    /**
     * The inserted ranges of objects in the new version of the collection.
     *
     * @return the {@link Range} array. A zero-sized array will be returned if no objects were inserted.
     */
    Range[] getInsertionRanges();

    /**
     * The modified ranges of objects in the new version of the collection.
     *
     * @return the {@link Range} array. A zero-sized array will be returned if no objects were modified.
     */
    Range[] getChangeRanges();

    /**
     * Returns any error that happened. If an error has happened, the state of the collection and other
     * changeset information is undefined. It is possible for a collection to go into an error state
     * after being created and starting to send updates.
     *
     * @return the error that happened.
     */
    @Nullable
    Throwable getError();

    /**
     *
     */
    class Range {
        /**
         * The start index of this change range.
         */
        public final int startIndex;

        /**
         * How many elements are inside this range.
         */
        public final int length;

        /**
         * Creates a {@link Range} with given start index and length.
         *
         * @param startIndex the start index of this change range.
         * @param length how many elements are inside this range.
         */
        public Range(int startIndex, int length) {
            this.startIndex = startIndex;
            this.length = length;
        }

        @Override
        public String toString() {
            return String.format(Locale.ENGLISH, "startIndex: %d, length: %d", startIndex, length);
        }
    }
}
