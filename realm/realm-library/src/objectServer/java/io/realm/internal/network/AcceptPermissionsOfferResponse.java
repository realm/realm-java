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

import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.log.RealmLog;
import okhttp3.Response;

/**
 * Class wrapping the response from `POST permissions/offers/:token:/accept`
 */
public class AcceptPermissionsOfferResponse extends AuthServerResponse {

    private String path;

    /**
     * Helper method for creating the proper lookup user response. This method will set the appropriate error
     * depending on any HTTP response codes or I/O errors.
     *
     * @param response the server response.
     * @return the user lookup response.
     */
    static AcceptPermissionsOfferResponse from(Response response) {
        String serverResponse;
        try {
            serverResponse = response.body().string();
        } catch (IOException e) {
            ObjectServerError error = new ObjectServerError(ErrorCode.IO_EXCEPTION, e);
            return new AcceptPermissionsOfferResponse(error);
        }
        if (!response.isSuccessful()) {
            return new AcceptPermissionsOfferResponse(AuthServerResponse.createError(serverResponse, response.code()));
        } else {
            return new AcceptPermissionsOfferResponse(serverResponse);
        }
    }

    /**
     * Helper method for creating a failed response.
     */
    public static AcceptPermissionsOfferResponse from(ObjectServerError objectServerError) {
        return new AcceptPermissionsOfferResponse(objectServerError);
    }

    /**
     * Helper method for creating a failed response from an {@link Exception}.
     */
    public static AcceptPermissionsOfferResponse from(Exception exception) {
        return AcceptPermissionsOfferResponse.from(new ObjectServerError(ErrorCode.fromException(exception), exception));
    }

    private AcceptPermissionsOfferResponse(ObjectServerError error) {
        RealmLog.debug("AcceptPermissionsOffer - Error: %s", error);
        setError(error);
        this.error = error;
    }

    private AcceptPermissionsOfferResponse(String serverResponse) {
        RealmLog.debug("AcceptPermissionsOffer - Success: %s", serverResponse);
        try {
            JSONObject obj = new JSONObject(serverResponse);
            path = obj.getString("path");
        } catch (JSONException e) {
            error = new ObjectServerError(ErrorCode.JSON_EXCEPTION, e);
        }
    }

    public String getPath() {
        return path;
    }
}
