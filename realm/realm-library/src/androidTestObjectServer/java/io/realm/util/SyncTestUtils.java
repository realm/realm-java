/*
 * Copyright 2016 Realm Inc.
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

package io.realm.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.SyncConfiguration;
import io.realm.SyncManager;
import io.realm.SyncSession;
import io.realm.SyncUser;
import io.realm.UserStore;
import io.realm.internal.network.AuthenticateResponse;
import io.realm.internal.objectserver.Token;

public class SyncTestUtils {

    public static final String USER_TOKEN = UUID.randomUUID().toString();
    public static final String DEFAULT_AUTH_URL = "http://objectserver.realm.io/auth";

    private final static Method SYNC_MANAGER_GET_USER_STORE_METHOD;
    private final static Method SYNC_USER_GET_ACCESS_TOKEN_METHOD;
    static {
        try {
            SYNC_MANAGER_GET_USER_STORE_METHOD = SyncManager.class.getDeclaredMethod("getUserStore");
            SYNC_USER_GET_ACCESS_TOKEN_METHOD = SyncUser.class.getDeclaredMethod("getRefreshToken");
            SYNC_MANAGER_GET_USER_STORE_METHOD.setAccessible(true);
            SYNC_USER_GET_ACCESS_TOKEN_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    public static SyncUser createTestAdminUser() {
        return createTestUser(USER_TOKEN, UUID.randomUUID().toString(), DEFAULT_AUTH_URL, Long.MAX_VALUE, true);
    }

    public static SyncUser createTestUser() {
        return createTestUser(USER_TOKEN, UUID.randomUUID().toString(), DEFAULT_AUTH_URL, Long.MAX_VALUE, false);
    }

    public static SyncUser createTestUser(long expires) {
        return createTestUser(USER_TOKEN, UUID.randomUUID().toString(), DEFAULT_AUTH_URL, expires, false);
    }

    public static SyncUser createTestUser(String authUrl) {
        return createTestUser(USER_TOKEN, UUID.randomUUID().toString(), authUrl, Long.MAX_VALUE, false);
    }

    public static SyncUser createNamedTestUser(String userIdentifier) {
        return createTestUser(USER_TOKEN, userIdentifier, DEFAULT_AUTH_URL, Long.MAX_VALUE, false);
    }

    public static SyncUser createTestUser(String userTokenValue, String userIdentifier, String authUrl, long expires, boolean isAdmin) {
        Token userToken = new Token(userTokenValue, userIdentifier, null, expires, null, isAdmin);

        JSONObject obj = new JSONObject();
        try {
            JSONObject realmDesc = new JSONObject();
            realmDesc.put("uri", "realm://objectserver.realm.io/default");

            obj.put("authUrl", authUrl);
            obj.put("userToken", userToken.toJson());
            SyncUser syncUser = SyncUser.fromJson(obj.toString());
            // persist the user to the ObjectStore sync metadata, to simulate real login, otherwise SyncUser.isValid will
            // "throw IllegalArgumentException: User not authenticated or authentication expired." since
            // the call to  SyncManager.getUserStore().isActive(syncUser.getIdentity()) will return false
            addToUserStore(syncUser);
            return syncUser;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static AuthenticateResponse createLoginResponse(long expires) {
        return createLoginResponse(USER_TOKEN, "JohnDoe", expires, false);
    }

    public static AuthenticateResponse createLoginResponse(String userTokenValue, String userIdentity, long expires, boolean isAdmin) {
        try {
            Token userToken = new Token(userTokenValue, userIdentity, null, expires, null, isAdmin);
            JSONObject response = new JSONObject();
            response.put("refresh_token", userToken.toJson());
            return AuthenticateResponse.from(response.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static AuthenticateResponse createErrorResponse(ErrorCode code) {
        return AuthenticateResponse.from(new ObjectServerError(code, "dummy"));
    }

    public static Token getRefreshToken(SyncUser user) {
        try {
            return (Token) SYNC_USER_GET_ACCESS_TOKEN_METHOD.invoke(user);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new AssertionError(e);
        }
    }

    private static void addToUserStore(SyncUser user) {
        try {
            UserStore userStore = (UserStore) SYNC_MANAGER_GET_USER_STORE_METHOD.invoke(null);
            userStore.put(user);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    // Fully synchronize a Realm with the server by making sure that all changes are uploaded
    // and downloaded again.
    public static void syncRealm(Realm realm) {
        SyncConfiguration config = (SyncConfiguration) realm.getConfiguration();
        SyncSession session = SyncManager.getSession(config);
        try {
            session.uploadAllLocalChanges();
            session.downloadAllServerChanges();
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
        realm.refresh();
    }
}
