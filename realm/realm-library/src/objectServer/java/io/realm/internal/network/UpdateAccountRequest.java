/*
 * Copyright 2018 Realm Inc.
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

import java.util.HashMap;
import java.util.Map;

import io.realm.internal.Util;

/**
 * This class encapsulates the JSON request body when either doing a password reset or an email confirmation
 * flow.
 */
public class UpdateAccountRequest {

    private static final Map<String, String> NO_DATA = new HashMap<>();

    private final String action;
    private final Map<String, String> data;
    private final String providerId; // Should be an email address, but let server validate that.

    public static UpdateAccountRequest requestPasswordReset(String email) {
        return new UpdateAccountRequest("reset_password", NO_DATA, email);
    }

    public static UpdateAccountRequest completePasswordReset(String resetPasswordToken, String newPassword) {
        Map<String, String> data = new HashMap<>();
        data.put("token", resetPasswordToken);
        data.put("new_password", newPassword);
        return new UpdateAccountRequest("complete_reset", data, null);
    }

    public static UpdateAccountRequest requestEmailConfirmation(String email) {
        return new UpdateAccountRequest("request_email_confirmation", NO_DATA, email);
    }

    public static UpdateAccountRequest completeEmailConfirmation(String confirmEmailToken) {
        Map<String, String> data = new HashMap<>();
        data.put("token", confirmEmailToken);
        return new UpdateAccountRequest("confirm_email", data, null);
    }

    private UpdateAccountRequest(String action, Map<String, String> data, String providerId) {
        this.action = action;
        this.data = data;
        this.providerId = providerId;
    }

    /**
     * Converts the request into a JSON payload.
     */
    public String toJson() {
        Map<String, Object> payload = new HashMap<String, Object>() {{
            if (!Util.isEmptyString(providerId)) {
                put("provider_id", providerId);
            }
            data.put("action", action);
            put("data", data);
        }};

        return new JSONObject(payload).toString();
    }
}
