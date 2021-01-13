package io.realm.internal.core;

import io.realm.MixedType;
import io.realm.internal.NativeContext;
import io.realm.internal.NativeObject;


public class NativeMixed implements NativeObject {
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    private final long nativePtr;

    public static NativeMixed newInstance(NativeContext context, Boolean value) {
        return new NativeMixed(nativeCreateMixedBoolean(value), context);
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

    private static native long nativeCreateMixedNull();

    private static native long nativeCreateMixedBoolean(boolean value);

    private static native int nativeGetMixedType(long nativePtr);

    private static native boolean nativeMixedAsBoolean(long nativePtr);

    private static native long nativeGetFinalizerPtr();
}
