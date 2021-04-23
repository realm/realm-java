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

package io.realm.internal.core;

import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.UUID;

import io.realm.RealmAnyType;
import io.realm.RealmModel;
import io.realm.internal.NativeContext;
import io.realm.internal.NativeObject;
import io.realm.internal.OsSharedRealm;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.Row;
import io.realm.internal.Table;


public class NativeRealmAny implements NativeObject {
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    private final long nativePtr;

    public NativeRealmAny(long nativePtr) {
        this.nativePtr = nativePtr;
        NativeContext.dummyContext.addReference(this);
    }

    public NativeRealmAny(Boolean value) {
        this(nativeCreateRealmAnyBoolean(value));
    }

    public NativeRealmAny(Number value) {
        this(nativeCreateRealmAnyLong(value.longValue()));
    }

    public NativeRealmAny(Float value) {
        this(nativeCreateRealmAnyFloat(value));
    }

    public NativeRealmAny(Double value) {
        this(nativeCreateRealmAnyDouble(value));
    }

    public NativeRealmAny(String value) {
        this(nativeCreateRealmAnyString(value));
    }

    public NativeRealmAny(byte[] value) {
        this(nativeCreateRealmAnyBinary(value));
    }

    public NativeRealmAny(Date value) {
        this(nativeCreateRealmAnyDate(value.getTime()));
    }

    public NativeRealmAny(ObjectId value) {
        this(nativeCreateRealmAnyObjectId(value.toString()));
    }

    public NativeRealmAny(Decimal128 value) {
        this(nativeCreateRealmAnyDecimal128(value.getLow(), value.getHigh()));
    }

    public NativeRealmAny(UUID value) {
        this(nativeCreateRealmAnyUUID(value.toString()));
    }

    public NativeRealmAny(RealmObjectProxy model) {
        this(createRealmAnyLink(model));
    }

    private static long createRealmAnyLink(RealmObjectProxy model) {
        Row row$realm = model.realmGet$proxyState().getRow$realm();

        long targetTablePtr = row$realm.getTable().getNativePtr();
        long targetObjectKey = row$realm.getObjectKey();

        return nativeCreateRealmAnyLink(targetTablePtr, targetObjectKey);
    }

    public NativeRealmAny() {
        this(nativeCreateRealmAnyNull());
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    public RealmAnyType getType() {
        return RealmAnyType.fromNativeValue(nativeGetRealmAnyType(nativePtr));
    }

    public boolean asBoolean() {
        return nativeRealmAnyAsBoolean(nativePtr);
    }

    public long asLong() {
        return nativeRealmAnyAsLong(nativePtr);
    }

    public float asFloat() {
        return nativeRealmAnyAsFloat(nativePtr);
    }

    public double asDouble() {
        return nativeRealmAnyAsDouble(nativePtr);
    }

    public String asString() {
        return nativeRealmAnyAsString(nativePtr);
    }

    public byte[] asBinary() {
        return nativeRealmAnyAsBinary(nativePtr);
    }

    public Date asDate() {
        return new Date(nativeRealmAnyAsDate(nativePtr));
    }

    public ObjectId asObjectId() {
        return new ObjectId(nativeRealmAnyAsObjectId(nativePtr));
    }

    public Decimal128 asDecimal128() {
        long[] data = nativeRealmAnyAsDecimal128(nativePtr);
        return Decimal128.fromIEEE754BIDEncoding(data[1]/*high*/, data[0]/*low*/);
    }

    public UUID asUUID() {
        return UUID.fromString(nativeRealmAnyAsUUID(nativePtr));
    }

    public <T extends RealmModel> Class<T> getModelClass(OsSharedRealm osSharedRealm, RealmProxyMediator mediator) {
        String className = Table.getClassNameForTable(nativeGetRealmModelTableName(nativePtr, osSharedRealm.getNativePtr()));
        return mediator.getClazz(className);
    }

    public String getRealmModelTableName(OsSharedRealm osSharedRealm) {
        return nativeGetRealmModelTableName(nativePtr, osSharedRealm.getNativePtr());
    }

    public long getRealmModelRowKey() {
        return nativeGetRealmModelRowKey(nativePtr);
    }

    public boolean coercedEquals(NativeRealmAny nativeRealmAny){
        return nativeEquals(nativePtr, nativeRealmAny.nativePtr);
    }

    private static native long nativeCreateRealmAnyNull();

    private static native long nativeCreateRealmAnyBoolean(boolean value);

    private static native boolean nativeRealmAnyAsBoolean(long nativePtr);

    private static native long nativeCreateRealmAnyLong(long value);

    private static native long nativeRealmAnyAsLong(long nativePtr);

    private static native long nativeCreateRealmAnyFloat(float value);

    private static native float nativeRealmAnyAsFloat(long nativePtr);

    private static native long nativeCreateRealmAnyDouble(double value);

    private static native double nativeRealmAnyAsDouble(long nativePtr);

    private static native long nativeCreateRealmAnyString(String value);

    private static native String nativeRealmAnyAsString(long nativePtr);

    private static native long nativeCreateRealmAnyBinary(byte[] value);

    private static native byte[] nativeRealmAnyAsBinary(long nativePtr);

    private static native long nativeCreateRealmAnyDate(long value);

    private static native long nativeRealmAnyAsDate(long nativePtr);

    private static native long nativeCreateRealmAnyObjectId(String value);

    private static native String nativeRealmAnyAsObjectId(long nativePtr);

    private static native long nativeCreateRealmAnyDecimal128(long low, long high);

    private static native long[] nativeRealmAnyAsDecimal128(long nativePtr);

    private static native long nativeCreateRealmAnyUUID(String value);

    private static native String nativeRealmAnyAsUUID(long nativePtr);

    private static native long nativeCreateRealmAnyLink(long targetTablePtr, long targetObjectKey);

    private static native int nativeGetRealmAnyType(long nativePtr);

    private static native String nativeGetRealmModelTableName(long nativePtr, long sharedRealmPtr);

    private static native long nativeGetRealmModelRowKey(long nativePtr);

    private static native boolean nativeEquals(long nativePtr, long nativeOtherPtr);

    private static native long nativeGetFinalizerPtr();
}
