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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Context {

    // Each group of related Realm objects will have a Context object in the root.
    // The root can be a table, a group, or a shared group.
    // The Context object is used to store a list of native pointers 
    // whose disposal need to be handed over from the garbage 
    // collection thread to the users thread.

    // Reserved to be used only as a placholder by rowReferences Map to avoid autoboxing allocations
    static final Integer ROW_REFERENCES_VALUE = 0;

    private ArrayList<Long> abandonedTables = new ArrayList<Long>();
    private ArrayList<Long> abandonedTableViews = new ArrayList<Long>();
    private ArrayList<Long> abandonedQueries = new ArrayList<Long>();

    HashMap<Reference<?>, Integer> rowReferences = new HashMap<Reference<?>, Integer>();
    ReferenceQueue<NativeObject> referenceQueue = new ReferenceQueue<NativeObject>();

    private boolean isFinalized = false;

    public void executeDelayedDisposal() {
        synchronized (this) {
            int size = abandonedTables.size();

            if(size > 0) {
                for (int i = 0; i < size; i++) {
                    Table.nativeClose(abandonedTables.get(i));
                }

                abandonedTables.clear();
            }

            size = abandonedTableViews.size();

            if(size > 0) {
                for (int i = 0; i < size; i++) {
                    TableView.nativeClose(abandonedTableViews.get(i));
                }

                abandonedTableViews.clear();
            }

            size = abandonedQueries.size();

            if(abandonedQueries.size() > 0) {

                for (int i = 0; i < size; i++) {
                    TableQuery.nativeClose(abandonedQueries.get(i));
                }

                abandonedQueries.clear();
            }

            cleanRows();
        }
    }

    // Faster version used only for context.Finalize() calls. Difference is it does not modify registries but set them to null
    private void executeDelayedDisposalFinalize() {
        synchronized (this) {

            for (long nativePointer : abandonedTables) {
                Table.nativeClose(nativePointer);
            }

            for (long nativePointer : abandonedTableViews) {
                TableView.nativeClose(nativePointer);
            }

            for (long nativePointer : abandonedQueries) {
                TableQuery.nativeClose(nativePointer);
            }

            cleanRowsFinalize();
        }
    }

    public void cleanRows() {
        NativeObjectReference reference = (NativeObjectReference) referenceQueue.poll();
        while (reference != null) {
            UncheckedRow.nativeClose(reference.nativePointer);
            rowReferences.remove(reference);
            reference = (NativeObjectReference) referenceQueue.poll();
        }
    }

    // used only by context.Finalize() -> executeDelayedDisposalFinalize..
    private void cleanRowsFinalize() {
        NativeObjectReference reference = (NativeObjectReference) referenceQueue.poll();
        while (reference != null) {
            UncheckedRow.nativeClose(reference.nativePointer);
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

    public void asyncDisposeGroup(long nativePointer) {
        Group.nativeClose(nativePointer);
    }

    public void asyncDisposeSharedGroup(long nativePointer) {
        SharedGroup.nativeClose(nativePointer);
    }

    protected void finalize() {
        synchronized (this) {
            isFinalized = true;
        }

        // Call special disposal which does not need to modify registry data because we are out of scope anyways
        executeDelayedDisposalFinalize();
    }
}
