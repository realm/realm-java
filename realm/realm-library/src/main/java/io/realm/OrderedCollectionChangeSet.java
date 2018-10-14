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
    public enum State {
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
         * <p>
         * For partially synchronized Realms, it is only possible to get into this state if an error
         * happened while evaluating the query on the server or some other error prevented data from
         * being downloaded.
         * <p>
         * In this state, the content of the {@link RealmResults} is undefined.
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
     * Returns {@code true} if the query result is considered "complete". For all local Realms, or
     * fully synchronized Realms, this method will always return {@code true}.
     * <p>
     * This method thus only makes sense for query-based synchronized Realms.
     * <p>
     * For those Realms, data is only downloaded when queried which means that until the data is
     * downloaded, a local query might return a query result that would not have been possible on a
     * fully synchronized Realm.
     * <p>
     * Consider the following case:
     * <ol>
     *   <li>An app is online and makes a query for all messages containing the word "Realm".</li>
     *   <li>Partial synchronization downloads all those messages.</li>
     *   <li>The app goes offline.</li>
     *   <li>The app makes an offline query against all messages containing the word "Database".</li>
     * </ol>
     *
     * Here there are two situations where the query result might be considered "incomplete".
     * <p>
     * The first is when the "Realm" query runs for the first time. The local query will finish
     * faster than the network can download data so the query will initially report an empty
     * incomplete query result.
     * <p>
     * The second is when the "Database" query is run. The initial query result will not be
     * empty, but contain all messages that contain both "Realm" and "Database", as they are already
     * available offline.
     * <p>
     * In both cases, a new notification will be triggered as soon as the device is able to download
     * the data required to produce a "complete" query result.
     *
     * @return {@code true} if the query result is fully consistent with the server at some point in
     * time. {@code false} if the query was executed while the device was offline or all data
     * has not been downloaded yet.
     */
    boolean isCompleteResult();

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
