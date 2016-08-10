package io.realm.objectserver.credentials;

import java.net.URL;

public class UsernameAndPassword extends ObjectServerCredentials {


    public UsernameAndPassword(URL authentificationUrl, boolean createUser) {
        super(authentificationUrl, createUser);
    }

    @Override
    public LoginType getLoginType() {
        return LoginType.USERNAME_PASSWORD;
    }

    @Override
    public String getToken() {
        return null;
    }


}
