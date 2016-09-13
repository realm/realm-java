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

package io.realm.objectserver.internal.network;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.realm.log.RealmLog;
import io.realm.objectserver.ErrorCode;
import io.realm.objectserver.internal.Token;
import io.realm.objectserver.ObjectServerError;
import okhttp3.Response;

/**
 * This class represents the response for a authenticate request.
 */
public class AuthenticateResponse {

    private final ObjectServerError error;
    private final Token accessToken;
    private final Token refreshToken;

    /**
     * Helper method for creating the proper Authenticate response. This method will set the appropriate error
     * depending on any HTTP response codes or IO errors.
     */
    public static AuthenticateResponse createFrom(Response response) {
        String serverResponse;
        try {
            serverResponse = response.body().string();
        } catch (IOException e) {
            ObjectServerError error = new ObjectServerError(ErrorCode.IO_EXCEPTION, e);
            return new AuthenticateResponse(error);
        }
        RealmLog.debug("Authenticate response: " + serverResponse);
        if (response.code() != 200) {
            try {
                JSONObject obj = new JSONObject(serverResponse);
                String type = obj.getString("type");
                String hint = obj.optString("hint", null);
                String title = obj.optString("title", null);
                ErrorCode errorCode = ErrorCode.fromInt(obj.optInt("code", -1));
                ObjectServerError error = new ObjectServerError(errorCode, title, hint, type);
                return new AuthenticateResponse(error);
            } catch (JSONException e) {
                ObjectServerError error = new ObjectServerError(ErrorCode.JSON_EXCEPTION, "Server failed with " +
                        response.code() + ", but could not parse error.", e);
                return new AuthenticateResponse(error);
            }
        } else {
            return new AuthenticateResponse(serverResponse);
        }
    }

    /**
     * Create a unsuccessful authentication response. This should only happen in case of network / IO problems.
     */
    public AuthenticateResponse(ObjectServerError error) {
        this.error = error;
        this.accessToken = null;
        this.refreshToken = null;
    }

    /**
     * Parse a valid (200) server response. It might still result in a unsuccessful authentication attempt, if the
     * JSON response could not be parsed correctly.
     */
    public AuthenticateResponse(String serverResponse) {
        ObjectServerError error;
        String identifier;
        String path;
        String appId;
        Token accessToken;
        Token refreshToken;
        try {
            JSONObject obj = new JSONObject(serverResponse);
            accessToken = obj.has("accessToken") ? Token.from(obj.getJSONObject("accessToken")) : null;
            refreshToken = obj.has("refreshToken") ? Token.from(obj.getJSONObject("refreshToken")) : null;
            error = null;
        } catch (JSONException ex) {
            accessToken = null;
            refreshToken = null;
            error = new ObjectServerError(ErrorCode.JSON_EXCEPTION, ex);
        }

        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.error = error;
    }

    public boolean isValid() {
        return (error == null);
    }

    public ObjectServerError getError() {
        return error;
    }

    public Token getAccessToken() {
        return accessToken;
    }

    public Token getRefreshToken() {
        return refreshToken;
    }
}
