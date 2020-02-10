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

import java.io.IOException;
import java.util.Date;

import javax.annotation.Nonnull;

import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.internal.android.JsonUtils;
import io.realm.internal.permissions.PermissionOfferResponse;
import io.realm.log.RealmLog;
import io.realm.permissions.AccessLevel;
import okhttp3.Response;

/**
 * Class wrapping the response from `POST permissions/offers`
 */
public class MakePermissionsOfferResponse extends AuthServerResponse {

    private PermissionOfferResponse response;

    /**
     * Helper method for creating the proper lookup user response. This method will set the appropriate error
     * depending on any HTTP response codes or I/O errors.
     *
     * @param response the server response.
     * @return the user lookup response.
     */
    static MakePermissionsOfferResponse from(Response response) {
        String serverResponse;
        try {
            serverResponse = response.body().string();
        } catch (IOException e) {
            ObjectServerError error = new ObjectServerError(ErrorCode.IO_EXCEPTION, e);
            return new MakePermissionsOfferResponse(error);
        }
        if (!response.isSuccessful()) {
            return new MakePermissionsOfferResponse(AuthServerResponse.createError(serverResponse, response.code()));
        } else {
            return new MakePermissionsOfferResponse(serverResponse);
        }
    }

    /**
     * Helper method for creating a failed response.
     */
    public static MakePermissionsOfferResponse from(ObjectServerError objectServerError) {
        return new MakePermissionsOfferResponse(objectServerError);
    }

    /**
     * Helper method for creating a failed response from an {@link Exception}.
     */
    public static MakePermissionsOfferResponse from(Exception exception) {
        return MakePermissionsOfferResponse.from(new ObjectServerError(ErrorCode.fromException(exception), exception));
    }

    private MakePermissionsOfferResponse(ObjectServerError error) {
        RealmLog.debug("MakePermissionsOffer - Error: %s", error);
        setError(error);
        this.error = error;
    }

    private MakePermissionsOfferResponse(String serverResponse) {
        RealmLog.debug("MakePermissionsOffer - Success: %s", serverResponse);
        try {
            JSONObject obj = new JSONObject(serverResponse);
            @Nonnull String path = obj.getString("realmPath");
            Date expiresAt = obj.isNull("expiresAt") ? null : JsonUtils.stringToDate(obj.getString("expiresAt"));
            AccessLevel accessLevel = AccessLevel.fromKey(obj.getString("accessLevel"));
            Date createdAt = JsonUtils.stringToDate(obj.getString("createdAt"));
            String userId = obj.getString("userId");
            String token = obj.getString("token");
            response = new PermissionOfferResponse(path, expiresAt, accessLevel, createdAt, userId, token);
        } catch (JSONException e) {
            error = new ObjectServerError(ErrorCode.JSON_EXCEPTION, e);
        }
    }

    public String getToken() {
        return response.getToken();
    }
}
