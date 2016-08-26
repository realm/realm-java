package io.realm.internal.objectserver.network;

import io.realm.objectserver.ErrorCode;
import io.realm.internal.objectserver.Token;

public class RefreshResponse {
    private Token refreshToken;
    private ErrorCode errorCode;
    private String errorMessage;

    public boolean isValid() {
        return false;
    }

    public Token getRefreshToken() {
        return refreshToken;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
