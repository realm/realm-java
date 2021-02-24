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


import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.UUID;

import javax.annotation.Nullable;


public class NativeArgumentList implements NativeObject {
    private final long nativePtr;

    public NativeArgumentList(NativeContext context) {
        nativePtr = nativeCreate();
        context.addReference(this);
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeGetFinalizerPtr();
    }

    public long insertLong(@Nullable Number value) {
        return (value == null) ? nativeInsertNull(nativePtr) : nativeInsertInteger(nativePtr, value.longValue());
    }

    public long insertFloat(@Nullable Float value) {
        return (value == null) ? nativeInsertNull(nativePtr) : nativeInsertFloat(nativePtr, value);
    }

    public long insertDouble(@Nullable Double value) {
        return (value == null) ? nativeInsertNull(nativePtr) : nativeInsertDouble(nativePtr, value);
    }

    public long insertBoolean(@Nullable Boolean value) {
        return (value == null) ? nativeInsertNull(nativePtr) : nativeInsertBoolean(nativePtr, value);
    }

    public long insertDate(@Nullable Date value) {
        return (value == null) ? nativeInsertNull(nativePtr) : nativeInsertDate(nativePtr, value.getTime());
    }

    public long insertString(@Nullable String value) {
        return (value == null) ? nativeInsertNull(nativePtr) : nativeInsertString(nativePtr, value);
    }

    public long insertByteArray(@Nullable byte[] value) {
        return (value == null) ? nativeInsertNull(nativePtr) : nativeInsertByteArray(nativePtr, value);
    }

    public long insertDecimal128(@Nullable Decimal128 value) {
        return (value == null) ? nativeInsertNull(nativePtr) : nativeInsertDecimal128(nativePtr, value.getLow(), value.getHigh());
    }

    public long insertObjectId(@Nullable ObjectId value) {
        return (value == null) ? nativeInsertNull(nativePtr) : nativeInsertObjectId(nativePtr, value.toString());
    }

    public long insertUUID(@Nullable UUID value) {
        return (value == null) ? nativeInsertNull(nativePtr) : nativeInsertUUID(nativePtr, value.toString());
    }

    public long insertObject(long objectPtr){
        return nativeInsertObject(nativePtr, objectPtr);
    }

    public long insertNull() {
        return nativeInsertNull(nativePtr);
    }

    private static native long nativeCreate();

    private static native long nativeInsertNull(long listPtr);

    private static native long nativeInsertInteger(long listPtr, long val);

    private static native long nativeInsertString(long listPtr, String val);

    private static native long nativeInsertFloat(long listPtr, float val);

    private static native long nativeInsertDouble(long listPtr, double val);

    private static native long nativeInsertBoolean(long listPtr, boolean val);

    private static native long nativeInsertByteArray(long listPtr, byte[] val);

    private static native long nativeInsertDate(long listPtr, long val);

    private static native long nativeInsertDecimal128(long listPtr, long low, long high);

    private static native long nativeInsertObjectId(long listPtr, String data);

    private static native long nativeInsertUUID(long listPtr, String data);

    private static native long nativeInsertObject(long listPtr, long rowPtr);

    private static native long nativeGetFinalizerPtr();
}
