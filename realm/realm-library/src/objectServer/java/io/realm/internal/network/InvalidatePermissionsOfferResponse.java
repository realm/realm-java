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
import java.util.ArrayList;
import java.util.List;

import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.log.RealmLog;
import io.realm.permissions.Permission;
import okhttp3.Response;

/**
 * Class wrapping the response from `DELETE /permissions/offers/:token:`
 */
public class InvalidatePermissionsOfferResponse extends AuthServerResponse {

    /**
     * Helper method for creating the proper response. This method will set the appropriate error
     * depending on any HTTP response codes or I/O errors.
     *
     * @param response the server response.
     * @return the user lookup response.
     */
    static InvalidatePermissionsOfferResponse from(Response response) {
        String serverResponse;
        try {
            serverResponse = response.body().string();
        } catch (IOException e) {
            ObjectServerError error = new ObjectServerError(ErrorCode.IO_EXCEPTION, e);
            return new InvalidatePermissionsOfferResponse(error);
        }
        if (!response.isSuccessful()) {
            return new InvalidatePermissionsOfferResponse(AuthServerResponse.createError(serverResponse, response.code()));
        } else {
            return new InvalidatePermissionsOfferResponse(serverResponse);
        }
    }

    /**
     * Helper method for creating a failed response.
     */
    public static InvalidatePermissionsOfferResponse from(ObjectServerError objectServerError) {
        return new InvalidatePermissionsOfferResponse(objectServerError);
    }

    /**
     * Helper method for creating a failed response from an {@link Exception}.
     */
    public static InvalidatePermissionsOfferResponse from(Exception exception) {
        return InvalidatePermissionsOfferResponse.from(new ObjectServerError(ErrorCode.fromException(exception), exception));
    }

    private InvalidatePermissionsOfferResponse(ObjectServerError error) {
        RealmLog.debug("InvalidatePermissionOffer - Error: %s", error);
        setError(error);
        this.error = error;
    }

    private InvalidatePermissionsOfferResponse(String serverResponse) {
        RealmLog.debug("InvalidatePermissionOffer - Success: %s", serverResponse);
        // No data to store
    }
}
