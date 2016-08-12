package io.realm.objectserver.session;

import io.realm.objectserver.credentials.Credentials;

/**
 * STARTED State. This is just an intermediate step that can be used to initialize the session properly.
 */
class BoundState extends FsmState {
    @Override
    public void entry(Session session) {
        // Do nothing. If everything is setup correctly. We should now be synchronizing any changes
        // between the local and remote Realm.
    }

    @Override
    public void exit(Session session) {
        session.unbindActiveConnection();
    }

    @Override
    public void onUnbind(Session session) {
        session.nextState(SessionState.UNBOUND);
    }

    @Override
    public void onStop(Session session) {
        session.nextState(SessionState.STOPPED);
    }

    @Override
    public void onRefresh(Session session) {
        // TODO How to replace an access token on an active connection
        session.refreshAccessToken();
        session.nextState(SessionState.STOPPED);
    }

    @Override
    public void onSetCredentials(Session session, Credentials credentials) {
        session.replaceCredentials(credentials);
        session.nextState(SessionState.BINDING_REALM); // Retry binding immediately
    }
}
