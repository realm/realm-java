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

import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.List;

public class Context {

    // Pool to hold the phantom references.
    // The size of array for storing phantom references will never decrease. Instead, we use another array to hold the
    // index of the free slot. When adding the reference, pick the last index from freeIndexList and put the reference
    // to the corresponding slot. When removing the reference, simply add the index to the end of freeIndexList without
    // setting the corresponding slot to null for efficiency reasons. The reference will be freed finally when the slot
    // gets overwritten or the whole context gets freed.
    private static class ReferencesPool {
        ArrayList<NativeObjectReference> pool = new ArrayList<NativeObjectReference>();
        ArrayList<Integer> freeIndexList = new ArrayList<Integer>();

        void add(NativeObjectReference ref) {
            if (pool.size() <= ref.refIndex) {
                pool.add(ref);
            } else {
                pool.set(ref.refIndex, ref);
            }
        }

        Integer getFreeIndex() {
            Integer index;
            int freeIndexListSize = freeIndexList.size();
            if (freeIndexListSize == 0) {
                index = pool.size();
            } else {
                index = freeIndexList.remove(freeIndexListSize - 1);
            }
            return index;
        }
    }

    // Each group of related Realm objects will have a Context object in the root.
    // The root can be a table, a group, or a shared group.
    // The Context object is used to store a list of native pointers 
    // whose disposal need to be handed over from the garbage 
    // collection thread to the users thread.

    private List<Long> abandonedTables = new ArrayList<Long>();
    private List<Long> abandonedTableViews = new ArrayList<Long>();
    private List<Long> abandonedQueries = new ArrayList<Long>();

    private ReferencesPool referencesPool = new ReferencesPool();
    private ReferenceQueue<NativeObject> referenceQueue = new ReferenceQueue<NativeObject>();

    private boolean isFinalized = false;

    public synchronized void addReference(int type, NativeObject referent) {
        referencesPool.add(new NativeObjectReference(type, referent, referenceQueue, referencesPool.getFreeIndex()));
    }

    public synchronized void executeDelayedDisposal() {
        for (int i = 0; i < abandonedTables.size(); i++) {
            long nativePointer = abandonedTables.get(i);
            Table.nativeClose(nativePointer);
        }
        abandonedTables.clear();

        for (int i = 0; i < abandonedTableViews.size(); i++) {
            long nativePointer = abandonedTableViews.get(i);
            TableView.nativeClose(nativePointer);
        }
        abandonedTableViews.clear();

        for (int i = 0; i < abandonedQueries.size(); i++) {
            long nativePointer = abandonedQueries.get(i);
            TableQuery.nativeClose(nativePointer);
        }
        abandonedQueries.clear();

        cleanNativeReferences();
    }

    private void cleanNativeReferences() {
        NativeObjectReference reference = (NativeObjectReference) referenceQueue.poll();
        while (reference != null) {
            // Dealloc the native resources
            reference.cleanup();
            // Inline referencesPool.remove() to make it faster.
            // referencesPool.pool.set(index, null); is not really needed. Make it faster by not
            // setting the slot to null.
            referencesPool.freeIndexList.add(reference.refIndex);
            reference = (NativeObjectReference) referenceQueue.poll();
        }
    }

    public void asyncDisposeTable(long nativePointer, boolean isRoot) {
        if (isRoot || isFinalized) {
            Table.nativeClose(nativePointer);
        }
        else {
            abandonedTables.add(nativePointer);
        }
    }

    public void asyncDisposeTableView(long nativePointer) {
        if (isFinalized) {
            TableView.nativeClose(nativePointer);
        }
        else {
            abandonedTableViews.add(nativePointer);
        }
    }

    public void asyncDisposeQuery(long nativePointer) {
        if (isFinalized) {
            TableQuery.nativeClose(nativePointer);
        }
        else {
            abandonedQueries.add(nativePointer);
        }
    }

    protected void finalize() throws Throwable {
        synchronized (this) {
            isFinalized = true;
        }
        executeDelayedDisposal();
        super.finalize();
    }
}
