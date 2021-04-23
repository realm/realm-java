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
import io.realm.internal.Keep;
import io.realm.internal.NativeContext;
import io.realm.internal.NativeObject;
import io.realm.internal.OsSharedRealm;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.Row;
import io.realm.internal.Table;

@Keep
public class NativeRealmAny implements NativeObject {
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    private final long nativePtr;

    public NativeRealmAny(long nativePtr) {
        this.nativePtr = nativePtr;
        NativeContext.dummyContext.addReference(this);
    }

    public NativeRealmAny(Boolean value) {
        this(nativeCreateBoolean(value));
    }

    public NativeRealmAny(Number value) {
        this(nativeCreateLong(value.longValue()));
    }

    public NativeRealmAny(Float value) {
        this(nativeCreateFloat(value));
    }

    public NativeRealmAny(Double value) {
        this(nativeCreateDouble(value));
    }

    public NativeRealmAny(String value) {
        this(nativeCreateString(value));
    }

    public NativeRealmAny(byte[] value) {
        this(nativeCreateBinary(value));
    }

    public NativeRealmAny(Date value) {
        this(nativeCreateDate(value.getTime()));
    }

    public NativeRealmAny(ObjectId value) {
        this(nativeCreateObjectId(value.toString()));
    }

    public NativeRealmAny(Decimal128 value) {
        this(nativeCreateDecimal128(value.getLow(), value.getHigh()));
    }

    public NativeRealmAny(UUID value) {
        this(nativeCreateUUID(value.toString()));
    }

    public NativeRealmAny(RealmObjectProxy model) {
        this(createRealmAnyLink(model));
    }

    private static long createRealmAnyLink(RealmObjectProxy model) {
        Row row$realm = model.realmGet$proxyState().getRow$realm();

        long targetTablePtr = row$realm.getTable().getNativePtr();
        long targetObjectKey = row$realm.getObjectKey();

        return nativeCreateLink(targetTablePtr, targetObjectKey);
    }

    public NativeRealmAny() {
        this(nativeCreateNull());
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
        return RealmAnyType.fromNativeValue(nativeGetType(nativePtr));
    }

    public boolean asBoolean() {
        return nativeAsBoolean(nativePtr);
    }

    public long asLong() {
        return nativeAsLong(nativePtr);
    }

    public float asFloat() {
        return nativeAsFloat(nativePtr);
    }

    public double asDouble() {
        return nativeAsDouble(nativePtr);
    }

    public String asString() {
        return nativeAsString(nativePtr);
    }

    public byte[] asBinary() {
        return nativeAsBinary(nativePtr);
    }

    public Date asDate() {
        return new Date(nativeAsDate(nativePtr));
    }

    public ObjectId asObjectId() {
        return new ObjectId(nativeAsObjectId(nativePtr));
    }

    public Decimal128 asDecimal128() {
        long[] data = nativeAsDecimal128(nativePtr);
        return Decimal128.fromIEEE754BIDEncoding(data[1]/*high*/, data[0]/*low*/);
    }

    public UUID asUUID() {
        return UUID.fromString(nativeAsUUID(nativePtr));
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

    private static native long nativeCreateNull();

    private static native long nativeCreateBoolean(boolean value);

    private static native boolean nativeAsBoolean(long nativePtr);

    private static native long nativeCreateLong(long value);

    private static native long nativeAsLong(long nativePtr);

    private static native long nativeCreateFloat(float value);

    private static native float nativeAsFloat(long nativePtr);

    private static native long nativeCreateDouble(double value);

    private static native double nativeAsDouble(long nativePtr);

    private static native long nativeCreateString(String value);

    private static native String nativeAsString(long nativePtr);

    private static native long nativeCreateBinary(byte[] value);

    private static native byte[] nativeAsBinary(long nativePtr);

    private static native long nativeCreateDate(long value);

    private static native long nativeAsDate(long nativePtr);

    private static native long nativeCreateObjectId(String value);

    private static native String nativeAsObjectId(long nativePtr);

    private static native long nativeCreateDecimal128(long low, long high);

    private static native long[] nativeAsDecimal128(long nativePtr);

    private static native long nativeCreateUUID(String value);

    private static native String nativeAsUUID(long nativePtr);

    private static native long nativeCreateLink(long targetTablePtr, long targetObjectKey);

    private static native int nativeGetType(long nativePtr);

    private static native String nativeGetRealmModelTableName(long nativePtr, long sharedRealmPtr);

    private static native long nativeGetRealmModelRowKey(long nativePtr);

    private static native boolean nativeEquals(long nativePtr, long nativeOtherPtr);

    private static native long nativeGetFinalizerPtr();
}
