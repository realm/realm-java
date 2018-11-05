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
package io.realm.internal.objectstore;

import java.io.Closeable;
import java.util.Date;
import java.util.List;
import java.util.Set;

import io.realm.ImportFlag;
import io.realm.MutableRealmInteger;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.internal.NativeContext;
import io.realm.internal.OsSharedRealm;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Table;
import io.realm.internal.UncheckedRow;

/**
 * This class is a wrapper around building up object data for calling `Object::create()`
 * <p>
 * Fill the object data by calling the various `addX()` methods, then create a new Object or update
 * an existing one by calling {@link #createNewObject()} or {@link #updateExistingObject()}.
 * <p>
 * This class assumes it is only being used from within a write transaction. Using it outside one
 * will result in undefined behaviour.
 * <p>
 * The native
 * resources are created in the constructor of this class and destroyed when calling either of the
 * above two methods.
 * <p>
 * <H1>Design thoughts</H1>
 * <p>
 * Ideally we would have sent all properties across in one JNI call, but the only way to do that would
 * have been using two `Object[]` arrays which would have resulted in a ton of JNI calls back
 * again for resolving the primitive values of boxed types (since JNI do not know about boxed
 * primitives).
 * <p>
 * The upside of making a JNI call for each property is that we do minimal allocations on the Java
 * side. Also each method call is fairly lightweight as no checks are performed compared to using
 * Proxy setters and {@link io.realm.internal.UncheckedRow}. The only downside is the current need for
 * sending the key as well. Hopefully we can change that to schema indices at some point.
 * <p>
 * There is quite a few variants we can attempt to optimize this, but at this point we lack data
 * that can guide any architectural design and the only way to really find out is to build out each
 * solution and benchmark it.
 */
public class OsObjectBuilder implements Closeable {

    private final Table table;
    private final long sharedRealmPtr;
    private final long builderPtr;
    private final long tablePtr;
    private final NativeContext context;

    private static ItemCallback<? extends RealmModel> objectItemCallback = new ItemCallback<RealmModel>() {
        @Override
        public void handleItem(long listPtr, RealmModel item) {
            RealmObjectProxy proxyItem = (RealmObjectProxy) item;
            nativeAddIntegerListItem(listPtr, ((UncheckedRow) proxyItem.realmGet$proxyState().getRow$realm()).getNativePtr());
        }
    };

    private static ItemCallback<String> stringItemCallback = new ItemCallback<String>() {
        @Override
        public void handleItem(long listPtr, String item) {
            nativeAddStringListItem(listPtr, item);
        }
    };

    private static ItemCallback<Byte> byteItemCallback = new ItemCallback<Byte>() {
        @Override
        public void handleItem(long listPtr, Byte item) {
            nativeAddIntegerListItem(listPtr, item.longValue());
        }
    };

    private static ItemCallback<Short> shortItemCallback = new ItemCallback<Short>() {
        @Override
        public void handleItem(long listPtr, Short item) {
            nativeAddIntegerListItem(listPtr, item);
        }
    };

    private static ItemCallback<Integer> integerItemCallback = new ItemCallback<Integer>() {
        @Override
        public void handleItem(long listPtr, Integer item) {
            nativeAddIntegerListItem(listPtr, item);
        }
    };

    private static ItemCallback<Long> longItemCallback = new ItemCallback<Long>() {
        @Override
        public void handleItem(long listPtr, Long item) {
            nativeAddIntegerListItem(listPtr, item);
        }
    };

    private static ItemCallback<Boolean> booleanItemCallback = new ItemCallback<Boolean>() {
        @Override
        public void handleItem(long listPtr, Boolean item) {
            nativeAddBooleanListItem(listPtr, item);
        }
    };

    private static ItemCallback<Float> floatItemCallback = new ItemCallback<Float>() {
        @Override
        public void handleItem(long listPtr, Float item) {
            nativeAddFloatListItem(listPtr, item);
        }
    };

    private static ItemCallback<Double> doubleItemCallback = new ItemCallback<Double>() {
        @Override
        public void handleItem(long listPtr, Double item) {
            nativeAddDoubleListItem(listPtr, item);
        }
    };

    private static ItemCallback<Date> dateItemCallback = new ItemCallback<Date>() {
        @Override
        public void handleItem(long listPtr, Date item) {
            nativeAddDateListItem(listPtr, item.getTime());
        }
    };

    private static ItemCallback<byte[]> byteArrayItemCallback = new ItemCallback<byte[]>() {
        @Override
        public void handleItem(long listPtr, byte[] item) {
            nativeAddByteArrayListItem(listPtr, item);
        }
    };

