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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.SyncUser;
import io.realm.internal.network.AuthenticateResponse;
import io.realm.internal.objectserver.ObjectServerUser;
import io.realm.internal.objectserver.Token;

public class SyncTestUtils {

    public static String USER_TOKEN = UUID.randomUUID().toString();
    public static String REALM_TOKEN = UUID.randomUUID().toString();
    public static String DEFAULT_AUTH_URL = "http://objectserver.realm.io/auth";

    public static SyncUser createRandomTestUser() {
        return createTestUser(UUID.randomUUID().toString(), UUID.randomUUID().toString(), DEFAULT_AUTH_URL, Long.MAX_VALUE);
    }

    public static SyncUser createTestUser() {
        return createTestUser(USER_TOKEN, REALM_TOKEN, DEFAULT_AUTH_URL, Long.MAX_VALUE);
    }

    public static SyncUser createTestUser(long expires) {
        return createTestUser(USER_TOKEN, REALM_TOKEN, DEFAULT_AUTH_URL, expires);
    }

    public static SyncUser createTestUser(String authUrl) {
        return createTestUser(USER_TOKEN, REALM_TOKEN, authUrl, Long.MAX_VALUE);
    }

    public static SyncUser createTestUser(String userTokenValue, String realmTokenValue, String authUrl, long expires) {
        Token userToken = new Token(userTokenValue, "JohnDoe", null, expires, null);
        Token accessToken = new Token(realmTokenValue, "JohnDoe", "/foo", expires, new Token.Permission[] {Token.Permission.DOWNLOAD });
        ObjectServerUser.AccessDescription desc = new ObjectServerUser.AccessDescription(accessToken, "/data/data/myapp/files/default", false);

        JSONObject obj = new JSONObject();
        try {
            JSONArray realmList = new JSONArray();
            JSONObject realmDesc = new JSONObject();
            realmDesc.put("uri", "realm://objectserver.realm.io/default");
            realmDesc.put("description", desc.toJson());
            realmList.put(realmDesc);

            obj.put("authUrl", authUrl);
            obj.put("userToken", userToken.toJson());
            obj.put("realms", realmList);
            return SyncUser.fromJson(obj.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static AuthenticateResponse createLoginResponse(long expires) {
        return createLoginResponse(USER_TOKEN, "JohnDoe", expires);
    }

    public static AuthenticateResponse createLoginResponse(String userTokenValue, String userIdentity, long expires) {
        try {
            Token userToken = new Token(userTokenValue, userIdentity, null, expires, null);
            JSONObject response = new JSONObject();
            response.put("refresh_token", userToken.toJson());
            return AuthenticateResponse.from(response.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static AuthenticateResponse createRefreshResponse() {
        try {
            Token userToken = new Token(USER_TOKEN, "JohnDoe", null, Long.MAX_VALUE, null);
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
}
