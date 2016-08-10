package io.realm.objectserver.credentials;

import io.realm.objectserver.User;

public interface ObjectServerCredentialsHandler {
        void setCredentials(User user);
    }
