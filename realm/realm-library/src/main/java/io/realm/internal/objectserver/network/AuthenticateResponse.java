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

package io.realm.internal.objectserver.network;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.realm.internal.Util;
import io.realm.internal.log.RealmLog;
import io.realm.objectserver.ErrorCode;
import io.realm.internal.objectserver.Token;
import okhttp3.Response;

public class AuthenticateResponse {

    private final ErrorCode errorCode;
    private final String errorMessage;

    private final String identifier;
    private final String path;
    private final String appId;
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
            return new AuthenticateResponse(ErrorCode.IO_ERROR, "Failed to read response." + Util.getStackTrace(e));
        }
        RealmLog.d("Authenticate response: " + serverResponse);
        if (response.code() != 200) {
            try {
                JSONObject obj = new JSONObject(serverResponse);
                String type = obj.getString("type");
                String hint = obj.getString("hint");
                ErrorCode errorCode = ErrorCode.fromAuthError(type);
                return new AuthenticateResponse(errorCode, hint);
            } catch (JSONException e) {
                return new AuthenticateResponse(ErrorCode.UNEXPECTED_JSON_FORMAT, "Server failed with " + response.code() +
                        ", but could not parse error." + Util.getStackTrace(e));
            }
        } else {
            return new AuthenticateResponse(serverResponse);
        }
    }

    /**
     * Create a unsuccessful authentication response. This should only happen in case of network / IO problems.
     */
    public AuthenticateResponse(ErrorCode reason, String errorMessage) {
        this.errorCode = reason;
        this.errorMessage = errorMessage;
        this.identifier = null;
        this.path = null;
        this.appId = null;
        this.accessToken = null;
        this.refreshToken = null;
    }

    /**
     * Parse a valid (200) server response. It might still result in a unsuccessful authentication attemp.
     */
    public AuthenticateResponse(String serverResponse) {
        ErrorCode errorCode;
        String errorMessage;
        String identifier;
        String path;
        String appId;
        Token accessToken;
        Token refreshToken;
        try {
            JSONObject obj = new JSONObject(serverResponse);
            identifier = obj.getString("identity");
            path = obj.optString("path");
            appId = obj.optString("app_id"); // FIXME No longer sent?
            accessToken = obj.has("token") ? Token.from(obj) : null;
            refreshToken = obj.has("refresh") ? Token.from(obj.getJSONObject("refresh")) : null;
            errorCode = null;
            errorMessage = null;
        } catch (JSONException ex) {
            identifier = null;
            path =  null;
            appId = null;
            accessToken = null;
            refreshToken = null;
            errorCode = ErrorCode.BAD_SYNTAX;
            errorMessage = "Unexpected JSON: " + Util.getStackTrace(ex);
        }
        this.identifier = identifier;
        this.path = path;
        this.appId = appId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public boolean isValid() {
        return (errorCode == null);
    }
    public String getErrorMessage() {
        return errorMessage;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getPath() {
        return path;
    }

    public String getAppId() {
        return appId;
    }

    public Token getAccessToken() {
        return accessToken;
    }

    public Token getRefreshToken() {
        return refreshToken;
    }
}
