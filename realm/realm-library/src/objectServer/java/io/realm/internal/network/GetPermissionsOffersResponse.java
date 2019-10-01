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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.internal.android.JsonUtils;
import io.realm.log.RealmLog;
import io.realm.permissions.AccessLevel;
import io.realm.permissions.Permission;
import io.realm.permissions.PermissionOffer;
import okhttp3.Response;

/**
 * Class wrapping the response from `GET permissions/offers`
 */
public class GetPermissionsOffersResponse extends AuthServerResponse {

    private final List<PermissionOffer> offers = new ArrayList<>();

    /**
     * Helper method for creating the proper lookup user response. This method will set the appropriate error
     * depending on any HTTP response codes or I/O errors.
     *
     * @param response the server response.
     * @return the user lookup response.
     */
    static GetPermissionsOffersResponse from(Response response) {
        String serverResponse;
        try {
            serverResponse = response.body().string();
        } catch (IOException e) {
            ObjectServerError error = new ObjectServerError(ErrorCode.IO_EXCEPTION, e);
            return new GetPermissionsOffersResponse(error);
        }
        if (!response.isSuccessful()) {
            return new GetPermissionsOffersResponse(AuthServerResponse.createError(serverResponse, response.code()));
        } else {
            return new GetPermissionsOffersResponse(serverResponse);
        }
    }

    /**
     * Helper method for creating a failed response.
     */
    public static GetPermissionsOffersResponse from(ObjectServerError objectServerError) {
        return new GetPermissionsOffersResponse(objectServerError);
    }

    /**
     * Helper method for creating a failed response from an {@link Exception}.
     */
    public static GetPermissionsOffersResponse from(Exception exception) {
        return GetPermissionsOffersResponse.from(new ObjectServerError(ErrorCode.fromException(exception), exception));
    }

    private GetPermissionsOffersResponse(ObjectServerError error) {
        RealmLog.debug("GetPermissionOffers - Error: %s", error);
        setError(error);
        this.error = error;
    }

    private GetPermissionsOffersResponse(String serverResponse) {
        RealmLog.debug("GetPermissionOffers - Success: %s", serverResponse);
        try {
            JSONObject responseObject = new JSONObject(serverResponse);
            JSONArray responseOffersList = responseObject.getJSONArray("offers");
            for (int i = 0; i < responseOffersList.length(); i++) {
                JSONObject obj = responseOffersList.getJSONObject(i);
                String path = obj.getString("realmPath");
                Date expiresAt = obj.isNull("expiresAt") ? null : JsonUtils.stringToDate(obj.getString("expiresAt"));
                AccessLevel accessLevel = AccessLevel.fromKey(obj.getString("accessLevel"));
                Date createdAt = JsonUtils.stringToDate(obj.getString("createdAt"));
                String userId = obj.getString("userId");
                String token = obj.getString("token");
                offers.add(new PermissionOffer(path, accessLevel, expiresAt, createdAt, userId, token));
            }
        } catch (JSONException e) {
            error = new ObjectServerError(ErrorCode.JSON_EXCEPTION, e);
        }
    }

    public List<PermissionOffer> getOffers() {
        return offers;
    }
}
