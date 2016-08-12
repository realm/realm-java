package io.realm.objectserver.credentials;

import io.realm.objectserver.session.Session;

public interface CredentialsHandler {
    void getCredentials(Session session);
}
