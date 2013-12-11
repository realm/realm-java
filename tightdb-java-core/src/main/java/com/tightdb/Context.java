package com.tightdb;

import java.util.ArrayList;
import java.util.List;

class Context {

    private List<Long> abandonedSubtables  = new ArrayList<Long>();
    private List<Long> abandonedTableViews  = new ArrayList<Long>();
    private List<Long> abandonedQueries = new ArrayList<Long>();
    
    private boolean isFinalized = false;

    public void executeDelayedDisposal() {
        synchronized (this) {
            for (long nativePointer: abandonedSubtables){
                // Make sure table is valid before closing it, otherwise Illegal State Exception is thrown
                if (Table.nativeIsValid(nativePointer)) {
                    Table.nativeClose(nativePointer);
                }
            }
            abandonedSubtables.clear();

            for (long nativePointer: abandonedTableViews){
                TableView.nativeClose(nativePointer);
            }
            abandonedTableViews.clear();
            
            for (long nativePointer: abandonedQueries){
                TableQuery.nativeClose(nativePointer);
            }
            abandonedQueries.clear();
        }
    }

    public void asyncDisposeTable(long nativePointer, boolean isRoot) {
        synchronized (this) {
            if (isRoot || isFinalized) {
                Table.nativeClose(nativePointer);
            }
            else {
                abandonedSubtables.add(nativePointer);
            }
        }
    }

    public void asyncDisposeTableView(long nativePointer) {
        synchronized (this) {
            if (isFinalized) {
                TableView.nativeClose(nativePointer);
            }
            else {
                abandonedTableViews.add(nativePointer);
            }
        }
    }

    public void asyncDisposeQuery(long nativePointer) {
        synchronized (this) {
            if (isFinalized) {
                TableQuery.nativeClose(nativePointer);
            }
            else {
                abandonedQueries.add(nativePointer);
            }
        }
    }

    public void asyncDisposeGroup(long nativePointer) {
        synchronized (this) {
            Group.nativeClose(nativePointer);
        }
    }

    protected void finalize() {
        synchronized (this) {
            isFinalized = true;
        }
        executeDelayedDisposal();
    }
}
