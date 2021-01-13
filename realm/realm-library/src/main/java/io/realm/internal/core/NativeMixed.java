package io.realm.internal.core;

import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.UUID;

import io.realm.MixedType;
import io.realm.internal.NativeContext;
import io.realm.internal.NativeObject;
import io.realm.internal.OsSharedRealm;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;


public class NativeMixed implements NativeObject {
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    private final long nativePtr;

    public static NativeMixed newInstance(NativeContext context, Boolean value) {
        return new NativeMixed(nativeCreateMixedBoolean(value), context);
    }

    public static NativeMixed newInstance(NativeContext context, Number value) {
        return new NativeMixed(nativeCreateMixedLong(value.longValue()), context);
    }

    public static NativeMixed newInstance(NativeContext context, Float value) {
        return new NativeMixed(nativeCreateMixedFloat(value), context);
    }

    public static NativeMixed newInstance(NativeContext context, Double value) {
        return new NativeMixed(nativeCreateMixedDouble(value), context);
    }

    public static NativeMixed newInstance(NativeContext context, String value) {
        return new NativeMixed(nativeCreateMixedString(value), context);
    }

    public static NativeMixed newInstance(NativeContext context, byte[] value) {
        return new NativeMixed(nativeCreateMixedBinary(value), context);
    }

    public static NativeMixed newInstance(NativeContext context, Date value) {
        return new NativeMixed(nativeCreateMixedDate(value.getTime()), context);
    }

    public static NativeMixed newInstance(NativeContext context, ObjectId value) {
        return new NativeMixed(nativeCreateMixedObjectId(value.toString()), context);
    }

    public static NativeMixed newInstance(NativeContext context, Decimal128 value) {
        return new NativeMixed(nativeCreateMixedDecimal128(value.getHigh(), value.getLow()), context);
    }

    public static NativeMixed newInstance(NativeContext context, UUID value) {
        return new NativeMixed(nativeCreateMixedUUID(value.toString()), context);
    }

    public static NativeMixed newInstance(NativeContext context, RealmObjectProxy model) {
        Row row$realm = model.realmGet$proxyState().getRow$realm();

        long targetTablePtr = row$realm.getTable().getNativePtr();
        long targetObjectKey = row$realm.getObjectKey();

        return new NativeMixed(nativeCreateMixedLink(targetTablePtr, targetObjectKey), context);
    }

    public static NativeMixed newInstance(NativeContext context) {
        return new NativeMixed(nativeCreateMixedNull(), context);
    }

    public NativeMixed(long nativePtr, NativeContext context) {
        this.nativePtr = nativePtr;
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

    public MixedType getType() {
        return MixedType.fromNativeValue(nativeGetMixedType(nativePtr));
    }

    public boolean asBoolean() {
        return nativeMixedAsBoolean(nativePtr);
    }

    public long asLong() {
        return nativeMixedAsLong(nativePtr);
    }

    public float asFloat() {
        return nativeMixedAsFloat(nativePtr);
    }

    public double asDouble() {
        return nativeMixedAsDouble(nativePtr);
    }

    public String asString() {
        return nativeMixedAsString(nativePtr);
    }

    public byte[] asBinary() {
        return nativeMixedAsBinary(nativePtr);
    }

    public Date asDate() {
        return new Date(nativeMixedAsDate(nativePtr));
    }

    public ObjectId asObjectId() {
        return new ObjectId(nativeMixedAsObjectId(nativePtr));
    }

    public Decimal128 asDecimal128() {
        long[] data = nativeMixedAsDecimal128(nativePtr);
        return Decimal128.fromIEEE754BIDEncoding(data[0], data[1]);
    }

    public UUID asUUID() {
        return UUID.fromString(nativeMixedAsUUID(nativePtr));
    }

    public String getRealmModelTableName(OsSharedRealm osSharedRealm) {
        return nativeGetRealmModelTableName(nativePtr, osSharedRealm.getNativePtr());
    }

    public long getRealmModelRowKey() {
        return nativeGetRealmModelRowKey(nativePtr);
    }

    private static native long nativeCreateMixedNull();

    private static native long nativeCreateMixedBoolean(boolean value);

    private static native boolean nativeMixedAsBoolean(long nativePtr);

    private static native long nativeCreateMixedLong(long value);

    private static native long nativeMixedAsLong(long nativePtr);

    private static native long nativeCreateMixedFloat(float value);

    private static native float nativeMixedAsFloat(long nativePtr);

    private static native long nativeCreateMixedDouble(double value);

    private static native double nativeMixedAsDouble(long nativePtr);

    private static native long nativeCreateMixedString(String value);

    private static native String nativeMixedAsString(long nativePtr);

    private static native long nativeCreateMixedBinary(byte[] value);

    private static native byte[] nativeMixedAsBinary(long nativePtr);

    private static native long nativeCreateMixedDate(long value);

    private static native long nativeMixedAsDate(long nativePtr);

    private static native long nativeCreateMixedObjectId(String value);

    private static native String nativeMixedAsObjectId(long nativePtr);

    private static native long nativeCreateMixedDecimal128(long high, long low);

    private static native long[] nativeMixedAsDecimal128(long nativePtr);

    private static native long nativeCreateMixedUUID(String value);

    private static native String nativeMixedAsUUID(long nativePtr);

    private static native long nativeCreateMixedLink(long targetTablePtr, long targetObjectKey);

    private static native int nativeGetMixedType(long nativePtr);

    private static native String nativeGetRealmModelTableName(long nativePtr, long sharedRealmPtr);

    private static native long nativeGetRealmModelRowKey(long nativePtr);

    private static native long nativeGetFinalizerPtr();
}