    private static ItemCallback<MutableRealmInteger> mutableRealmIntegerItemCallback = new ItemCallback<MutableRealmInteger>() {
        @Override
        public void handleItem(long listPtr, MutableRealmInteger item) {
            Long value = item.get();
            if (value == null) {
                nativeAddNullListItem(listPtr);
            } else {
                nativeAddIntegerListItem(listPtr, value);
            }
        }
    };

    // If true, fields will not be updated if the same value would be written to it.
    private final boolean ignoreFieldsWithSameValue;

    public OsObjectBuilder(Table table, long maxColumnIndex, Set<ImportFlag> flags) {
        OsSharedRealm sharedRealm = table.getSharedRealm();
        this.sharedRealmPtr = sharedRealm.getNativePtr();
        this.table = table;
        this.tablePtr = table.getNativePtr();
        this.builderPtr = nativeCreateBuilder(maxColumnIndex + 1);
        this.context = sharedRealm.context;
        this.ignoreFieldsWithSameValue = flags.contains(ImportFlag.CHECK_SAME_VALUES_BEFORE_SET);
    }

    public void addInteger(long columnIndex, Byte val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnIndex);
        } else {
            nativeAddInteger(builderPtr, columnIndex, val);
        }
                                                    }

    public void addInteger(long columnIndex, Short val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnIndex);
        } else {
            nativeAddInteger(builderPtr, columnIndex, val);
        }
    }

    public void addInteger(long columnIndex, Integer val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnIndex);
        } else {
            nativeAddInteger(builderPtr, columnIndex, val);
        }
    }

    public void addInteger(long columnIndex, Long val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnIndex);
        } else {
            nativeAddInteger(builderPtr, columnIndex, val);
        }
    }

    public void addMutableRealmInteger(long columnIndex, MutableRealmInteger val) {
        if (val == null || val.get() == null) {
            nativeAddNull(builderPtr, columnIndex);
        } else {
            nativeAddInteger(builderPtr, columnIndex, val.get());
        }
    }

    public void addString(long columnIndex, String val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnIndex);
        } else {
            nativeAddString(builderPtr, columnIndex, val);
        }
    }

    public void addFloat(long columnIndex, Float val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnIndex);
        } else {
            nativeAddFloat(builderPtr, columnIndex, val);
        }
    }

    public void addDouble(long columnIndex, Double val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnIndex);
        } else {
            nativeAddDouble(builderPtr, columnIndex, val);
        }
    }

    public void addBoolean(long columnIndex, Boolean val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnIndex);
        } else {
            nativeAddBoolean(builderPtr, columnIndex, val);
        }
    }

    public void addDate(long columnIndex, Date val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnIndex);
        } else {
            nativeAddDate(builderPtr, columnIndex, val.getTime());
        }
    }

    public void addByteArray(long columnIndex, byte[] val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnIndex);
        } else {
            nativeAddByteArray(builderPtr, columnIndex, val);
        }
    }

    public void addNull(long columnIndex) {
        nativeAddNull(builderPtr, columnIndex);
    }

    public void addObject(long columnIndex, RealmModel val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnIndex);
        } else {
            RealmObjectProxy proxy = (RealmObjectProxy) val;
            UncheckedRow row = (UncheckedRow) proxy.realmGet$proxyState().getRow$realm();
            nativeAddObject(builderPtr, columnIndex, row.getNativePtr());
        }
    }

    private <T> void addListItem(long builderPtr, long columnIndex, List<T> list, ItemCallback<T> itemCallback) {
        if (list != null) {
            long listPtr = nativeStartList(list.size());
            for (int i = 0; i < list.size(); i++) {
                T item = list.get(i);
                if (item == null) {
                    nativeAddNullListItem(listPtr);
                } else {
                    itemCallback.handleItem(listPtr, item);
                }
            }
            nativeStopList(builderPtr, columnIndex, listPtr);
        } else {
            addEmptyList(columnIndex);
        }
    }

    public <T extends RealmModel> void addObjectList(long columnIndex, RealmList<T> list) {
        // Null objects references are not allowed. So we can optimize the JNI boundary by
        // sending all object references in one long[] array.
        if (list != null) {
            long[] rowPointers = new long[list.size()];
            for (int i = 0; i < list.size(); i++) {
                RealmObjectProxy item = (RealmObjectProxy) list.get(i);
                if (item == null) {
                    throw new IllegalArgumentException("Null values are not allowed in RealmLists containing Realm models");
                } else {
                    rowPointers[i] = ((UncheckedRow) item.realmGet$proxyState().getRow$realm()).getNativePtr();
                }
            }
            nativeAddObjectList(builderPtr, columnIndex, rowPointers);
        } else {
            nativeAddObjectList(builderPtr, columnIndex, new long[0]);
        }
    }

    public void addStringList(long columnIndex, RealmList<String> list) {
        addListItem(builderPtr, columnIndex, list, stringItemCallback);
    }

    public void addByteList(long columnIndex, RealmList<Byte> list) {
        addListItem(builderPtr, columnIndex, list, byteItemCallback);
    }

    public void addShortList(long columnIndex, RealmList<Short> list) {
        addListItem(builderPtr, columnIndex, list, shortItemCallback);
    }

    public void addIntegerList(long columnIndex, RealmList<Integer> list) {
        addListItem(builderPtr, columnIndex, list, integerItemCallback);
    }

    public void addLongList(long columnIndex, RealmList<Long> list) {
        addListItem(builderPtr, columnIndex, list, longItemCallback);
    }

    public void addBooleanList(long columnIndex, RealmList<Boolean> list) {
        addListItem(builderPtr, columnIndex, list, booleanItemCallback);
    }

    public void addFloatList(long columnIndex, RealmList<Float> list) {
        addListItem(builderPtr, columnIndex, list, floatItemCallback);
    }

    public void addDoubleList(long columnIndex, RealmList<Double> list) {
        addListItem(builderPtr, columnIndex, list, doubleItemCallback);
    }

    public void addDateList(long columnIndex, RealmList<Date> list) {
        addListItem(builderPtr, columnIndex, list, dateItemCallback);
    }

    public void addByteArrayList(long columnIndex, RealmList<byte[]> list) {
        addListItem(builderPtr, columnIndex, list, byteArrayItemCallback);
    }

    public void addMutableRealmIntegerList(long columnIndex, RealmList<MutableRealmInteger> list) {
        addListItem(builderPtr, columnIndex, list, mutableRealmIntegerItemCallback);
    }

    private void addEmptyList(long columnIndex) {
        long listPtr = nativeStartList(0);
        nativeStopList(builderPtr, columnIndex, listPtr);
    }

    /**
     * Updates any existing object if it exists, otherwise creates a new one.
     *
     * The builder is automatically closed after calling this method.
     */
    public void updateExistingObject() {
        try {
            nativeCreateOrUpdate(sharedRealmPtr, tablePtr, builderPtr, true, ignoreFieldsWithSameValue);
        } finally {
            close();
        }
    }

    /**
     * Create a new object.
     *
     * The builder is automatically closed after calling this method.
     */
    public UncheckedRow createNewObject() {
        UncheckedRow row;
        try {
            long rowPtr = nativeCreateOrUpdate(sharedRealmPtr, tablePtr, builderPtr, false, false);
            row = new UncheckedRow(context, table, rowPtr);
        } finally {
            close();
        }
        return row;
    }

    /**
     * Returns the underlying native pointer representing this builder.
     */
    public long getNativePtr() {
        return builderPtr;
    }

    /**
     * Manually closes the underlying Builder
     */
    @Override
    public void close() {
        nativeDestroyBuilder(builderPtr);
    }

    private interface ItemCallback<T>  {
        void handleItem(long listPtr, T item);
    }

    private static native long nativeCreateBuilder(long size);
    private static native void nativeDestroyBuilder(long builderPtr);
    private static native long nativeCreateOrUpdate(long sharedRealmPtr,
                                                    long tablePtr,
                                                    long builderPtr,
                                                    boolean updateExistingObject,
                                                    boolean ignoreFieldsWithSameValue);

    // Add simple properties
    private static native void nativeAddNull(long builderPtr, long columnIndex);
    private static native void nativeAddInteger(long builderPtr, long columnIndex, long val);
    private static native void nativeAddString(long builderPtr, long columnIndex, String val);
    private static native void nativeAddFloat(long builderPtr, long columnIndex, float val);
    private static native void nativeAddDouble(long builderPtr, long columnIndex, double val);
    private static native void nativeAddBoolean(long builderPtr, long columnIndex, boolean val);
    private static native void nativeAddByteArray(long builderPtr, long columnIndex, byte[] val);
    private static native void nativeAddDate(long builderPtr, long columnIndex, long val);
    private static native void nativeAddObject(long builderPtr, long columnIndex, long rowPtr);

    // Methods for adding lists
    // Lists sent across JNI one element at a time
    private static native long nativeStartList(long size);
    private static native void nativeStopList(long builderPtr, long columnIndex, long listPtr);
    private static native void nativeAddNullListItem(long listPtr);
    private static native void nativeAddIntegerListItem(long listPtr, long value);
    private static native void nativeAddStringListItem(long listPtr, String val);
    private static native void nativeAddFloatListItem(long listPtr, float val);
    private static native void nativeAddDoubleListItem(long listPtr, double val);
    private static native void nativeAddBooleanListItem(long listPtr, boolean val);
    private static native void nativeAddByteArrayListItem(long listPtr, byte[] val);
    private static native void nativeAddDateListItem(long listPtr, long val);
    private static native void nativeAddObjectListItem(long listPtr, long rowPtr);
    private static native void nativeAddObjectList(long builderPtr, long columnIndex, long[] rowPtrs);
}
