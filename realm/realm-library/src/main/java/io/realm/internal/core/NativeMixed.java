package io.realm.internal.core;

import io.realm.MixedType;
import io.realm.RealmModel;
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

    public MixedType getType(){
        return MixedType.fromNativeValue(nativeGetMixedType(nativePtr));
    }

    public boolean asBoolean(){
        return nativeMixedAsBoolean(nativePtr);
    }

    public String getRealmModelTableName(OsSharedRealm osSharedRealm){
        return nativeGetRealmModelTableName(nativePtr, osSharedRealm.getNativePtr());
    }

    public long getRealmModelRowKey(){
        return nativeGetRealmModelRowKey(nativePtr);
    }

    private static native long nativeCreateMixedNull();

    private static native long nativeCreateMixedBoolean(boolean value);

    private static native long nativeCreateMixedLink(long targetTablePtr, long targetObjectKey);

    private static native int nativeGetMixedType(long nativePtr);

    private static native boolean nativeMixedAsBoolean(long nativePtr);

    private static native String nativeGetRealmModelTableName(long nativePtr, long sharedRealmPtr);

    private static native long nativeGetRealmModelRowKey(long nativePtr);

    private static native long nativeGetFinalizerPtr();
}
