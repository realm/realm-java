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
