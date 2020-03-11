package io.realm.internal.objectstore;

import io.realm.internal.NativeObject;
import io.realm.internal.util.Pair;

public class OsSyncUser implements NativeObject {

    private final long nativePtr;
    private static final long nativeFinalizerPtr = nativeGetFinalizerMethodPtr();

    public OsSyncUser(long nativePtr) {
        this.nativePtr = nativePtr;
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
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
        return nativeGetBirthday(nativePtr);
    }

    public String getMinAge() {
        return nativeGetMinAge(nativePtr);
    }

    public String getMaxAge() {
        return nativeGetMaxAge(nativePtr);
    }

    public String getIdentity() {
        return nativeGetIdentity(nativePtr);
    }

    public String getAccessToken() {
        return nativeGetAccessToken(nativePtr);
    }

    public String getRefreshToken() {
        return nativeGetRefreshToken(nativePtr);
    }

    public Pair<String, String>[] getIdentities() {
        String[] identityData = nativeGetIdentities(nativePtr);
        @SuppressWarnings("unchecked")
        Pair<String, String>[] identities = new Pair[identityData.length/2];
        for (int i = 0; i < identityData.length; i = i + 2) {
            identities[i] = new Pair<>(identityData[i], identityData[i+1]);
        }
        return identities;
    }


    private static native long nativeGetFinalizerMethodPtr();

    // Profile data
    private static native String nativeGetName(long nativePtr);
    private static native String nativeGetEmail(long nativePtr);
    private static native String nativeGetPictureUrl(long nativePtr);
    private static native String nativeGetFirstName(long nativePtr);
    private static native String nativeGetLastName(long nativePtr);
    private static native String nativeGetGender(long nativePtr);
    private static native String nativeGetBirthday(long nativePtr);
    private static native String nativeGetMinAge(long nativePtr);
    private static native String nativeGetMaxAge(long nativePtr);

    private static native String nativeGetIdentity(long nativePtr);
    private static native String nativeGetAccessToken(long nativePtr);
    private static native String nativeGetRefreshToken(long nativePtr);
    private static native String[] nativeGetIdentities(long nativePtr); // Returns pairs of {id, provider}
}



