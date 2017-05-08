/*
 * Copyright 2017 Realm Inc.
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
 * Class wrapping the response from `/auth/password`
 */
public class ChangePasswordResponse extends AuthServerResponse {

    public static ChangePasswordResponse from(Response response) {
        if (response.isSuccessful()) {
            return new ChangePasswordResponse();
        }
        try {
            String serverResponse = response.body().string();
            return new ChangePasswordResponse(AuthServerResponse.createError(serverResponse, response.code()));
        } catch (IOException e) {
            ObjectServerError error = new ObjectServerError(ErrorCode.IO_EXCEPTION, e);
            return new ChangePasswordResponse(error);
        }
    }

    public static ChangePasswordResponse createFailure(ObjectServerError objectServerError) {
        return new ChangePasswordResponse(objectServerError);
    }

    private ChangePasswordResponse() {
        this.error = null;
    }

    private ChangePasswordResponse(ObjectServerError error) {
        this.error = error;
    }

}
