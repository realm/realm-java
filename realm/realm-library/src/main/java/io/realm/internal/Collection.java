/*
 * Copyright 2014 Realm Inc.
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

public class Collection implements NativeObject {
    
    private final long nativePtr;
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();
    private final Context context;
    private final TableQuery query;

    // Public for static checking in JNI
    public static final byte AGGREGATE_FUNCTION_MINIMUM = 1;
    public static final byte AGGREGATE_FUNCTION_MAXIMUM = 2;
    public static final byte AGGREGATE_FUNCTION_AVERAGE = 3;
    public static final byte AGGREGATE_FUNCTION_SUM     = 4;

    protected Collection(SharedRealm sharedRealm, TableQuery query, long indices[], boolean[] orders) {
        this.context = sharedRealm.context;
        this.query = query;

        this.nativePtr = nativeCreateResults(sharedRealm.getNativePtr(), query.getNativePtr(), indices, orders);
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    private static native long nativeGetFinalizerPtr();
    private static native long nativeCreateResults(long sharedRealmNativePtr, long queryNativePtr, long[] columnIndices,
                                                   boolean[] orders);
    private static native long nativeCreateSnapshot(long nativePtr);
    private static native long nativeGetRow(long nativePtr, int index);
    private static native boolean nativeContains(long nativePtr, long nativeRowPtr);
    private static native void nativeClear(long nativePtr);
    private static native long nativeSize(long nativePtr);
    private static native Object nativeAggregate(long nativePtr, long columnIndex, byte aggregateFunc);
    private static native long nativeSort(long nativePtr, long[] columnIndices, boolean[] orders);
    private native long nativeAddListener(long nativePtr);
}
