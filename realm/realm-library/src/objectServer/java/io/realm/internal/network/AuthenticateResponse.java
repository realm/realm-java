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

package io.realm.internal.network;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.internal.objectserver.Token;
import io.realm.log.RealmLog;
import okhttp3.Response;

/**
 * This class represents the response for an authenticate request.
 */
public class AuthenticateResponse extends AuthServerResponse {

    private static final String JSON_FIELD_ACCESS_TOKEN = "access_token";
    private static final String JSON_FIELD_REFRESH_TOKEN = "refresh_token";

    private final Token accessToken;
    private final Token refreshToken;

    /**
     * Helper method for creating the proper Authenticate response. This method will set the appropriate error
     * depending on any HTTP response codes or IO errors.
     *
     * @param response the HTTP response.
     * @return an authenticate response.
     */
    public static AuthenticateResponse from(Response response) {
        String serverResponse;
        try {
            serverResponse = response.body().string();
        } catch (IOException e) {
            ObjectServerError error = new ObjectServerError(ErrorCode.IO_EXCEPTION, e);
            return new AuthenticateResponse(error);
        }
        if (!response.isSuccessful()) {
            return new AuthenticateResponse(AuthServerResponse.createError(serverResponse, response.code()));
        } else {
            return new AuthenticateResponse(serverResponse);
        }
    }

    /**
     * Helper method for creating the response from a JSON string.
     */
    public static AuthenticateResponse from(String json) {
        return new AuthenticateResponse(json);
    }

    /**
     * Helper method for creating a failed response.
     */
    public static AuthenticateResponse from(ObjectServerError error) {
        return new AuthenticateResponse(error);
    }

    /**
     * Helper method for creating a failed response from an {@link Exception}.
     */
    public static AuthenticateResponse from(Exception exception) {
        return AuthenticateResponse.from(new ObjectServerError(ErrorCode.fromException(exception), exception));
    }

    /**
     * Helper method for creating a valid user login response. The user returned will be assumed to have all permissions
     * and doesn't expire.
     *
     * @param identifier user identifier.
     * @param refreshToken user's refresh token.
     */
    public static AuthenticateResponse createValidResponseWithUser(String identifier, String refreshToken, boolean isAdmin) {
        try {
            JSONObject response = new JSONObject();
            response.put(JSON_FIELD_REFRESH_TOKEN, new Token(refreshToken, identifier, null, Long.MAX_VALUE, Token.Permission.ALL, isAdmin).toJson());
            return new AuthenticateResponse(response.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates an unsuccessful authentication response. This should only happen in case of network or I/O related
     * issues.
     *
     * @param error the network or I/O error.
     */
    private AuthenticateResponse(ObjectServerError error) {
        RealmLog.debug("AuthenticateResponse - Error: " + error);
        setError(error);
        this.accessToken = null;
        this.refreshToken = null;
    }

    /**
     * Parses a valid (200) server response. It might still result in an unsuccessful authentication attempt, if the
     * JSON response could not be parsed correctly.
     *
     * @param serverResponse the server response.
     */
    private AuthenticateResponse(String serverResponse) {
        ObjectServerError error;
        Token accessToken;
        Token refreshToken;
        String message;
        try {
            JSONObject obj = new JSONObject(serverResponse);
            accessToken = obj.has(JSON_FIELD_ACCESS_TOKEN) ?
                    Token.from(obj.getJSONObject(JSON_FIELD_ACCESS_TOKEN)) : null;
            refreshToken = obj.has(JSON_FIELD_REFRESH_TOKEN) ?
                    Token.from(obj.getJSONObject(JSON_FIELD_REFRESH_TOKEN)) : null;
            error = null;
            if (accessToken == null) {
                message = "accessToken = null";
            } else {
                message = String.format(Locale.US, "Identity %s; Path %s", accessToken.identity(), accessToken.path());
            }
        } catch (JSONException ex) {
            accessToken = null;
            refreshToken = null;
            //noinspection ThrowableInstanceNeverThrown
            error = new ObjectServerError(ErrorCode.JSON_EXCEPTION, ex);
            message = String.format(Locale.US, "Error %s", error.getErrorMessage());
        }
        RealmLog.debug("AuthenticateResponse. " + message);
        setError(error);
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public Token getAccessToken() {
        return accessToken;
    }

    public Token getRefreshToken() {
        return refreshToken;
    }

}
