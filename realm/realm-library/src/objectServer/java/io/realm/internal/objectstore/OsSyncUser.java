package io.realm.internal.objectstore;

import io.realm.internal.NativeObject;

public class OsSyncUser implements NativeObject {

    private final long nativePtr;

    public OsSyncUser(long nativePtr) {
        this.nativePtr = nativePtr;
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return 0;
    }

    public String nativeGetName() {
        return nativeGetName(nativePtr);
    }

    public String getEmail() {
        return nativeGetEmail(nativePtr);
    }

    public String getPictureUrl() {
        return nativeGetPictureUrl(nativePtr);
    }

    public String getFirstName() {
        return nativeGetFirstName(nativePtr);
    }

    public String getLastName() {
        return nativeGetLastName(nativePtr);
    }

    public String getGender() {
        return nativeGetGender(nativePtr);
    }

    public String getBirthday() {
        return nativeGetBirthDay(nativePtr);
    }

    public String getMinAge() {
        return nativeGetMinAge(nativePtr);
    }

    public String getMaxAge() {
        return nativeGetMaxAge(nativePtr);
    }

    private static native String nativeGetName(long nativePtr);
    private static native String nativeGetEmail(long nativePtr);
    private static native String nativeGetPictureUrl(long nativePtr);
    private static native String nativeGetFirstName(long nativePtr);
    private static native String nativeGetLastName(long nativePtr);
    private static native String nativeGetGender(long nativePtr);
    private static native String nativeGetBirthDay(long nativePtr);
    private static native String nativeGetMinAge(long nativePtr);
    private static native String nativeGetMaxAge(long nativePtr);
}
