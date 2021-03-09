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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import io.realm.ImportFlag;
import io.realm.Mixed;
import io.realm.MixedNativeFunctionsImpl;
import io.realm.MutableRealmInteger;
import io.realm.RealmDictionary;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.internal.MixedNativeFunctions;
import io.realm.internal.NativeContext;
import io.realm.internal.OsSharedRealm;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Table;
import io.realm.internal.UncheckedRow;
import io.realm.internal.android.TypeUtils;


/**
 * This class is a wrapper around building up object data for calling `Object::create()`
 * <p>
 * Fill the object data by calling the various `addX()` methods, then create a new Object or update
 * an existing one by calling {@link #createNewObject()}, {@link #updateExistingTopLevelObject()} or
 * {@link #updateExistingEmbeddedObject(RealmObjectProxy)}.
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

    // ------------------------------------------
    // List handlers
    // ------------------------------------------

    private static ItemCallback<? extends RealmModel> objectItemCallback = new ItemCallback<RealmModel>() {
        @Override
        public void handleItem(long containerPtr, RealmModel item) {
            RealmObjectProxy proxyItem = (RealmObjectProxy) item;
            nativeAddIntegerListItem(containerPtr, ((UncheckedRow) proxyItem.realmGet$proxyState().getRow$realm()).getNativePtr());
        }
    };

    private static ItemCallback<String> stringItemCallback = new ItemCallback<String>() {
        @Override
        public void handleItem(long containerPtr, String item) {
            nativeAddStringListItem(containerPtr, item);
        }
    };

    private static ItemCallback<Byte> byteItemCallback = new ItemCallback<Byte>() {
        @Override
        public void handleItem(long containerPtr, Byte item) {
            nativeAddIntegerListItem(containerPtr, item.longValue());
        }
    };

    private static ItemCallback<Short> shortItemCallback = new ItemCallback<Short>() {
        @Override
        public void handleItem(long containerPtr, Short item) {
            nativeAddIntegerListItem(containerPtr, item);
        }
    };

    private static ItemCallback<Integer> integerItemCallback = new ItemCallback<Integer>() {
        @Override
        public void handleItem(long containerPtr, Integer item) {
            nativeAddIntegerListItem(containerPtr, item);
        }
    };

    private static ItemCallback<Long> longItemCallback = new ItemCallback<Long>() {
        @Override
        public void handleItem(long containerPtr, Long item) {
            nativeAddIntegerListItem(containerPtr, item);
        }
    };

    private static ItemCallback<Boolean> booleanItemCallback = new ItemCallback<Boolean>() {
        @Override
        public void handleItem(long containerPtr, Boolean item) {
            nativeAddBooleanListItem(containerPtr, item);
        }
    };

    private static ItemCallback<Float> floatItemCallback = new ItemCallback<Float>() {
        @Override
        public void handleItem(long containerPtr, Float item) {
            nativeAddFloatListItem(containerPtr, item);
        }
    };

    private static ItemCallback<Double> doubleItemCallback = new ItemCallback<Double>() {
        @Override
        public void handleItem(long containerPtr, Double item) {
            nativeAddDoubleListItem(containerPtr, item);
        }
    };

    private static ItemCallback<Date> dateItemCallback = new ItemCallback<Date>() {
        @Override
        public void handleItem(long containerPtr, Date item) {
            nativeAddDateListItem(containerPtr, item.getTime());
        }
    };

    private static ItemCallback<byte[]> byteArrayItemCallback = new ItemCallback<byte[]>() {
        @Override
        public void handleItem(long containerPtr, byte[] item) {
            nativeAddByteArrayListItem(containerPtr, item);
        }
    };

    private static ItemCallback<MutableRealmInteger> mutableRealmIntegerItemCallback = new ItemCallback<MutableRealmInteger>() {
        @Override
        public void handleItem(long containerPtr, MutableRealmInteger item) {
            Long value = item.get();
            if (value == null) {
                nativeAddNullListItem(containerPtr);
            } else {
                nativeAddIntegerListItem(containerPtr, value);
            }
        }
    };

    private static ItemCallback<Decimal128> decimal128ItemCallback = new ItemCallback<Decimal128>() {
        @Override
        public void handleItem(long containerPtr, Decimal128 item) {
            nativeAddDecimal128ListItem(containerPtr, item.getLow(), item.getHigh());
        }
    };

    private static ItemCallback<ObjectId> objectIdItemCallback = new ItemCallback<ObjectId>() {
        @Override
        public void handleItem(long containerPtr, ObjectId item) {
            nativeAddObjectIdListItem(containerPtr, item.toString());
        }
    };

    private static ItemCallback<UUID> uuidItemCallback = new ItemCallback<UUID>() {
        @Override
        public void handleItem(long containerPtr, UUID item) {
            nativeAddUUIDListItem(containerPtr, item.toString());
        }
    };

    // ------------------------------------------
    // Map/Dictionary handlers
    // ------------------------------------------

    private static ItemCallback<Map.Entry<String, Boolean>> booleanMapItemCallback = new ItemCallback<Map.Entry<String, Boolean>>() {
        @Override
        public void handleItem(long containerPtr, Map.Entry<String, Boolean> item) {
            nativeAddBooleanDictionaryEntry(containerPtr, item.getKey(), item.getValue());
        }
    };

    private static ItemCallback<Map.Entry<String, String>> stringMapItemCallback = new ItemCallback<Map.Entry<String, String>>() {
        @Override
        public void handleItem(long containerPtr, Map.Entry<String, String> item) {
            nativeAddStringDictionaryEntry(containerPtr, item.getKey(), item.getValue());
        }
    };

    private static ItemCallback<Map.Entry<String, Integer>> integerMapItemCallback = new ItemCallback<Map.Entry<String, Integer>>() {
        @Override
        public void handleItem(long containerPtr, Map.Entry<String, Integer> item) {
            nativeAddIntegerDictionaryEntry(containerPtr, item.getKey(), item.getValue());
        }
    };

    private static ItemCallback<Map.Entry<String, Float>> floatMapItemCallback = new ItemCallback<Map.Entry<String, Float>>() {
        @Override
        public void handleItem(long containerPtr, Map.Entry<String, Float> item) {
            nativeAddFloatDictionaryEntry(containerPtr, item.getKey(), item.getValue());
        }
    };

    private static ItemCallback<Map.Entry<String, Long>> longMapItemCallback = new ItemCallback<Map.Entry<String, Long>>() {
        @Override
        public void handleItem(long containerPtr, Map.Entry<String, Long> item) {
            nativeAddIntegerDictionaryEntry(containerPtr, item.getKey(), item.getValue());
        }
    };

    private static ItemCallback<Map.Entry<String, Short>> shortMapItemCallback = new ItemCallback<Map.Entry<String, Short>>() {
        @Override
        public void handleItem(long containerPtr, Map.Entry<String, Short> item) {
            nativeAddIntegerDictionaryEntry(containerPtr, item.getKey(), item.getValue());
        }
    };

    private static ItemCallback<Map.Entry<String, Byte>> byteMapItemCallback = new ItemCallback<Map.Entry<String, Byte>>() {
        @Override
        public void handleItem(long containerPtr, Map.Entry<String, Byte> item) {
            nativeAddIntegerDictionaryEntry(containerPtr, item.getKey(), item.getValue());
        }
    };

    private static ItemCallback<Map.Entry<String, Double>> doubleMapItemCallback = new ItemCallback<Map.Entry<String, Double>>() {
        @Override
        public void handleItem(long containerPtr, Map.Entry<String, Double> item) {
            nativeAddDoubleDictionaryEntry(containerPtr, item.getKey(), item.getValue());
        }
    };

    private static ItemCallback<Map.Entry<String, byte[]>> binaryMapItemCallback = new ItemCallback<Map.Entry<String, byte[]>>() {
        @Override
        public void handleItem(long containerPtr, Map.Entry<String, byte[]> item) {
            nativeAddBinaryDictionaryEntry(containerPtr, item.getKey(), item.getValue());
        }
    };

    private static ItemCallback<Map.Entry<String, Date>> dateMapItemCallback = new ItemCallback<Map.Entry<String, Date>>() {
        @Override
        public void handleItem(long containerPtr, Map.Entry<String, Date> item) {
            nativeAddDateDictionaryEntry(containerPtr, item.getKey(), item.getValue().getTime());
        }
    };

    private static ItemCallback<Map.Entry<String, Decimal128>> decimal128MapItemCallback = new ItemCallback<Map.Entry<String, Decimal128>>() {
        @Override
        public void handleItem(long containerPtr, Map.Entry<String, Decimal128> item) {
            nativeAddDecimal128DictionaryEntry(containerPtr, item.getKey(), item.getValue().getHigh(), item.getValue().getLow());
        }
    };

    private static ItemCallback<Map.Entry<String, ObjectId>> objectIdMapItemCallback = new ItemCallback<Map.Entry<String, ObjectId>>() {
        @Override
        public void handleItem(long containerPtr, Map.Entry<String, ObjectId> item) {
            nativeAddObjectIdDictionaryEntry(containerPtr, item.getKey(), item.getValue().toString());
        }
    };

    private static ItemCallback<Map.Entry<String, UUID>> uuidMapItemCallback = new ItemCallback<Map.Entry<String, UUID>>() {
        @Override
        public void handleItem(long containerPtr, Map.Entry<String, UUID> item) {
            nativeAddUUIDDictionaryEntry(containerPtr, item.getKey(), item.getValue().toString());
        }
    };

    private static ItemCallback<Mixed> mixedItemCallback = new ItemCallback<Mixed>() {
        private final MixedNativeFunctions mixedNativeFunctions = new MixedNativeFunctionsImpl();

        @Override
        public void handleItem(long listPtr, Mixed mixed) {
            mixedNativeFunctions.handleItem(listPtr, mixed);
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
            boolean isNullable = (columnKey == 0) || table.isColumnNullable(columnKey);
            for (int i = 0; i < list.size(); i++) {
                T item = list.get(i);
                if (item == null) {
                    if (!isNullable) {
                        throw new IllegalArgumentException("This 'RealmList' is not nullable. A non-null value is expected.");
                    }
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

    public void addMixedValueDictionary(long columnKey) {
        addEmptyDictionary(columnKey);
    }

    public void addMixedValueDictionary(long columnKey, List<String> keys, List<Long> mixedPointers) {
        addMixedDictionaryItem(builderPtr, columnKey, keys, mixedPointers);
    }

    private void addMixedDictionaryItem(
            long builderPtr,
            long columnKey,
            List<String> keys,
            List<Long> mixedPointers
    ) {
        if (keys.isEmpty() && mixedPointers.isEmpty()) {
            addEmptyDictionary(columnKey);
        } else {
            long dictionaryPtr = nativeStartDictionary();
            for (int i = 0; i < keys.size(); i++) {
                nativeAddMixedDictionaryEntry(dictionaryPtr, keys.get(i), mixedPointers.get(i));
            }
            nativeStopDictionary(builderPtr, columnKey, dictionaryPtr);

        }
    }

    public void addBooleanValueDictionary(long columnKey, RealmDictionary<Boolean> dictionary) {
        addDictionaryItem(builderPtr, columnKey, dictionary, booleanMapItemCallback);
    }

    public void addIntegerValueDictionary(long columnKey, RealmDictionary<Integer> dictionary) {
        addDictionaryItem(builderPtr, columnKey, dictionary, integerMapItemCallback);
    }

    public void addFloatValueDictionary(long columnKey, RealmDictionary<Float> dictionary) {
        addDictionaryItem(builderPtr, columnKey, dictionary, floatMapItemCallback);
    }

    public void addLongValueDictionary(long columnKey, RealmDictionary<Long> dictionary) {
        addDictionaryItem(builderPtr, columnKey, dictionary, longMapItemCallback);
    }

    public void addShortValueDictionary(long columnKey, RealmDictionary<Short> dictionary) {
        addDictionaryItem(builderPtr, columnKey, dictionary, shortMapItemCallback);
    }

    public void addByteValueDictionary(long columnKey, RealmDictionary<Byte> dictionary) {
        addDictionaryItem(builderPtr, columnKey, dictionary, byteMapItemCallback);
    }

    public void addDoubleValueDictionary(long columnKey, RealmDictionary<Double> dictionary) {
        addDictionaryItem(builderPtr, columnKey, dictionary, doubleMapItemCallback);
    }

    public void addStringValueDictionary(long columnKey, RealmDictionary<String> dictionary) {
        addDictionaryItem(builderPtr, columnKey, dictionary, stringMapItemCallback);
    }

    public void addDateValueDictionary(long columnKey, RealmDictionary<Date> dictionary) {
        addDictionaryItem(builderPtr, columnKey, dictionary, dateMapItemCallback);
    }

    public void addDecimal128ValueDictionary(long columnKey, RealmDictionary<Decimal128> dictionary) {
        addDictionaryItem(builderPtr, columnKey, dictionary, decimal128MapItemCallback);
    }

    public void addBinaryValueDictionary(long columnKey, RealmDictionary<byte[]> dictionary) {
        addDictionaryItem(builderPtr, columnKey, dictionary, binaryMapItemCallback);
    }

    public void addObjectIdValueDictionary(long columnKey, RealmDictionary<ObjectId> dictionary) {
        addDictionaryItem(builderPtr, columnKey, dictionary, objectIdMapItemCallback);
    }

    public void addUUIDValueDictionary(long columnKey, RealmDictionary<UUID> dictionary) {
        addDictionaryItem(builderPtr, columnKey, dictionary, uuidMapItemCallback);
    }

    private <T> void addDictionaryItem(
            long builderPtr,
            long columnKey,
            @Nullable RealmDictionary<T> dictionary,
            ItemCallback<Map.Entry<String, T>> mapItemCallback
    ) {
        if (dictionary != null) {
            long dictionaryPtr = nativeStartDictionary();
            for (Map.Entry<String, T> entry : dictionary.entrySet()) {
                if (entry.getValue() == null) {
                    nativeAddNullDictionaryEntry(dictionaryPtr, entry.getKey());
                } else {
                    mapItemCallback.handleItem(dictionaryPtr, entry);
                }
            }
            nativeStopDictionary(builderPtr, columnKey, dictionaryPtr);
        } else {
            addEmptyDictionary(columnKey);
        }
    }

    private void addEmptyDictionary(long columnKey) {
        nativeStopDictionary(builderPtr, columnKey, nativeStartDictionary());
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
        void handleItem(long containerPtr, T item);
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

    // dictionaries
    private static native long nativeStartDictionary();

    private static native void nativeStopDictionary(long builderPtr, long columnKey, long dictionaryPtr);

    private static native void nativeAddNullDictionaryEntry(long dictionaryPtr, String key);

    private static native void nativeAddBooleanDictionaryEntry(long dictionaryPtr, String key, boolean value);

    private static native void nativeAddStringDictionaryEntry(long dictionaryPtr, String key, String value);

    private static native void nativeAddIntegerDictionaryEntry(long dictionaryPtr, String key, long value);

    private static native void nativeAddDoubleDictionaryEntry(long dictionaryPtr, String key, double value);

    private static native void nativeAddFloatDictionaryEntry(long dictionaryPtr, String key, float value);

    private static native void nativeAddBinaryDictionaryEntry(long dictionaryPtr, String key, byte[] value);

    private static native void nativeAddDateDictionaryEntry(long dictionaryPtr, String key, long value);

    private static native void nativeAddDecimal128DictionaryEntry(long dictionaryPtr, String key, long high, long low);

    private static native void nativeAddObjectIdDictionaryEntry(long dictionaryPtr, String key, String value);

    private static native void nativeAddUUIDDictionaryEntry(long dictionaryPtr, String key, String value);

    private static native void nativeAddMixedDictionaryEntry(long dictionaryPtr, String key, long mixedPtr);
}
