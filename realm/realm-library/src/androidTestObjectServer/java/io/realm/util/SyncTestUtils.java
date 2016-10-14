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

    public static SyncUser createTestUser() {
        return createTestUser(Long.MAX_VALUE);
    }

    public static SyncUser createTestUser(long expires) {
        Token userToken = new Token(USER_TOKEN, "JohnDoe", null, expires, null);
        Token accessToken = new Token(REALM_TOKEN, "JohnDoe", "/foo", expires, new Token.Permission[] {Token.Permission.DOWNLOAD });
        ObjectServerUser.AccessDescription desc = new ObjectServerUser.AccessDescription(accessToken, "/data/data/myapp/files/default", false);

        JSONObject obj = new JSONObject();
        try {
            JSONArray realmList = new JSONArray();
            JSONObject realmDesc = new JSONObject();
            realmDesc.put("uri", "realm://objectserver.realm.io/default");
            realmDesc.put("description", desc.toJson());
            realmList.put(realmDesc);

            obj.put("authUrl", "http://objectserver.realm.io/auth");
            obj.put("userToken", userToken.toJson());
            obj.put("realms", realmList);
            return SyncUser.fromJson(obj.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static AuthenticateResponse createLoginResponse(long expires) {
        try {
            Token userToken = new Token(USER_TOKEN, "JohnDoe", null, expires, null);
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
