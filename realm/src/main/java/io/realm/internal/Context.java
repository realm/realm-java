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

import android.util.Log;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.realm.internal.log.RealmLog;

public class Context {

    // Each group of related Realm objects will have a Context object in the root.
    // The root can be a table, a group, or a shared group.
    // The Context object is used to store a list of native pointers 
    // whose disposal need to be handed over from the garbage 
    // collection thread to the users thread.

    private List<Long> abandonedTables = new ArrayList<Long>();
    private List<Long> abandonedTableViews = new ArrayList<Long>();
    private List<Long> abandonedQueries = new ArrayList<Long>();

    HashMap<Reference<?>, Integer> rowReferences = new HashMap<Reference<?>, Integer>();
    ReferenceQueue<NativeObject> referenceQueue = new ReferenceQueue<NativeObject>();

    private boolean isFinalized = false;

    public void executeDelayedDisposal() {
        synchronized (this) {
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

            cleanRows();
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
        executeDelayedDisposal();
    }
}
