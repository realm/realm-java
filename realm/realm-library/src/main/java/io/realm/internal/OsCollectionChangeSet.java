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

package io.realm.internal;

import java.util.Arrays;

import javax.annotation.Nullable;

import io.realm.OrderedCollectionChangeSet;
import io.realm.internal.sync.OsSubscription;

/**
 * Implementation of {@link OrderedCollectionChangeSet}. This class holds a pointer to the Object Store's
 * OsCollectionChangeSet and read from it only when needed. Creating an Java object from JNI when the collection
 * notification arrives, is avoided since we also support the collection listeners without a change set parameter,
 * parsing the change set may not be necessary all the time.
 */
public class OsCollectionChangeSet implements OrderedCollectionChangeSet, NativeObject {

    // Used in JNI.
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_DELETION = 0;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_INSERTION = 1;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_MODIFICATION = 2;
    // Max array length is VM dependent. This is a safe value.
    // See http://stackoverflow.com/questions/3038392/do-java-arrays-have-a-maximum-size
    @SuppressWarnings({"WeakerAccess", "unused"})
    public static final int MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;

    private static long finalizerPtr = nativeGetFinalizerPtr();
    private final long nativePtr;
    private final boolean firstAsyncCallback;
    protected final OsSubscription subscription;
    protected final boolean isPartialRealm;

    public OsCollectionChangeSet(long nativePtr, boolean firstAsyncCallback) {
        this(nativePtr, firstAsyncCallback, null, false);
    }

    public OsCollectionChangeSet(long nativePtr, boolean firstAsyncCallback, @Nullable OsSubscription subscription, boolean isPartialRealm) {
        this.nativePtr = nativePtr;
        this.firstAsyncCallback = firstAsyncCallback;
        this.subscription = subscription;
        this.isPartialRealm = isPartialRealm;
        NativeContext.dummyContext.addReference(this);
    }

    @Override
    public State getState() {
        throw new UnsupportedOperationException("This method should be overridden in a subclass");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getDeletions() {
        return nativeGetIndices(nativePtr, TYPE_DELETION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getInsertions() {
        return nativeGetIndices(nativePtr, TYPE_INSERTION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getChanges() {
        return nativeGetIndices(nativePtr, TYPE_MODIFICATION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Range[] getDeletionRanges() {
        return longArrayToRangeArray(nativeGetRanges(nativePtr, TYPE_DELETION));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Range[] getInsertionRanges() {
        return longArrayToRangeArray(nativeGetRanges(nativePtr, TYPE_INSERTION));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Range[] getChangeRanges() {
        return longArrayToRangeArray(nativeGetRanges(nativePtr, TYPE_MODIFICATION));
    }

    @Override
    public Throwable getError() {
        if (subscription != null && subscription.getState() == OsSubscription.SubscriptionState.ERROR) {
            return subscription.getError();
        }
        return null;
    }

    @Override
    public boolean isCompleteResult() {
        throw new UnsupportedOperationException("This method should be overridden in a subclass");
    }

    public boolean isRemoteDataLoaded() {
        if (!isPartialRealm) {
            return true;
        } else if (subscription == null) {
            // This will in some cases return false positives, like adding change listeners
            // to synchronous queries. For now this is acceptable.
            return false;
        } else {
            return subscription.getState() == OsSubscription.SubscriptionState.COMPLETE;
        }
    }

    /**
     * Returns {@code true} if this is the first time an asynchronous query returns a result, i.e.
     * the query completed. 
     */
    public boolean isFirstAsyncCallback() {
        return firstAsyncCallback;
    }

    /**
     * Returns {@code true} if this changeset is empty, and doesn't contain any relevant changes.
     */
    public boolean isEmpty() {
        // Since this wrap a Object Store changeset, it will always contains changes if an
        // Object Store changeset exists.
        return nativePtr == 0;
    }

    // Convert long array returned by the nativeGetXxxRanges() to Range array.
    private Range[] longArrayToRangeArray(int[] longArray) {
        //noinspection ConstantConditions
        if (longArray == null) {
            // Returns a size 0 array so we know JNI gets called.
            return new Range[0];
        }

        Range[] ranges = new Range[longArray.length / 2];
        for (int i = 0; i < ranges.length; i++) {
            ranges[i] = new Range(longArray[i * 2], longArray[i * 2 + 1]);
        }
        return ranges;
    }

    @Override
    public String toString() {
        if (nativePtr == 0)  {
            return "Change set is empty.";
        }

        String string = "Deletion Ranges: " +
                Arrays.toString(getDeletionRanges()) +
                "\n" +
                "Insertion Ranges: " +
                Arrays.toString(getInsertionRanges()) +
                "\n" +
                "Change Ranges: " +
                Arrays.toString(getChangeRanges());
        return string;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return finalizerPtr;
    }

    private native static long nativeGetFinalizerPtr();

    // Returns the ranges as a long array. eg.: [startIndex1, length1, startIndex2, length2, ...]
    private native static int[] nativeGetRanges(long nativePtr, int type);

    // Returns the indices array.
    private native static int[] nativeGetIndices(long nativePtr, int type);
}
