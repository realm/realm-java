package io.realm.internal.objectserver.network;

import io.realm.internal.objectserver.Error;
import io.realm.internal.objectserver.Token;

public class RefreshResponse {
    private Token refreshToken;
    private Error error;
    private String errorMessage;

    public boolean isValid() {
        return false;
    }

    public Token getRefreshToken() {
        return refreshToken;
    }

    public Error getError() {
        return error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
