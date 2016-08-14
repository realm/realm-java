package io.realm.internal.objectserver.network;

public class AuthenticateResponse {

    public boolean isValid() {
        // 200
        // Valid Json
        // Valid AccessToken + RefreshToken

        // Otherwise not valid.
        return false;
    }
    // TODO Should cover both the error and success part
}
