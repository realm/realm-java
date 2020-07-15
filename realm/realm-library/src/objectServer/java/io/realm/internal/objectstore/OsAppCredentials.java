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

import io.realm.internal.NativeObject;
import io.realm.internal.jni.JniBsonProtocol;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.AppException;
import io.realm.mongodb.Credentials;

/**
 * Class wrapping ObjectStores {@code realm::app::AppCredentials}.
 */
public class OsAppCredentials implements NativeObject {

    private static final int TYPE_ANONYMOUS = 1;
    private static final int TYPE_API_KEY = 2;
    private static final int TYPE_SERVER_API_KEY = 3;
    private static final int TYPE_APPLE = 4;
    private static final int TYPE_CUSTOM_FUNCTION = 5;
    private static final int TYPE_EMAIL_PASSWORD = 6;
    private static final int TYPE_FACEBOOK = 7;
    private static final int TYPE_GOOGLE = 8;
    private static final int TYPE_JWT = 9;
    private static final long finalizerPtr = nativeGetFinalizerMethodPtr();

    public static OsAppCredentials anonymous() {
        return new OsAppCredentials(nativeCreate(TYPE_ANONYMOUS));
    }

    public static OsAppCredentials apiKey(String key) {
        return new OsAppCredentials(nativeCreate(TYPE_API_KEY, key));
    }

    public static OsAppCredentials serverApiKey(String key) {
        return new OsAppCredentials(nativeCreate(TYPE_SERVER_API_KEY, key));
    }

    public static OsAppCredentials apple(String idToken) {
        return new OsAppCredentials(nativeCreate(TYPE_APPLE, idToken));
    }

    public static OsAppCredentials customFunction(Document args) {
        String encodedArgs = JniBsonProtocol.encode(args, AppConfiguration.DEFAULT_BSON_CODEC_REGISTRY);
        return new OsAppCredentials(nativeCreate(TYPE_CUSTOM_FUNCTION, encodedArgs));
    }

    public static OsAppCredentials emailPassword(String email, String password) {
        return new OsAppCredentials(nativeCreate(TYPE_EMAIL_PASSWORD, email, password));
    }

    public static OsAppCredentials facebook(String accessToken) {
        return new OsAppCredentials(nativeCreate(TYPE_FACEBOOK, accessToken));
    }

    public static OsAppCredentials google(String whatToCallThisToken) {
        return new OsAppCredentials(nativeCreate(TYPE_GOOGLE, whatToCallThisToken));
    }

    public static OsAppCredentials jwt(String jwtToken) {
        return new OsAppCredentials(nativeCreate(TYPE_JWT, jwtToken));
    }

    private final long nativePtr;

    private OsAppCredentials(long nativePtr) {
        this.nativePtr = nativePtr;
    }

    public String getProvider() {
        return nativeGetProvider(nativePtr);
    }

    public String asJson() {
        return nativeAsJson(nativePtr);
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return finalizerPtr;
    }

    private static native long nativeCreate(int type, Object... args);
    private static native String nativeGetProvider(long nativePtr);
    private static native String nativeAsJson(long nativePtr);
    private static native long nativeGetFinalizerMethodPtr();
}
