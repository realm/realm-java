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

import java.io.IOException;

import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.log.RealmLog;
import okhttp3.Response;

/**
 * This class represents the response for a log out request.
 */
public class LogoutResponse extends AuthServerResponse {

    /**
     * Helper method for creating the proper Logout response. This method will set the appropriate error
     * depending on any HTTP response codes or I/O errors.
     *
     * @param response the server response.
     * @return the log out response.
     */
    static LogoutResponse from(Response response) {
        if (response.isSuccessful()) {
            // success
            return new LogoutResponse();
        }
        try {
            String serverResponse = response.body().string();
            return new LogoutResponse(AuthServerResponse.createError(serverResponse, response.code()));
        } catch (IOException e) {
            ObjectServerError error = new ObjectServerError(ErrorCode.IO_EXCEPTION, e);
            return new LogoutResponse(error);
        }
    }

    /**
     * Helper method for creating a failed response.
     */
    public static LogoutResponse from(ObjectServerError error) {
        return new LogoutResponse(error);
    }

    /**
     * Helper method for creating a failed response from an {@link Exception}.
     */
    public static LogoutResponse from(Exception exception) {
        return LogoutResponse.from(new ObjectServerError(ErrorCode.fromException(exception), exception));
    }

    /**
     * Creates an unsuccessful authentication response. This should only happen in case of network or I/O
     * related issues.
     *
     * @param error an authentication response error.
     */
    private LogoutResponse(ObjectServerError error) {
        RealmLog.debug("Logout response - Error: " + error.getErrorMessage());
        setError(error);
    }

    /**
     * Parses a valid (204) server response.
     */
    private LogoutResponse() {
        RealmLog.debug("Logout response - Success");
        setError(null);
    }

    /**
     * Checks if response was valid.
     *
     * @return {@code true} if valid.
     */
    @Override
    public boolean isValid() {
        return (error == null) || (error.getErrorCode() == ErrorCode.EXPIRED_REFRESH_TOKEN);
    }
}
