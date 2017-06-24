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
import okhttp3.Response;

/**
 * Class wrapping the response from `GET /api/providers/:provider/accounts/:provider_id`
 */
public class LookupUserIdResponse extends AuthServerResponse {

    /**
     * Helper method for creating the proper change password response. This method will set the appropriate error
     * depending on any HTTP response codes or I/O errors.
     *
     * @param response the server response.
     * @return the change password response.
     */
    static LookupUserIdResponse from(Response response) {
        try {
            System.out.println(">>>>>>>>>>>>>>>>>> serverResponse: " + response.body().string() + " response.code: " + response.code());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response.isSuccessful()) {
            return new LookupUserIdResponse();
        }
        try {
            String serverResponse = response.body().string();

            return new LookupUserIdResponse(AuthServerResponse.createError(serverResponse, response.code()));
        } catch (IOException e) {
            ObjectServerError error = new ObjectServerError(ErrorCode.IO_EXCEPTION, e);
            return new LookupUserIdResponse(error);
        }
    }

    /**
     * Helper method for creating a failed response.
     */
    public static LookupUserIdResponse from(ObjectServerError objectServerError) {
        return new LookupUserIdResponse(objectServerError);
    }

    /**
     * Helper method for creating a failed response from an {@link Exception}.
     */
    public static LookupUserIdResponse from(Exception exception) {
        return LookupUserIdResponse.from(new ObjectServerError(ErrorCode.fromException(exception), exception));
    }

    private LookupUserIdResponse() {
        this.error = null;
    }

    private LookupUserIdResponse(ObjectServerError error) {
        this.error = error;
    }

}
