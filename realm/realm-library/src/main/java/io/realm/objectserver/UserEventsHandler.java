package io.realm.objectserver;

import io.realm.objectserver.credentials.ObjectServerCredentials;

public interface UserEventsHandler {
    void credetinalsRequired(User user) {
        // Set off login flow
        ObjectServerCredentials creds = new FacebookCredentials(token);
        user.addCredentials();
    }
}
