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

import java.io.IOException;

import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.log.RealmLog;
import okhttp3.Response;

/**
 * Class wrapping the response from `POST permissions/apply`
 */
public class ApplyPermissionsResponse extends AuthServerResponse {

    /**
     * Helper method for creating the proper lookup user response. This method will set the appropriate error
     * depending on any HTTP response codes or I/O errors.
     *
     * @param response the server response.
     * @return the user lookup response.
     */
    static ApplyPermissionsResponse from(Response response) {
        String serverResponse;
        try {
            serverResponse = response.body().string();
        } catch (IOException e) {
            ObjectServerError error = new ObjectServerError(ErrorCode.IO_EXCEPTION, e);
            return new ApplyPermissionsResponse(error);
        }
        if (!response.isSuccessful()) {
            return new ApplyPermissionsResponse(AuthServerResponse.createError(serverResponse, response.code()));
        } else {
            return new ApplyPermissionsResponse(serverResponse);
        }
    }

    /**
     * Helper method for creating a failed response.
     */
    public static ApplyPermissionsResponse from(ObjectServerError objectServerError) {
        return new ApplyPermissionsResponse(objectServerError);
    }

    /**
     * Helper method for creating a failed response from an {@link Exception}.
     */
    public static ApplyPermissionsResponse from(Exception exception) {
        return ApplyPermissionsResponse.from(new ObjectServerError(ErrorCode.fromException(exception), exception));
    }

    private ApplyPermissionsResponse(ObjectServerError error) {
        RealmLog.debug("ApplyPermissions - Error: %s", error);
        setError(error);
        this.error = error;
    }

    private ApplyPermissionsResponse(String serverResponse) {
        RealmLog.debug("ApplyPermissions - Success: %s", serverResponse);
    }
}
