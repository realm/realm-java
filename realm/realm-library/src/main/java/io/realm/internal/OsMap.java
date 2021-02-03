/*
 * Copyright 2020 Realm Inc.
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

import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.UUID;

import javax.annotation.Nullable;

import io.realm.internal.android.TypeUtils;
import io.realm.internal.core.NativeMixed;

/**
 * Java wrapper of Object Store Dictionary class. This backs managed versions of RealmMaps.
 */
public class OsMap implements NativeObject {

    private final long nativePtr;
    private final NativeContext context;
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    public OsMap(UncheckedRow row, long columnKey) {
        OsSharedRealm sharedRealm = row.getTable().getSharedRealm();
        this.nativePtr = nativeCreate(sharedRealm.getNativePtr(), row.getNativePtr(), columnKey);
        this.context = sharedRealm.context;
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

    public long size() {
        return nativeSize(nativePtr);
    }

    public void clear() {
        nativeClear(nativePtr);
    }

    // ------------------------------------------
    // TODO: handle other types of keys and avoid
    //  typecasting directly to string in phase 2
    //  for put and get methods.
    // ------------------------------------------

    public void put(Object key, @Nullable Object value) {
        if (value == null) {
            nativePutNull(nativePtr, (String) key);
        } else {
            String valueClassName = value.getClass().getCanonicalName();
            if (Long.class.getCanonicalName().equals(valueClassName)) {
                nativePutLong(nativePtr, (String) key, (Long) value);
            } else if (Integer.class.getCanonicalName().equals(valueClassName)) {
                nativePutLong(nativePtr, (String) key, (Integer) value);
            } else if (Short.class.getCanonicalName().equals(valueClassName)) {
                nativePutLong(nativePtr, (String) key, (Short) value);
            } else if (Byte.class.getCanonicalName().equals(valueClassName)) {
                nativePutLong(nativePtr, (String) key, (Byte) value);
            } else if (Float.class.getCanonicalName().equals(valueClassName)) {
                nativePutFloat(nativePtr, (String) key, (Float) value);
            } else if (Double.class.getCanonicalName().equals(valueClassName)) {
                nativePutDouble(nativePtr, (String) key, (Double) value);
            } else if (String.class.getCanonicalName().equals(valueClassName)) {
                nativePutString(nativePtr, (String) key, (String) value);
            } else if (Boolean.class.getCanonicalName().equals(valueClassName)) {
                nativePutBoolean(nativePtr, (String) key, (Boolean) value);
            } else if (Date.class.getCanonicalName().equals(valueClassName)) {
                nativePutDate(nativePtr, (String) key, ((Date) value).getTime());
            } else if (Decimal128.class.getCanonicalName().equals(valueClassName)) {
                Decimal128 decimal128 = (Decimal128) value;
                nativePutDecimal128(nativePtr, (String) key, decimal128.getHigh(), decimal128.getLow());
            } else if (Byte[].class.getCanonicalName().equals(valueClassName)) {
                nativePutBinary(nativePtr, (String) key, TypeUtils.convertNonPrimitiveBinaryToPrimitive((Byte[]) value));
            } else if (byte[].class.getCanonicalName().equals(valueClassName)) {
                nativePutBinary(nativePtr, (String) key, (byte[]) value);
            } else if (ObjectId.class.getCanonicalName().equals(valueClassName)) {
                nativePutObjectId(nativePtr, (String) key, ((ObjectId) value).toString());
            } else if (UUID.class.getCanonicalName().equals(valueClassName)) {
                nativePutUUID(nativePtr, (String) key, value.toString());
            } else {
                throw new UnsupportedOperationException("Class '" + valueClassName + "' not supported.");
            }
        }
    }

    public void put(Object key, long mixedPtr) {
        nativePutMixed(nativePtr, (String) key, mixedPtr);
    }

    public void putRow(Object key, long objKey) {
        nativePutRow(nativePtr, (String) key, objKey);
    }

    // TODO: add more put methods for different value types ad-hoc

    public void remove(Object key) {
        nativeRemove(nativePtr, (String) key);
    }

    public long getModelRowKey(Object key) {
        return nativeGetRow(nativePtr, (String) key);
    }

    @Nullable
    public Object get(Object key) {
        return nativeGetValue(nativePtr, (String) key);
    }

    public long getMixedPtr(Object key) {
        return nativeGetMixedPtr(nativePtr, (String) key);
    }

    private static native long nativeGetFinalizerPtr();

    private static native long nativeCreate(long nativeSharedRealmPtr, long nativeRowPtr, long columnKey);

    private static native Object nativeGetValue(long nativePtr, String key);

    private static native long nativeGetMixedPtr(long nativePtr, String key);

    private static native long nativeGetRow(long nativePtr, String key);

    private static native void nativePutNull(long nativePtr, String key);

    private static native void nativePutLong(long nativePtr, String key, long value);

    private static native void nativePutFloat(long nativePtr, String key, float value);

    private static native void nativePutDouble(long nativePtr, String key, double value);

    private static native void nativePutString(long nativePtr, String key, String value);

    private static native void nativePutBoolean(long nativePtr, String key, boolean value);

    private static native void nativePutDate(long nativePtr, String key, long value);

    private static native void nativePutDecimal128(long nativePtr, String key, long high, long low);

    private static native void nativePutBinary(long nativePtr, String key, byte[] value);

    private static native void nativePutObjectId(long nativePtr, String key, String value);

    private static native void nativePutUUID(long nativePtr, String key, String value);

    private static native void nativePutMixed(long nativePtr, String key, long nativeObjectPtr);

    private static native void nativePutRow(long nativePtr, String key, long objKey);

    private static native long nativeSize(long nativePtr);

    private static native void nativeClear(long nativePtr);

    private static native void nativeRemove(long nativePtr, String key);
}
