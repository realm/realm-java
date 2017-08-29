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
import java.util.Locale;

import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.log.RealmLog;
import okhttp3.Response;

/**
 * Class wrapping the response from `GET /api/providers/:provider/accounts/:provider_id`
 */
public class LookupUserIdResponse extends AuthServerResponse {

    private static final String JSON_FIELD_PROVIDER = "provider";
    private static final String JSON_FIELD_PROVIDER_ID = "provider_id";
    private static final String JSON_FIELD_USER = "user";
    private static final String JSON_FIELD_USER_ID = "id";
    private static final String JSON_FIELD_USER_IS_ADMIN = "isAdmin";

    private final String providerId;
    private final String provider;
    private final String userId;
    private final Boolean isAdmin;

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
        this.providerId = null;
        this.provider = null;
        this.userId = null;
        this.isAdmin = null;
    }

    private LookupUserIdResponse(String serverResponse) {
        ObjectServerError error;
        String provider;
        String providerId;
        String userId;
        Boolean isAdmin;
        String message;
        try {
            JSONObject obj = new JSONObject(serverResponse);
            provider = obj.getString(JSON_FIELD_PROVIDER);
            providerId = obj.getString(JSON_FIELD_PROVIDER_ID);
            JSONObject jsonUser = obj.getJSONObject(JSON_FIELD_USER);
            if (jsonUser != null) {
                userId = jsonUser.optString(JSON_FIELD_USER_ID, null);
                // can not use optBoolean since `null` is not permitted as default value
                // (we need it for the Boolean boxed type)
                isAdmin = jsonUser.has(JSON_FIELD_USER_IS_ADMIN) ? jsonUser.getBoolean(JSON_FIELD_USER_IS_ADMIN) : null;
                error = null;

                message = String.format(Locale.US, "Identity %s; Path %b", userId, isAdmin);

            } else {
                userId = null;
                isAdmin = null;
                error = null;
                message = "user = null";
            }

        } catch (JSONException e) {
            provider = null;
            providerId = null;
            userId = null;
            isAdmin = null;
            error = new ObjectServerError(ErrorCode.JSON_EXCEPTION, e);
            message = String.format(Locale.US, "Error %s", error.getErrorMessage());
        }

        RealmLog.debug("LookupUserIdResponse. " + message);
        setError(error);
        this.providerId = providerId;
        this.provider = provider;
        this.userId = userId;
        this.isAdmin = isAdmin;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getProvider() {
        return provider;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}
