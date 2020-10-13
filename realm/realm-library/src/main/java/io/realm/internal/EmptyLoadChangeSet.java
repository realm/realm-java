/*
 * Copyright 2018 Realm Inc.
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

import io.realm.RealmResults;

/**
 * Empty changeset used if {@link RealmResults#load()} is called manually or if no collection
 * changeset was available but the subscription was updated.
 */
public class EmptyLoadChangeSet extends OsCollectionChangeSet {

    private static final int[] NO_INDEX_CHANGES = new int[0];
    private static final Range[] NO_RANGE_CHANGES = new Range[0];

    public EmptyLoadChangeSet(boolean firstCallback) {
        super(0, firstCallback);
    }

    public EmptyLoadChangeSet() {
        super(0, true);
    }

    @Override
    public State getState() {
        return State.INITIAL;
    }

    @Override
    public int[] getDeletions() {
        return NO_INDEX_CHANGES;
    }

    @Override
    public int[] getInsertions() {
        return NO_INDEX_CHANGES;
    }

    @Override
    public int[] getChanges() {
        return NO_INDEX_CHANGES;
    }

    @Override
    public Range[] getDeletionRanges() {
        return NO_RANGE_CHANGES;
    }

    @Override
    public Range[] getInsertionRanges() {
        return NO_RANGE_CHANGES;
    }

    @Override
    public Range[] getChangeRanges() {
        return NO_RANGE_CHANGES;
    }

    @Override
    public Throwable getError() {
        return null;
    }

    @Override
    public boolean isFirstAsyncCallback() {
        return super.isFirstAsyncCallback();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public long getNativePtr() {
        return super.getNativePtr();
    }

    @Override
    public long getNativeFinalizerPtr() {
        return super.getNativeFinalizerPtr();
    }
}
