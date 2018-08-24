package io.realm.internal.core;

import io.realm.internal.NativeObject;
import io.realm.internal.OsSharedRealm;
import io.realm.internal.TableQuery;

/**
 * Java class wrapping the native {@code realm::DescriptorOrdering} class. This class
 * is used to track sort/distinct/limit criterias on a query.
 */
public class DescriptorOrdering implements NativeObject {

    private static final long nativeFinalizerMethodPtr = nativeGetFinalizerMethodPtr();
    private final long nativePtr;

    // Used to track constraints are already set. Throw in that case.
    // This is just to mirror old behaviour. We should consider lifting this restriction,
    // although it seems hard to find a use case that is not a logical bug in nested query
    // construction.
    private boolean sortDefined = false;
    private boolean distinctDefined = false;
    private boolean limitDefined = false;

    /**
     * Creates a standalone DescriptorOrdering. This only achieves meaning when combined with
     * a RealmQuery object.
     *
     * @see io.realm.internal.OsResults#createFromQuery(OsSharedRealm, TableQuery, DescriptorOrdering)
     */
    public DescriptorOrdering() {
        nativePtr = nativeCreate();
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerMethodPtr;
    }

    /**
     * Append a sort criteria.
     *
     * @param descriptor description of the sort
     */
    public void appendSort(QueryDescriptor descriptor) {
        if (sortDefined) {
            throw new IllegalStateException("A sorting order was already defined. It cannot be redefined");
        }
        nativeAppendSort(nativePtr, descriptor);
        sortDefined = true;
    }

    /**
     * Append a distinct criteria.
     *
     * @param descriptor description of the distinct critia
     */
    public void appendDistinct(QueryDescriptor descriptor) {
        if (distinctDefined) {
            throw new IllegalStateException("A distinct field was already defined. It cannot be redefined");
        }
        nativeAppendDistinct(nativePtr, descriptor);
        distinctDefined = true;
    }

    /**
     * Sets a limit criteria.
     */
    public void setLimit(long limit) {
        if (limitDefined) {
            throw new IllegalStateException("A limit was already set. It cannot be redefined.");
        }
        nativeSetLimit(nativePtr, limit);
        limitDefined = true;
    }

    /**
     * Returns true if no descriptors or limits have been added.
     */
    public boolean isEmpty() {
        return nativeIsEmpty(nativePtr);
    }


    private static native long nativeGetFinalizerMethodPtr();
    private static native long nativeCreate();
    private static native void nativeAppendSort(long descriptorPtr, QueryDescriptor sortDesc);
    private static native void nativeAppendDistinct(long descriptorPtr, QueryDescriptor sortDesc);
    private static native void nativeSetLimit(long descriptorPtr, long limit);
    private static native boolean nativeIsEmpty(long descriptorPtr);

}
