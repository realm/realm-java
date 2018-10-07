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

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.internal.NativeContext;
import io.realm.internal.NativeObject;
import io.realm.internal.OsSharedRealm;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Table;
import io.realm.internal.UncheckedRow;

/**
 * This class is a wrapper around building up object data for calling `Object::create()`
 * <p>
 * Fill the object data by calling the various `addX()` methods, then create a new or update
 * an existing by calling {@link #createNewObject()} or {@link #updateExistingObject()}. These
 * methods can be called multiple times if needed.
 * <p>
 * This class assumes it is only being used from within a write transaction. Using it outside one
 * will result in undefined behaviour.
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
public class OsObjectBuilder implements NativeObject {

    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    private final Table table;
    private final long sharedRealmPtr;
    private final long builderPtr;
    private final long tablePtr;
    private final NativeContext context;

    public OsObjectBuilder(Table table) {
        OsSharedRealm sharedRealm = table.getSharedRealm();
        this.sharedRealmPtr = sharedRealm.getNativePtr();
        this.table = table;
        this.tablePtr = table.getNativePtr();
        this.builderPtr = nativeCreateBuilder();
        this.context = sharedRealm.context;
        NativeContext.dummyContext.addReference(this);
    }

    @Override
    public long getNativePtr() {
        return builderPtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    /**
     * Add non-nullable property that maps to Core's INTEGER.
     */
    public void addInteger(String key, long val) {
        nativeAddInteger(builderPtr, key, val);
    }

    /**
     * Add nullable property that maps to Core's INTEGER.
     */
    public void addInteger(String key, Long val) {
        if (val == null) {
            nativeAddNull(builderPtr, key);
        } else {
            nativeAddInteger(builderPtr, key, val);
        }
    }

    /**
     * Add Nullable and Required String property.
     */
    public void addString(String key, String val) {
        if (val == null) {
            nativeAddNull(builderPtr, key);
        } else {
            nativeAddString(builderPtr, key, val);
        }
    }

    public void addFloat(String key, Float val) {
        if (val == null) {
            nativeAddNull(builderPtr, key);
        } else {
            nativeAddFloat(builderPtr, key, val);
        }
    }

    public void addDouble(String key, Double val) {
        if (val == null) {
            nativeAddNull(builderPtr, key);
        } else {
            nativeAddDouble(builderPtr, key, val);
        }
    }

    public void addBoolean(String key, Boolean val) {
        if (val == null) {
            nativeAddNull(builderPtr, key);
        } else {
            nativeAddBoolean(builderPtr, key, val);
        }
    }

    public void addDate(String key, Date val) {
        if (val == null) {
            nativeAddNull(builderPtr, key);
        } else {
            nativeAddDate(builderPtr, key, val.getTime());
        }
    }

    public void addByteArray(String key, byte[] val) {
        if (val == null) {
            nativeAddNull(builderPtr, key);
        } else {
            nativeAddByteArray(builderPtr, key, val);
        }
    }

    public void addObject(String key, RealmObjectProxy val) {
        if (val == null) {
            nativeAddNull(builderPtr, key);
        } else {
            nativeAddObject(builderPtr, key, ((UncheckedRow) val.realmGet$proxyState().getRow$realm()).getNativePtr());
        }
    }

    public void addObjectList(String key, RealmList<RealmModel> list) {

    }

    public void addByteList(String key, RealmList<Byte> list) {
    }

    public void addShortList(String key, RealmList<Short> list) {
    }

    public void addLongList(String key, RealmList<Long> list) {
        if (list != null) {
            long listPtr = nativeStartList(list.size());
            for (int i = 0; i < list.size(); i++) {
                Long item = list.get(i);
                if (item == null) {
                    nativeAddNullListItem(listPtr);
                } else {
                    nativeAddIntegerListItem(listPtr, item);
                }
            }
            nativeStopList(builderPtr, key, listPtr);
        } else {
            addEmptyList(key);
        }
    }

    public void addIntegerList(String key, RealmList<Integer> list) {
    }

    public void addBooleanList(String key, RealmList<Boolean> list) {
    }

    public void addFloatList(String key, RealmList<Float> list) {
    }

    public void addDoubleList(String key, RealmList<Double> list) {
    }

    public void addDateList(String key, RealmList<Date> list) {
    }

    public void addByteArrayList(String key, RealmList<byte[]> list) {
    }

    private void addEmptyList(String key) {
        long listPtr = nativeStartList(0);
        nativeStopList(builderPtr, key, listPtr);
    }

    public UncheckedRow updateExistingObject() {
        long rowPtr = nativeCreateOrUpdate(sharedRealmPtr, tablePtr, builderPtr, true);
        return new UncheckedRow(context, table, rowPtr);
    }

    public UncheckedRow createNewObject() {
        long rowPtr = nativeCreateOrUpdate(sharedRealmPtr, tablePtr, builderPtr, true);
        return new UncheckedRow(context, table, rowPtr);
    }

    private static native long nativeCreateBuilder();
    private static native long nativeGetFinalizerPtr();
    private static native long nativeCreateOrUpdate(long sharedRealmPtr, long tablePtr, long builderPtr, boolean updateExistingObject);

    // Add simple properties
    private static native void nativeAddNull(long builderPtr, String key);
    private static native void nativeAddInteger(long builderPtr, String key, long val);
    private static native void nativeAddString(long builderPtr, String key, String val);
    private static native void nativeAddFloat(long builderPtr, String key, float val);
    private static native void nativeAddDouble(long builderPtr, String key, double val);
    private static native void nativeAddBoolean(long builderPtr, String key, boolean val);
    private static native void nativeAddByteArray(long builderPtr, String key, byte[] val);
    private static native void nativeAddDate(long builderPtr, String key, long val);
    private static native void nativeAddObject(long builderPtr, String key, long rowPtr);

    // Methods for adding lists
    // Lists sent across JNI one element at a time
    // TODO: Consider bulk sending them where possible (needs to be measured)
    private static native long nativeStartList(long size);
    private static native void nativeStopList(long builderPtr, String key, long listPtr);
    private static native void nativeAddNullListItem(long listPtr);
    private static native void nativeAddIntegerListItem(long listPtr, long value);
}
