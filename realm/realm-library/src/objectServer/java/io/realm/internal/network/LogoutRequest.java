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

import io.realm.internal.objectserver.Token;

/**
 * This class encapsulates a request to log out a user on the Realm Authentication Server. It is responsible for
 * constructing the JSON understood by the Realm Authentication Server.
 */
public class LogoutRequest {

    private final String token;

    public static LogoutRequest create(Token userToken) {
        return new LogoutRequest(userToken.value());
    }

    private LogoutRequest(String token) {
        this.token = token;
    }

    /**
     * Converts the request into a JSON payload.
     */
    public String toJson() {
        try {
            JSONObject request = new JSONObject();
            request.put("token", token);
            return request.toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
