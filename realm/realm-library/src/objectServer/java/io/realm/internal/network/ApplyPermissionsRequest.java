/*
 * Copyright 2019 Realm Inc.
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

import io.realm.permissions.AccessLevel;
import io.realm.permissions.PermissionRequest;
import io.realm.permissions.UserCondition;

/**
 * Class wrapping a request for updating/setting permissions `POST permissions/apply`
 */
public class ApplyPermissionsRequest {

    private final AccessLevel level;
    private final String realmUrl;
    private final String userId;
    private final String metadataKey;
    private final String metadataValue;

    public ApplyPermissionsRequest(PermissionRequest request) {
        UserCondition condition = request.getCondition();
        level = request.getAccessLevel();
        realmUrl = request.getUrl();

        switch (condition.getType()) {
            case USER_ID:
                userId = condition.getValue();
                metadataKey = null;
                metadataValue = null;
                break;
            case METADATA:
                userId = null;
                metadataKey = condition.getKey();
                metadataValue = condition.getValue();
                break;
            default:
                throw new IllegalArgumentException("Unsupported type: " + condition.getType());
        }
    }

    public String toJson() throws JSONException {
        JSONObject request = new JSONObject();
        request.put("realmPath", realmUrl);
        request.put("accessLevel", level.getKey());
        JSONObject condition = new JSONObject();
        if (userId != null) {
            condition.put("userId", userId);
        } else {
            condition.put("metadataKey", metadataKey);
            condition.put("metadataValue", metadataValue);
        }
        request.put("condition", condition);
        return request.toString();
    }
}
