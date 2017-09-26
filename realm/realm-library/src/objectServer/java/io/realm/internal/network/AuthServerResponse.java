package io.realm.internal.network;
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

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.ErrorCode;
import io.realm.ObjectServerError;

/**
 * Base class for all response types from the Realm Authentication Server.
 */
public abstract class AuthServerResponse {

    protected ObjectServerError error;

    /**
     * Checks if this response was valid.
     *
     * @return {@code true} if valid, {@code false} otherwise.
     */
    public boolean isValid() {
        return (error == null);
    }

    /**
     * If {@link #isValid()} returns {@code false}, this method will return the error causing this.
     *
     * @return the error.
     */
    public ObjectServerError getError() {
        return error;
    }

    protected void setError(ObjectServerError error) {
        this.error = error;
    }

    /**
     * Parse an HTTP error from a Realm Authentication Server. The server returns errors following
     * https://tools.ietf.org/html/rfc7807 with an extra "code" field for Realm specific error codes.
     *
     * @param response the server response.
     * @param httpErrorCode the HTTP error code.
     * @return an server error.
     */
    public static ObjectServerError createError(String response, int httpErrorCode) {
        try {
            JSONObject obj = new JSONObject(response);
            String title = obj.optString("title", null);
            String hint = obj.optString("hint", null);
            ErrorCode errorCode;
            if (obj.has("code")) {
                errorCode = ErrorCode.fromInt(obj.getInt("code"));
            } else if (obj.has("status")) {
                errorCode = ErrorCode.fromInt(obj.getInt("status"));
            } else {
                errorCode = ErrorCode.UNKNOWN;
            }
            return new ObjectServerError(errorCode, title, hint);
        } catch (JSONException e) {
            return new ObjectServerError(ErrorCode.JSON_EXCEPTION, "Server failed with " +
                    httpErrorCode + ", but could not parse error.", e);
        }
    }
}
