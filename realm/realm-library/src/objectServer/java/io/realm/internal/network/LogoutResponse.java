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

    private final ObjectServerError error;

    /**
     * Helper method for creating the proper Authenticate response. This method will set the appropriate error
     * depending on any HTTP response codes or I/O errors.
     *
     * @param response the server response.
     * @return the log out response.
     */
    static LogoutResponse createFrom(Response response) {
        String serverResponse;
        try {
            serverResponse = response.body().string();
        } catch (IOException e) {
            ObjectServerError error = new ObjectServerError(ErrorCode.IO_EXCEPTION, e);
            return new LogoutResponse(error);
        }
        RealmLog.debug("Authenticate response: " + serverResponse);
        if (response.code() != 200) {
            return new LogoutResponse(AuthServerResponse.createError(serverResponse, response.code()));
        } else {
            return new LogoutResponse(serverResponse);
        }
    }

    /**
     * Creates an unsuccessful authentication response. This should only happen in case of network or I/O
     * related issues.
     *
     * @param error an authentication response error.
     */
    private LogoutResponse(ObjectServerError error) {
        this.error = error;
    }

    /**
     * Parses a valid (200) server response.
     *
     * @param serverResponse the server response.
     */
    private LogoutResponse(String serverResponse) {
        this.error = null;
        // TODO endpoint not finalized
    }

    /**
     * Checks if response was valid.
     *
     * @return {@code true} if valid.
     */
    public boolean isValid() {
//        return (error == null);
        return true;
    }

    /**
     * Returns the error.
     *
     * @return the error.
     */
    public ObjectServerError getError() {
        return error;
    }
}
