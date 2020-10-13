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
package io.realm.internal.objectstore;

import org.bson.Document;

import java.util.concurrent.atomic.AtomicReference;

import io.realm.RealmAsyncTask;
import io.realm.internal.NativeObject;
import io.realm.internal.Util;
import io.realm.internal.jni.JniBsonProtocol;
import io.realm.internal.jni.OsJNIResultCallback;
import io.realm.internal.jni.OsJNIVoidResultCallback;
import io.realm.internal.mongodb.Request;
import io.realm.internal.network.ResultHandler;
import io.realm.internal.util.Pair;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.AppException;

public class OsSyncUser implements NativeObject {

    private final long nativePtr;
    private static final long nativeFinalizerPtr = nativeGetFinalizerMethodPtr();

    public static final byte STATE_LOGGED_IN = 1;
    public static final byte STATE_REMOVED = 2;
    public static final byte STATE_LOGGED_OUT = 3;

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

    public String getLocalIdentity() {
        return nativeGetLocalIdentity(nativePtr);
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
            identities[i/2] = new Pair<>(identityData[i], identityData[i+1]);
        }
        return identities;
    }

    public String getDeviceId() {
        return nativeGetDeviceId(nativePtr);
    }

    /**
     * @return {@link #STATE_LOGGED_IN}, {@link #STATE_LOGGED_OUT} or {@link #STATE_REMOVED}
     */
    public byte getState() {
        return nativeGetState(nativePtr);
    }

    public Document getCustomData() {
        String encodedData = nativeCustomData(nativePtr);
        // Stitch also used default codec registry for parsing access token
        return JniBsonProtocol.decode(encodedData, AppConfiguration.DEFAULT_BSON_CODEC_REGISTRY.get(Document.class));
    }

    public void refreshCustomData() {
        AtomicReference<AppException> error = new AtomicReference<>(null);
        nativeRefreshCustomData(nativePtr, new OsJNIVoidResultCallback(error));
        ResultHandler.handleResult(null, error);
    }

    public void invalidate() {
        nativeSetState(nativePtr, STATE_REMOVED);
    }

    public String getProviderType() {
        return nativeGetProviderType(nativePtr);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OsSyncUser that = (OsSyncUser) o;
        return getIdentity().equals(that.getIdentity());
    }

    @Override
    public int hashCode() {
        return getIdentity().hashCode();
    }

    private static native long nativeGetFinalizerMethodPtr();
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
    private static native String nativeGetLocalIdentity(long nativePtr);
    private static native String nativeGetAccessToken(long nativePtr);
    private static native String nativeGetRefreshToken(long nativePtr);
    private static native String[] nativeGetIdentities(long nativePtr); // Returns pairs of {id, provider}
    private static native byte nativeGetState(long nativePtr);
    private static native void nativeSetState(long nativePtr, byte state);
    private static native String nativeGetProviderType(long nativePtr);
    private static native String nativeGetDeviceId(long nativePtr);
    private static native String nativeCustomData(long nativeUserPtr);
    private static native void nativeRefreshCustomData(long nativeUserPtr, OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
}
