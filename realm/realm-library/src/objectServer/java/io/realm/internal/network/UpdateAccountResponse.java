/*
 * Copyright 2018 Realm Inc.
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
import okhttp3.Response;

/**
 * This class represents the response from an {@link UpdateAccountRequest} network call.
 */
public class UpdateAccountResponse extends AuthServerResponse {

    public static UpdateAccountResponse from(Exception exception) {
        return new UpdateAccountResponse(new ObjectServerError(ErrorCode.fromException(exception), exception));
    }

    public static UpdateAccountResponse from(Response response) {
        if (response.isSuccessful()) {
            return new UpdateAccountResponse();
        } else {
            try {
                String serverResponse = response.body().string();
                return new UpdateAccountResponse(AuthServerResponse.createError(serverResponse, response.code()));
            } catch (IOException e) {
                ObjectServerError error = new ObjectServerError(ErrorCode.IO_EXCEPTION, e);
                return new UpdateAccountResponse(error);
            }
        }
    }

    /**
     * Create a failure response object.
     */
    public UpdateAccountResponse(ObjectServerError error) {
        this.error = error;
    }

    /**
     * Create a successful response object.
     */
    public UpdateAccountResponse() {
    }
}
