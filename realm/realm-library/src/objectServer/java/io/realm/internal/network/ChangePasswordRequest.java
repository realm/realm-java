/*
 * Copyright 2017 Realm Inc.
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
 * This class encapsulates a request to change the password for a user on the Realm Authentication Server. It is
 * responsible for constructing the JSON understood by the Realm Authentication Server.
 */
public class ChangePasswordRequest {

    private final String token;
    private final String newPassword;
    private String userID; //optional, used to change the password when using the admin account.

    public static ChangePasswordRequest create(Token userToken, String newPassword) {
        return new ChangePasswordRequest(userToken.value(), newPassword);
    }

    public static ChangePasswordRequest create(Token adminToken, String userID, String newPassword) {
        return new ChangePasswordRequest(adminToken.value(), newPassword, userID);
    }

    private ChangePasswordRequest(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }

    private ChangePasswordRequest(String token, String newPassword, String userID) {
        this.token = token;
        this.newPassword = newPassword;
        this.userID = userID;
    }

    /**
     * Converts the request into a JSON payload.
     */
    public String toJson() {
        try {
            JSONObject request = new JSONObject();
            if (userID != null) {
                request.put("user_id", userID);
            }
            JSONObject data = new JSONObject();
            data.put("new_password", newPassword);
            request.put("data", data);
            return request.toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
