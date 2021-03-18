/*
 * Copyright 2021 Realm Inc.
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

import java.util.Collection;

import javax.annotation.Nullable;

public class OsSet implements NativeObject {

    public enum ExternalCollectionOperation {
        CONTAINS_ALL,
        ADD_ALL,
        REMOVE_ALL,
        RETAIN_ALL
    }

    private static final int VALUE_FOUND = 1;
    private static final int VALUE_NOT_FOUND = 0;
    private static final int NULL_VALUE_NOT_FOUND = -13;

    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    private final long nativePtr;
    private final NativeContext context;
    private final OsSharedRealm osSharedRealm;

    public OsSet(UncheckedRow row, long columnKey) {
        this.osSharedRealm = row.getTable().getSharedRealm();
        this.nativePtr = nativeCreate(osSharedRealm.getNativePtr(), row.getNativePtr(), columnKey);
        this.context = osSharedRealm.context;
        context.addReference(this);
    }

    // Used to freeze sets
    private OsSet(OsSharedRealm osSharedRealm, long nativePtr) {
        this.osSharedRealm = osSharedRealm;
        this.nativePtr = nativePtr;
        this.context = osSharedRealm.context;
        context.addReference(this);
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    public boolean isValid() {
        return nativeIsValid(nativePtr);
    }

    public Object getValueAtIndex(int position) {
        return nativeGetValueAtIndex(nativePtr, position);
    }

    public long size() {
        return nativeSize(nativePtr);
    }

    // ----------------------------------------------------
    // String operations
    // ----------------------------------------------------

    public boolean contains(@Nullable String value) {
        if (value == null) {
            return nativeContainsNull(nativePtr);
        } else {
            return nativeContainsString(nativePtr, (String) value);
        }
    }

    public boolean containsAllString(Collection<?> collection) {
        // TODO: extract to its own method
        String[] stringArray = new String[collection.size()];

        Integer nullSentinelIndex = null;

        int totalLength = 0;
        for (Object value : collection) {
            if (value == null) {
                if (nullSentinelIndex == null) {
                    nullSentinelIndex = totalLength;
                    totalLength++;
                }
            } else {
                // TODO: maybe can be done better...?
                if (value instanceof String) {
                    stringArray[totalLength] = (String) value;
                    totalLength++;
                } else {
                    // Collection is not contained if it is not the same type
                    return false;
                }
            }
        }

        int nullSentinel = nullSentinelIndex == null ? NULL_VALUE_NOT_FOUND : nullSentinelIndex;
        return nativeContainsAllString(nativePtr, stringArray, totalLength, nullSentinel);
    }

    public boolean add(@Nullable String value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeAddNull(nativePtr);
        } else {
            indexAndFound = nativeAddString(nativePtr, value);
        }
        return indexAndFound[1] != VALUE_NOT_FOUND;
    }

    public <E> boolean addAllString(Collection<? extends E> collection) {
        // TODO: extract to its own method
        String[] stringArray = new String[collection.size()];

        Integer nullSentinelIndex = null;

        int totalLength = 0;
        for (E value : collection) {
            if (value == null) {
                if (nullSentinelIndex == null) {
                    nullSentinelIndex = totalLength;
                    totalLength++;
                }
            } else {
                // TODO: maybe can be done better...?
                if (value instanceof String) {
                    stringArray[totalLength] = (String) value;
                    totalLength++;
                } else {
                    throw new IllegalArgumentException("Invalid collection type. Set and collection must contain the same type of elements.");
                }
            }
        }

        int nullSentinel = nullSentinelIndex == null ? NULL_VALUE_NOT_FOUND : nullSentinelIndex;
        return nativeAddAllString(nativePtr, stringArray, totalLength, nullSentinel);
    }

    public boolean remove(@Nullable String value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeRemoveNull(nativePtr);
        } else {
            indexAndFound = nativeRemoveString(nativePtr, (String) value);
        }
        return indexAndFound[1] == 1;       // 1 means true, i.e. it was found
    }

    public <E> boolean removeAllString(Collection<? extends E> collection) {
        // TODO: extract to its own method
        String[] stringArray = new String[collection.size()];

        Integer nullSentinelIndex = null;

        int totalLength = 0;
        for (E value : collection) {
            if (value == null) {
                if (nullSentinelIndex == null) {
                    nullSentinelIndex = totalLength;
                    totalLength++;
                }
            } else {
                // TODO: maybe can be done better...?
                if (value instanceof String) {
                    stringArray[totalLength] = (String) value;
                    totalLength++;
                } else {
                    throw new IllegalArgumentException("Invalid collection type. Set and collection must contain the same type of elements.");
                }
            }
        }

        int nullSentinel = nullSentinelIndex == null ? NULL_VALUE_NOT_FOUND : nullSentinelIndex;
        return nativeRemoveAllString(nativePtr, stringArray, totalLength, nullSentinel);
    }

    public <E> boolean retainAllString(Collection<? extends E> collection) {
//        // Intersection with an empty collection results into an empty set
//        if (collection.isEmpty()) {
//            nativeClear(nativePtr);
//            return true;
//        }
//
//        // Remove existing duplicates and preserve ordering using a HashSet
//        Set<? extends E> setWithNoDuplicates = new HashSet<>(collection);
//
//        int size = setWithNoDuplicates.size();
//        String[] values = setWithNoDuplicates.toArray(new String[size]);
//        return funnelStringCollection(values, ExternalCollectionOperation.RETAIN_ALL);
        return false;
    }

    // ----------------------------------------------------
    // Integer operations
    // ----------------------------------------------------

    public boolean contains(@Nullable Long value) {
        if (value == null) {
            return nativeContainsNull(nativePtr);
        } else {

            return nativeContainsLong(nativePtr, value);
        }
    }

    public boolean containsAllInteger(Collection<?> collection) {
        // TODO: extract to its own method
        long[] longArray = new long[collection.size()];

        Integer nullSentinelIndex = null;

        int totalLength = 0;
        for (Object value : collection) {
            if (value == null) {
                if (nullSentinelIndex == null) {
                    nullSentinelIndex = totalLength;
                    totalLength++;
                }
            } else {
                // TODO: maybe can be done better...?
                if (value instanceof Integer) {
                    longArray[totalLength] = ((Integer) value).longValue();
                    totalLength++;
                } else {
                    // Collection is not contained if it is not the same type
                    return false;
                }
            }
        }

        int nullSentinel = nullSentinelIndex == null ? NULL_VALUE_NOT_FOUND : nullSentinelIndex;
        return nativeContainsAllLong(nativePtr, longArray, totalLength, nullSentinel);
    }

    public boolean add(@Nullable Integer value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeAddNull(nativePtr);
        } else {
            indexAndFound = nativeAddLong(nativePtr, value.longValue());
        }
        return indexAndFound[1] != VALUE_NOT_FOUND;
    }

    public <E> boolean addAllInteger(Collection<? extends E> collection) {
        // TODO: extract to its own method
        long[] longArray = new long[collection.size()];

        Integer nullSentinelIndex = null;

        int totalLength = 0;
        for (E value : collection) {
            if (value == null) {
                if (nullSentinelIndex == null) {
                    nullSentinelIndex = totalLength;
                    totalLength++;
                }
            } else {
                // TODO: maybe can be done better...?
                if (value instanceof Integer) {
                    longArray[totalLength] = ((Integer) value).longValue();
                    totalLength++;
                } else {
                    throw new IllegalArgumentException("Invalid collection type. Set and collection must contain the same type of elements.");
                }
            }
        }

        int nullSentinel = nullSentinelIndex == null ? NULL_VALUE_NOT_FOUND : nullSentinelIndex;
        return nativeAddAllLong(nativePtr, longArray, totalLength, nullSentinel);
    }

    public boolean remove(@Nullable Integer value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeRemoveNull(nativePtr);
        } else {
            indexAndFound = nativeRemoveLong(nativePtr, value.longValue());
        }
        return indexAndFound[1] == 1;       // 1 means true, i.e. it was found
    }

    public <E> boolean removeAllInteger(Collection<? extends E> collection) {
        // TODO: extract to its own method
        long[] longArray = new long[collection.size()];

        Integer nullSentinelIndex = null;

        int totalLength = 0;
        for (E value : collection) {
            if (value == null) {
                if (nullSentinelIndex == null) {
                    nullSentinelIndex = totalLength;
                    totalLength++;
                }
            } else {
                // TODO: maybe can be done better...?
                if (value instanceof Integer) {
                    longArray[totalLength] = (int) value;
                    totalLength++;
                } else {
                    throw new IllegalArgumentException("Invalid collection type. Set and collection must contain the same type of elements.");
                }
            }
        }

        int nullSentinel = nullSentinelIndex == null ? NULL_VALUE_NOT_FOUND : nullSentinelIndex;
        return nativeRemoveAllLong(nativePtr, longArray, totalLength, nullSentinel);
    }

    public <E> boolean retainAllInteger(Collection<? extends E> collection) {
//        // TODO: extract to its own method
//        long[] longArray = new long[collection.size()];
//
//        Integer nullSentinelIndex = null;
//
//        int totalLength = 0;
//        for (E value : collection) {
//            if (value == null) {
//                if (nullSentinelIndex == null) {
//                    nullSentinelIndex = totalLength;
//                    totalLength++;
//                }
//            } else {
//                // TODO: maybe can be done better...?
//                if (value instanceof Integer) {
//                    longArray[totalLength] = (long) value;
//                    totalLength++;
//                } else {
//                    throw new IllegalArgumentException("Invalid collection type. Set and collection must contain the same type of elements.");
//                }
//            }
//        }
//
//        int nullSentinel = nullSentinelIndex == null ? NULL_VALUE_NOT_FOUND : nullSentinelIndex;
//        return nativeRetainAllLong(nativePtr, longArray, totalLength, nullSentinel);
        return false;
    }

    // ----------------------------------------------------
    // Set operations
    // ----------------------------------------------------

    public boolean containsAll(OsSet otherRealmSet) {
        return nativeContainsAll(nativePtr, otherRealmSet.getNativePtr());
    }

    public boolean union(OsSet otherRealmSet) {
        return nativeUnion(nativePtr, otherRealmSet.getNativePtr());
    }

    public boolean asymmetricDifference(OsSet otherSet) {
        return nativeAsymmetricDifference(nativePtr, otherSet.getNativePtr());
    }

    public boolean intersect(OsSet otherSet) {
        return nativeIntersect(nativePtr, otherSet.getNativePtr());
    }

    public void clear() {
        nativeClear(nativePtr);
    }

    public OsSet freeze(OsSharedRealm frozenSharedRealm) {
        long frozenNativePtr = nativeFreeze(this.nativePtr, frozenSharedRealm.getNativePtr());
        return new OsSet(frozenSharedRealm, frozenNativePtr);
    }

    // ----------------------------------------------------
    // Private stuff
    // ----------------------------------------------------

    private void checkCollectionType(Object[] values, Class<?> valueClass) {
        // Throw if collection and set are not the same type
        Class<?> itemClass = values[0].getClass();
        if (itemClass != valueClass) {
            throw new IllegalArgumentException("Invalid collection type. Set and collection must contain the same type of elements.");
        }
    }

    private boolean funnelStringCollection(String[] values, ExternalCollectionOperation operation) {
//        checkCollectionType(values, String.class);
//        switch (operation) {
//            case ADD_ALL:
//                return nativeAddAllString(nativePtr, values);
//            case REMOVE_ALL:
//                return nativeRemoveAllString(nativePtr, values);
//            case RETAIN_ALL:
//                return nativeRetainAllString(nativePtr, values);
//            default:
//                throw new IllegalStateException("Unexpected value: " + operation);
//        }
        return false;
    }

    private static native long nativeGetFinalizerPtr();

    private static native long nativeCreate(long sharedRealmPtr, long nativeRowPtr, long columnKey);

    private static native boolean nativeIsValid(long nativePtr);

    private static native Object nativeGetValueAtIndex(long nativePtr, int position);

    private static native long nativeSize(long nativePtr);

    private static native boolean nativeContainsNull(long nativePtr);

    private static native boolean nativeContainsString(long nativePtr, String value);

    private static native boolean nativeContainsLong(long nativePtr, long value);

    private static native long[] nativeAddNull(long nativePtr);

    private static native long[] nativeAddString(long nativePtr, String value);

    private static native long[] nativeAddLong(long nativePtr, long value);

    private static native long[] nativeRemoveNull(long nativePtr);

    private static native long[] nativeRemoveString(long nativePtr, String value);

    private static native long[] nativeRemoveLong(long nativePtr, long value);

    private static native boolean nativeContainsAllString(long nativePtr, String[] otherSet, long otherSetSize, long nullSentinelIndex);

    private static native boolean nativeContainsAllLong(long nativePtr, long[] otherSet, long otherSetSize, long nullSentinelIndex);

    private static native boolean nativeContainsAll(long nativePtr, long otherRealmSetNativePtr);

    private static native boolean nativeUnion(long nativePtr, long otherRealmSetNativePtr);

    private static native boolean nativeAddAllString(long nativePtr, String[] otherSet, long otherSetSize, long nullSentinel);

    private static native boolean nativeAddAllLong(long nativePtr, long[] otherSet, long otherSetSize, long nullSentinel);

    private static native boolean nativeAsymmetricDifference(long nativePtr, long otherRealmSetNativePtr);

    private static native boolean nativeRemoveAllString(long nativePtr, String[] otherSet, long otherSetSize, long nullSentinel);

    private static native boolean nativeRemoveAllLong(long nativePtr, long[] otherSet, long otherSetSize, long nullSentinel);

    private static native boolean nativeIntersect(long nativePtr, long otherRealmSetNativePtr);

    private static native boolean nativeRetainAllString(long nativePtr, String[] otherSet);

    private static native boolean nativeRetainAllLong(long nativePtr, long[] otherSet, long otherSetSize, long[] nullPositions, long nullPositionsSize);

    private static native void nativeClear(long nativePtr);

    private static native long nativeFreeze(long nativePtr, long frozenRealmPtr);
}
