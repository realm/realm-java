package io.realm.internal;

import android.os.Debug;
import android.util.Log;

import java.util.Arrays;

public class BatchDeleter {
    public static class PointerVector {
        private static final int INITIAL_SIZE = 4096;
        private long[] pointers = new long[INITIAL_SIZE];
        private int length;

        public void add(long pointer) {
            if (length < pointers.length) {
                pointers[length] = pointer;
                length ++;
            } else {
                // Double the array size.
                pointers = Arrays.copyOf(pointers, pointers.length * 2);
                pointers[length] = pointer;
                length ++;
            }
        }

        public void clear() {
            length = 0;
        }
    }

    private PointerVector linkViews;
    private PointerVector rows;

    public void add(NativeObjectReference reference) {
        switch (reference.type) {
            case NativeObjectReference.TYPE_LINK_VIEW:
                if (linkViews == null) {
                    linkViews = new PointerVector();
                }
                linkViews.add(reference.nativePointer);
                break;
            case NativeObjectReference.TYPE_ROW:
                if (rows == null) {
                    rows = new PointerVector();
                }
                rows.add(reference.nativePointer);
                break;
        }
    }

    public void dealloc() {
        long[] linkViewPointers = null;
        long[] rowPointers = null;
        if (linkViews != null) {
            linkViewPointers = linkViews.pointers;
            if (linkViewPointers.length != linkViews.length)  {
                linkViewPointers[linkViews.length] = 0;
            }
        }
        if (rows != null) {
            rowPointers = rows.pointers;
            if (rowPointers.length != rows.length)  {
                rowPointers[rows.length] = 0;
            }
        }
        deleteNativePointers(linkViewPointers, rowPointers);
        if(linkViews != null) {
            linkViews.clear();
        }
        if(rows != null) {
            rows.clear();
        }
    }

    //public native static void deleteNativePointers(int[] types, long[]... pointers);
    public native static void deleteNativePointers(long[] linkViews, long[] rows);
}
