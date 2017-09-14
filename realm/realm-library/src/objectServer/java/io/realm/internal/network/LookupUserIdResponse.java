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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.log.RealmLog;
import okhttp3.Response;

/**
 * Class wrapping the response from `GET /auth/users/:userId`
 */
public class LookupUserIdResponse extends AuthServerResponse {

    private static final String JSON_FIELD_USER_ID = "userId";
    private static final String JSON_FIELD_USER_IS_ADMIN = "isAdmin";
    private static final String JSON_FIELD_METADATA =  "metadata";

    private final String userId;
    private final Boolean isAdmin;
    private final Map<String, String> metadata;

    /**
     * Helper method for creating the proper lookup user response. This method will set the appropriate error
     * depending on any HTTP response codes or I/O errors.
     *
     * @param response the server response.
     * @return the user lookup response.
     */
    static LookupUserIdResponse from(Response response) {
        String serverResponse;
        try {
            serverResponse = response.body().string();
        } catch (IOException e) {
            ObjectServerError error = new ObjectServerError(ErrorCode.IO_EXCEPTION, e);
            return new LookupUserIdResponse(error);
        }
        if (!response.isSuccessful()) {
            return new LookupUserIdResponse(AuthServerResponse.createError(serverResponse, response.code()));
        } else {
            return new LookupUserIdResponse(serverResponse);
        }
    }

    /**
     * Helper method for creating a failed response.
     */
    public static LookupUserIdResponse from(ObjectServerError objectServerError) {
        return new LookupUserIdResponse(objectServerError);
    }

    /**
     * Helper method for creating a failed response from an {@link Exception}.
     */
    public static LookupUserIdResponse from(Exception exception) {
        return LookupUserIdResponse.from(new ObjectServerError(ErrorCode.fromException(exception), exception));
    }

    private LookupUserIdResponse(ObjectServerError error) {
        RealmLog.debug("LookupUserIdResponse - Error: " + error);
        setError(error);
        this.error = error;
        this.userId = null;
        this.isAdmin = null;
        this.metadata = new HashMap<>();
    }

    private LookupUserIdResponse(String serverResponse) {
        ObjectServerError error;
        String userId;
        Boolean isAdmin;
        String message;
        Map<String, String> metadata;
        try {
            JSONObject obj = new JSONObject(serverResponse);
            if (obj != null) {
                userId = obj.getString(JSON_FIELD_USER_ID);
                isAdmin = obj.getBoolean(JSON_FIELD_USER_IS_ADMIN);
                metadata = jsonToMap(obj.getJSONObject(JSON_FIELD_METADATA));
                error = null;

                message = String.format(Locale.US, "Identity %s; Path %b", userId, isAdmin);

            } else {
                userId = null;
                isAdmin = null;
                metadata = new HashMap<>();
                error = null;
                message = "user = null";
            }

        } catch (JSONException e) {
            userId = null;
            isAdmin = null;
            metadata = new HashMap<>();
            error = new ObjectServerError(ErrorCode.JSON_EXCEPTION, e);
            message = String.format(Locale.US, "Error %s", error.getErrorMessage());
        }

        RealmLog.debug("LookupUserIdResponse. " + message);
        setError(error);
        this.userId = userId;
        this.isAdmin = isAdmin;
        this.metadata = metadata;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public Map<String, String> getMetadata() { return metadata; }

    private static Map<String, String> jsonToMap(JSONObject json) throws JSONException {
        Map<String, String> map = new HashMap<>();
        if(json != JSONObject.NULL) {
            map = toMap(json);
        }
        return map;
    }

    private static Map<String, String> toMap(JSONObject object) throws JSONException {
        Map<String, String> map = new HashMap<>();
        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            String value = object.getString(key);
            map.put(key, value);
        }
        return map;
    }
}
