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

package io.realm.internal.core;

import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import io.realm.RealmModel;
import io.realm.internal.NativeContext;
import io.realm.internal.NativeObject;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.UncheckedRow;


public final class NativeRealmAnyCollection implements NativeObject {
    private final long nativePtr;

    public static NativeRealmAnyCollection newBooleanCollection(Collection<Boolean> collection) {
        boolean[] booleanValues = new boolean[collection.size()];
        boolean[] notNull = new boolean[collection.size()];

        int i = 0;
        for (Boolean aBoolean : collection) {
            if (aBoolean != null) {
                booleanValues[i] = aBoolean;
                notNull[i] = true;
            }
            i++;
        }
        return new NativeRealmAnyCollection(nativeCreateBooleanCollection(booleanValues, notNull));
    }

    public static NativeRealmAnyCollection newIntegerCollection(Collection<? extends Number> collection) {
        long[] integerValues = new long[collection.size()];
        boolean[] notNull = new boolean[collection.size()];

        int i = 0;
        for (Number number : collection) {
            if (number != null) {
                integerValues[i] = number.longValue();
                notNull[i] = true;
            }
            i++;
        }
        return new NativeRealmAnyCollection(nativeCreateIntegerCollection(integerValues, notNull));
    }

    public static NativeRealmAnyCollection newFloatCollection(Collection<? extends Float> collection) {
        float[] floatValues = new float[collection.size()];
        boolean[] notNull = new boolean[collection.size()];

        int i = 0;
        for (Float aFloat : collection) {
            if (aFloat != null) {
                floatValues[i] = aFloat;
                notNull[i] = true;
            }
            i++;
        }
        return new NativeRealmAnyCollection(nativeCreateFloatCollection(floatValues, notNull));
    }

    public static NativeRealmAnyCollection newDoubleCollection(Collection<? extends Double> collection) {
        double[] doubleValues = new double[collection.size()];
        boolean[] notNull = new boolean[collection.size()];

        int i = 0;
        for (Double aDouble : collection) {
            if (aDouble != null) {
                doubleValues[i] = aDouble;
                notNull[i] = true;
            }
            i++;
        }
        return new NativeRealmAnyCollection(nativeCreateDoubleCollection(doubleValues, notNull));
    }

    public static NativeRealmAnyCollection newStringCollection(Collection<String> collection) {
        String[] stringValues = new String[collection.size()];
        boolean[] notNull = new boolean[collection.size()];

        int i = 0;
        for (String aString : collection) {
            if (aString != null) {
                stringValues[i] = aString;
                notNull[i] = true;
            }
            i++;
        }

        return new NativeRealmAnyCollection(nativeCreateStringCollection(stringValues, notNull));
    }

    public static NativeRealmAnyCollection newBinaryCollection(Collection<? extends byte[]> collection) {
        byte[][] binaryValues = new byte[collection.size()][];
        boolean[] notNull = new boolean[collection.size()];

        int i = 0;
        for (byte[] binaryValue : collection) {
            if (binaryValue != null) {
                binaryValues[i] = binaryValue;
                notNull[i] = true;
            }
            i++;
        }

        return new NativeRealmAnyCollection(nativeCreateBinaryCollection(binaryValues, notNull));
    }

    public static NativeRealmAnyCollection newDateCollection(Collection<? extends Date> collection) {
        long[] dateValues = new long[collection.size()];
        boolean[] notNull = new boolean[collection.size()];

        int i = 0;
        for (Date aDate : collection) {
            if (aDate != null) {
                dateValues[i] = aDate.getTime();
                notNull[i] = true;
            }
            i++;
        }
        return new NativeRealmAnyCollection(nativeCreateDateCollection(dateValues, notNull));
    }

    public static NativeRealmAnyCollection newObjectIdCollection(Collection<? extends ObjectId> collection) {
        String[] objectIdValues = new String[collection.size()];
        boolean[] notNull = new boolean[collection.size()];

        int i = 0;
        for (ObjectId objectId : collection) {
            if (objectId != null) {
                objectIdValues[i] = objectId.toString();
                notNull[i] = true;
            }
            i++;
        }

        return new NativeRealmAnyCollection(nativeCreateObjectIdCollection(objectIdValues, notNull));
    }

