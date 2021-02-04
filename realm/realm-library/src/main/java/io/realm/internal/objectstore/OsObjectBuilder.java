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

import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.io.Closeable;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import io.realm.ImportFlag;
import io.realm.Mixed;
import io.realm.MixedHandlerImpl;
import io.realm.MutableRealmInteger;
import io.realm.RealmDictionary;
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
 * an existing one by calling {@link #createNewObject()}, {@link #updateExistingTopLevelObject()} or.
 * {@link #updateExistingEmbeddedObject(RealmObjectProxy)}
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

    private static ItemCallback<Decimal128> decimal128ItemCallback = new ItemCallback<Decimal128>() {
        @Override
        public void handleItem(long listPtr, Decimal128 item) {
            nativeAddDecimal128ListItem(listPtr, item.getLow(), item.getHigh());
        }
    };

    private static ItemCallback<ObjectId> objectIdItemCallback = new ItemCallback<ObjectId>() {
        @Override
        public void handleItem(long listPtr, ObjectId item) {
            nativeAddObjectIdListItem(listPtr, item.toString());
        }
    };

    private static ItemCallback<UUID> uuidItemCallback = new ItemCallback<UUID>() {
        @Override
        public void handleItem(long listPtr, UUID item) {
            nativeAddUUIDListItem(listPtr, item.toString());
        }
    };

    private static ItemCallback<Mixed> mixedItemCallback = new ItemCallback<Mixed>() {
        private final MixedHandler mixedHandler = new MixedHandlerImpl();

        @Override
        public void handleItem(long listPtr, Mixed mixed) {
            mixedHandler.handleItem(listPtr, mixed);
        }
    };

    // If true, fields will not be updated if the same value would be written to it.
    private final boolean ignoreFieldsWithSameValue;

    public OsObjectBuilder(Table table, Set<ImportFlag> flags) {
        OsSharedRealm sharedRealm = table.getSharedRealm();
        this.sharedRealmPtr = sharedRealm.getNativePtr();
        this.table = table;
        this.table.getColumnNames();
        this.tablePtr = table.getNativePtr();
        this.builderPtr = nativeCreateBuilder();
        this.context = sharedRealm.context;
        this.ignoreFieldsWithSameValue = flags.contains(ImportFlag.CHECK_SAME_VALUES_BEFORE_SET);
    }

    public void addInteger(long columnKey, @Nullable Byte val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnKey);
        } else {
            nativeAddInteger(builderPtr, columnKey, val);
        }
    }

    public void addInteger(long columnKey, @Nullable Short val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnKey);
        } else {
            nativeAddInteger(builderPtr, columnKey, val);
        }
    }

    public void addInteger(long columnKey, @Nullable Integer val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnKey);
        } else {
            nativeAddInteger(builderPtr, columnKey, val);
        }
    }

    public void addInteger(long columnKey, @Nullable Long val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnKey);
        } else {
            nativeAddInteger(builderPtr, columnKey, val);
        }
    }

    public void addMutableRealmInteger(long columnKey, @Nullable MutableRealmInteger val) {
        if (val == null || val.get() == null) {
            nativeAddNull(builderPtr, columnKey);
        } else {
            nativeAddInteger(builderPtr, columnKey, val.get());
        }
    }

    public void addMixed(long columnKey, long mixedPtr) {
        nativeAddMixed(builderPtr, columnKey, mixedPtr);
    }

    public void addString(long columnKey, @Nullable String val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnKey);
        } else {
            nativeAddString(builderPtr, columnKey, val);
        }
    }

    public void addFloat(long columnKey, @Nullable Float val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnKey);
        } else {
            nativeAddFloat(builderPtr, columnKey, val);
        }
    }

    public void addDouble(long columnKey, @Nullable Double val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnKey);
        } else {
            nativeAddDouble(builderPtr, columnKey, val);
        }
    }

    public void addBoolean(long columnKey, @Nullable Boolean val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnKey);
        } else {
            nativeAddBoolean(builderPtr, columnKey, val);
        }
    }

    public void addDate(long columnKey, @Nullable Date val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnKey);
        } else {
            nativeAddDate(builderPtr, columnKey, val.getTime());
        }
    }

    public void addByteArray(long columnKey, @Nullable byte[] val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnKey);
        } else {
            nativeAddByteArray(builderPtr, columnKey, val);
        }
    }

    public void addDecimal128(long columnKey, @Nullable Decimal128 val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnKey);
        } else {
            nativeAddDecimal128(builderPtr, columnKey, val.getLow(), val.getHigh());
        }
    }

    public void addObjectId(long columnKey, @Nullable ObjectId val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnKey);
        } else {
            nativeAddObjectId(builderPtr, columnKey, val.toString());
        }
    }

    public void addUUID(long columnKey, @Nullable UUID val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnKey);
        } else {
            nativeAddUUID(builderPtr, columnKey, val.toString());
        }
    }

    public void addNull(long columnKey) {
        nativeAddNull(builderPtr, columnKey);
    }

    public void addObject(long columnKey, @Nullable RealmModel val) {
        if (val == null) {
            nativeAddNull(builderPtr, columnKey);
        } else {
            RealmObjectProxy proxy = (RealmObjectProxy) val;
            UncheckedRow row = (UncheckedRow) proxy.realmGet$proxyState().getRow$realm();
            nativeAddObject(builderPtr, columnKey, row.getNativePtr());
        }
    }

    private <T> void addListItem(long builderPtr, long columnKey, @Nullable List<T> list, ItemCallback<T> itemCallback) {
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
            nativeStopList(builderPtr, columnKey, listPtr);
        } else {
            addEmptyList(columnKey);
        }
    }

    public <T extends RealmModel> void addObjectList(long columnKey, @Nullable RealmList<T> list) {
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
            nativeAddObjectList(builderPtr, columnKey, rowPointers);
        } else {
            nativeAddObjectList(builderPtr, columnKey, new long[0]);
        }
    }

    public void addStringList(long columnKey, RealmList<String> list) {
        addListItem(builderPtr, columnKey, list, stringItemCallback);
    }

    public void addByteList(long columnKey, RealmList<Byte> list) {
        addListItem(builderPtr, columnKey, list, byteItemCallback);
    }

    public void addShortList(long columnKey, RealmList<Short> list) {
        addListItem(builderPtr, columnKey, list, shortItemCallback);
    }

    public void addIntegerList(long columnKey, RealmList<Integer> list) {
        addListItem(builderPtr, columnKey, list, integerItemCallback);
    }

    public void addLongList(long columnKey, RealmList<Long> list) {
        addListItem(builderPtr, columnKey, list, longItemCallback);
    }

    public void addBooleanList(long columnKey, RealmList<Boolean> list) {
        addListItem(builderPtr, columnKey, list, booleanItemCallback);
    }

    public void addFloatList(long columnKey, RealmList<Float> list) {
        addListItem(builderPtr, columnKey, list, floatItemCallback);
    }

    public void addDoubleList(long columnKey, RealmList<Double> list) {
        addListItem(builderPtr, columnKey, list, doubleItemCallback);
    }

    public void addDateList(long columnKey, RealmList<Date> list) {
        addListItem(builderPtr, columnKey, list, dateItemCallback);
    }

    public void addByteArrayList(long columnKey, RealmList<byte[]> list) {
        addListItem(builderPtr, columnKey, list, byteArrayItemCallback);
    }

    public void addMutableRealmIntegerList(long columnKey, RealmList<MutableRealmInteger> list) {
        addListItem(builderPtr, columnKey, list, mutableRealmIntegerItemCallback);
    }

    public void addDecimal128List(long columnKey, RealmList<Decimal128> list) {
        addListItem(builderPtr, columnKey, list, decimal128ItemCallback);
    }

    public void addObjectIdList(long columnKey, RealmList<ObjectId> list) {
        addListItem(builderPtr, columnKey, list, objectIdItemCallback);
    }

    public void addUUIDList(long columnKey, RealmList<UUID> list) {
        addListItem(builderPtr, columnKey, list, uuidItemCallback);
    }

    public void addMixedList(long columnKey, RealmList<Mixed> list) {
        addListItem(builderPtr, columnKey, list, mixedItemCallback);
    }

    private void addEmptyList(long columnKey) {
        long listPtr = nativeStartList(0);
        nativeStopList(builderPtr, columnKey, listPtr);
    }

    public void addMixedValueDictionary(long columnKey, RealmDictionary<Mixed> dictionary) {
        // FIXME
    }

    public void addBooleanValueDictionary(long columnKey, RealmDictionary<Boolean> dictionary) {
        // FIXME
    }

    /**
     * Updates any existing object if it exists, otherwise creates a new one.
     * <p>
     * Updating an existing object requires that the primary key is defined as one of the fields.
     * <p>
     * The builder is automatically closed after calling this method.
     */
    public void updateExistingTopLevelObject() {
        try {
            nativeCreateOrUpdateTopLevelObject(sharedRealmPtr, tablePtr, builderPtr, true, ignoreFieldsWithSameValue);
        } finally {
            close();
        }
    }

    /**
     * Updates an existing embedded object.
     * <p>
     * The builder is automatically closed after calling this method.
     */
    public void updateExistingEmbeddedObject(RealmObjectProxy embeddedObject) {
        try {
            long objKey = embeddedObject.realmGet$proxyState().getRow$realm().getObjectKey();
            nativeUpdateEmbeddedObject(sharedRealmPtr, tablePtr, builderPtr, objKey, ignoreFieldsWithSameValue);
        } finally {
            close();
        }
    }

    /**
     * Create a new object.
     * <p>
     * The builder is automatically closed after calling this method.
     */
    public UncheckedRow createNewObject() {
        UncheckedRow row;
        try {
            long rowPtr = nativeCreateOrUpdateTopLevelObject(sharedRealmPtr, tablePtr, builderPtr, false, false);
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

    private interface ItemCallback<T> {
        void handleItem(long listPtr, T item);
    }

    private static native long nativeCreateBuilder();

    private static native void nativeDestroyBuilder(long builderPtr);

    private static native long nativeCreateOrUpdateTopLevelObject(long sharedRealmPtr,
            long tablePtr,
            long builderPtr,
            boolean updateExistingObject,
            boolean ignoreFieldsWithSameValue);

    private static native long nativeUpdateEmbeddedObject(long sharedRealmPtr,
            long tablePtr,
            long builderPtr,
            long objKey,
            boolean ignoreFieldsWithSameValue);

    // Add simple properties
    private static native void nativeAddNull(long builderPtr, long columnKey);

    private static native void nativeAddInteger(long builderPtr, long columnKey, long val);

    private static native void nativeAddString(long builderPtr, long columnKey, String val);

    private static native void nativeAddFloat(long builderPtr, long columnKey, float val);

    private static native void nativeAddDouble(long builderPtr, long columnKey, double val);

    private static native void nativeAddBoolean(long builderPtr, long columnKey, boolean val);

    private static native void nativeAddByteArray(long builderPtr, long columnKey, byte[] val);

    private static native void nativeAddDate(long builderPtr, long columnKey, long val);

    private static native void nativeAddObject(long builderPtr, long columnKey, long rowPtr);

    private static native void nativeAddDecimal128(long builderPtr, long columnKey, long low, long high);

    private static native void nativeAddObjectId(long builderPtr, long columnKey, String data);

    private static native void nativeAddUUID(long builderPtr, long columnKey, String data);

    private static native void nativeAddMixed(long builderPtr, long columnKey, long mixedPtr);

    // Methods for adding lists
    // Lists sent across JNI one element at a time
    private static native long nativeStartList(long size);

    private static native void nativeStopList(long builderPtr, long columnKey, long listPtr);

    private static native void nativeAddNullListItem(long listPtr);

    private static native void nativeAddIntegerListItem(long listPtr, long value);

    private static native void nativeAddStringListItem(long listPtr, String val);

    private static native void nativeAddFloatListItem(long listPtr, float val);

    private static native void nativeAddDoubleListItem(long listPtr, double val);

    private static native void nativeAddBooleanListItem(long listPtr, boolean val);

    private static native void nativeAddByteArrayListItem(long listPtr, byte[] val);

    private static native void nativeAddDateListItem(long listPtr, long val);

    private static native void nativeAddDecimal128ListItem(long listPtr, long low, long high);

    private static native void nativeAddObjectIdListItem(long listPtr, String data);

    private static native void nativeAddUUIDListItem(long listPtr, String data);

    public static native void nativeAddMixedListItem(long listPtr, long mixedPtr);

    private static native void nativeAddObjectListItem(long listPtr, long rowPtr);

    private static native void nativeAddObjectList(long builderPtr, long columnKey, long[] rowPtrs);
}
