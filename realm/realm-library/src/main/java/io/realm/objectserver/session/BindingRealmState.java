package io.realm.objectserver.session;

import io.realm.objectserver.credentials.Credentials;

/**
 * BINDING State. After bind() is called, this state will attempt to bind the local Realm to the remote. This is an
 * asynchronous operation, that must be able to be interrupted.
 */
public class BindingRealmState extends FsmState {

    @Override
    public void entry(Session session) {
        if (!session.isAuthenticated()) {
            // FIXME How to handle errors?
            session.bindWithTokens();
        } else {
            // Not access token available. We need to authenticate first.
            session.nextState(SessionState.AUTHENTICATING);
        }
    }

    @Override
    public void exit(Session session) {
        // TODO Abort any async stuff going on, possible in `session.bindWithTokens()`
    }

    @Override
    public void onBind(Session session) {
        session.nextState(SessionState.BINDING_REALM); // Will trigger a retry.
    }

    @Override
    public void onUnbind(Session session) {
        session.nextState(SessionState.UNBOUND);
    }

    @Override
    public void onSetCredentials(Session session, Credentials credentials) {
        session.replaceCredentials(credentials);
        session.nextState(SessionState.BINDING_REALM); // Retry with new credentials
    }
}
