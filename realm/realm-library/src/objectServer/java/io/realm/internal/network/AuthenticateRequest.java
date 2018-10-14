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

package io.realm.internal.network;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import io.realm.internal.objectserver.Token;
import io.realm.SyncCredentials;
import io.realm.SyncManager;

/**
 * This class encapsulates a request to authenticate a user on the Realm Authentication Server. It is responsible for
 * constructing the JSON understood by the Realm Authentication Server.
 */
public class AuthenticateRequest {

    private final String provider;
    private final String data;
    private final String appId;
    private final Map<String, Object> userInfo;
    private final String path;

    /**
     * Generates a proper login request for a new user.
     */
    public static AuthenticateRequest userLogin(SyncCredentials credentials) {
        if (credentials == null) {
           throw new IllegalArgumentException("Non-null credentials required.");
        }
        String provider = credentials.getIdentityProvider();
        String data = credentials.getUserIdentifier();
        Map<String, Object> userInfo = credentials.getUserInfo();
        String appId = SyncManager.APP_ID;
        return new AuthenticateRequest(provider, data, appId, null, userInfo);
    }

    /**
     * Generates a request for refreshing a user token.
     */
    public static AuthenticateRequest userRefresh(Token userToken, String serverUrl) {
        return new AuthenticateRequest("realm",
                userToken.value(),
                SyncManager.APP_ID,
                serverUrl,
                Collections.<String, Object>emptyMap()
        );
    }

    /**
     * Generates a request for accessing a Realm
     */
    public static AuthenticateRequest realmLogin(Token userToken, String serverUrl) {
        // Authenticate a given Realm path using an already logged in user.
        return new AuthenticateRequest("realm",
                userToken.value(),
                SyncManager.APP_ID,
                serverUrl,
                Collections.<String, Object>emptyMap()
        );
    }

    private AuthenticateRequest(String provider, String data, String appId, String path, Map<String, Object> userInfo) {
        this.provider = provider;
        this.data = data;
        this.appId = appId;
        this.path = path;
        this.userInfo = userInfo;
    }

    /**
     * Converts the request into a JSON payload.
     */
    public String toJson() {
        JSONObject request = new JSONObject();
        try {
            request.put("provider", provider);
            request.put("data", data);
            request.put("app_id", appId);
            if (path != null) {
                request.put("path", path);
            }
            request.put("user_info", new JSONObject(userInfo));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return request.toString();
    }
}
