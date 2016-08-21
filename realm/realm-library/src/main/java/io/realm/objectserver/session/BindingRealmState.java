package io.realm.objectserver.session;

import io.realm.objectserver.Credentials;

/**
 * BINDING State. After bind() is called, this state will attempt to bind the local Realm to the remote. This is an
 * asynchronous operation, that must be able to be interrupted.
 */
public class BindingRealmState extends FsmState {

    @Override
    public void onEnterState() {
        if (session.isAuthenticated(session.configuration)) {
            // FIXME How to handle errors?
            session.bindWithTokens();
        } else {
            // Not access token available. We need to authenticateUser first.
            gotoNextState(SessionState.AUTHENTICATING);
        }
    }

    @Override
    public void onExitState() {
        // TODO Abort any async stuff going on, possible in `session.bindWithTokens()`
    }

    @Override
    public void onBind() {
        gotoNextState(SessionState.BINDING_REALM); // Will trigger a retry.
    }

    @Override
    public void onUnbind() {
        gotoNextState(SessionState.UNBOUND);
    }

    @Override
    public void onSetCredentials(Credentials credentials) {
        session.replaceCredentials(credentials);
        gotoNextState(SessionState.BINDING_REALM); // Retry with new credentials
    }
}