    public static NativeRealmAnyCollection newDecimal128Collection(Collection<? extends Decimal128> collection) {
        long[] lowValues = new long[collection.size()];
        long[] highValues = new long[collection.size()];
        boolean[] notNull = new boolean[collection.size()];

        int i = 0;
        for (Decimal128 decimal128 : collection) {
            if (decimal128 != null) {
                lowValues[i] = decimal128.getLow();
                highValues[i] = decimal128.getHigh();
                notNull[i] = true;
            }
            i++;
        }

        return new NativeRealmAnyCollection(nativeCreateDecimal128Collection(lowValues, highValues, notNull));
    }

    public static NativeRealmAnyCollection newUUIDCollection(Collection<? extends UUID> collection) {
        String[] uuidValues = new String[collection.size()];
        boolean[] notNull = new boolean[collection.size()];

        int i = 0;
        for (UUID uuid : collection) {
            if (uuid != null) {
                uuidValues[i] = uuid.toString();
                notNull[i] = true;
            }
            i++;
        }

        return new NativeRealmAnyCollection(nativeCreateUUIDCollection(uuidValues, notNull));
    }

    public static NativeRealmAnyCollection newRealmModelCollection(Collection<? extends RealmModel> collection) {
        long[] objectValues = new long[collection.size()];
        boolean[] notNull = new boolean[collection.size()];

        int i = 0;
        for (RealmModel model : collection) {
            if (model != null) {
                RealmObjectProxy proxy = (RealmObjectProxy) model;
                objectValues[i] = ((UncheckedRow) proxy.realmGet$proxyState().getRow$realm()).getNativePtr();
                notNull[i] = true;
            }
            i++;
        }

        return new NativeRealmAnyCollection(nativeCreateObjectCollection(objectValues, notNull));
    }

    public static NativeRealmAnyCollection newRealmAnyCollection(long[] realmAnyPtrs, boolean[] notNull) {
        return new NativeRealmAnyCollection(nativeCreateRealmAnyCollection(realmAnyPtrs, notNull));
    }

    private NativeRealmAnyCollection(long nativePtr) {
        this.nativePtr = nativePtr;
        NativeContext.dummyContext.addReference(this);
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeGetFinalizerPtr();
    }

    public int getSize(){
        return nativeGetCollectionSize(nativePtr);
    }

    public NativeRealmAny getItem(int index){
        return new NativeRealmAny(nativeGetCollectionItem(nativePtr, index));
    }

    private static native long nativeCreateBooleanCollection(boolean[] booleanValues, boolean[] notNull);

    private static native long nativeCreateIntegerCollection(long[] integerValues, boolean[] notNull);

    private static native long nativeCreateFloatCollection(float[] floatValues, boolean[] notNull);

    private static native long nativeCreateDoubleCollection(double[] doubleValues, boolean[] notNull);

    private static native long nativeCreateStringCollection(String[] stringValues, boolean[] notNull);

    private static native long nativeCreateBinaryCollection(byte[][] binaryValues, boolean[] notNull);

    private static native long nativeCreateDateCollection(long[] dateValues, boolean[] notNull);

    private static native long nativeCreateObjectIdCollection(String[] objectIdValues, boolean[] notNull);

    private static native long nativeCreateDecimal128Collection(long[] lowValues, long[] highValues, boolean[] notNull);

    private static native long nativeCreateUUIDCollection(String[] uuidValues, boolean[] notNull);

    private static native long nativeCreateObjectCollection(long[] objectValues, boolean[] notNull);

    private static native long nativeCreateRealmAnyCollection(long[] realmAnyPtrs, boolean[] notNull);

    private static native int nativeGetCollectionSize(long nativePtr);

    private static native long nativeGetCollectionItem(long nativePtr, int index);

    private static native long nativeGetFinalizerPtr();
}
