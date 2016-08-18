package io.realm.objectserver.session;

import io.realm.objectserver.Credentials;

/**
 * STARTED State. This is just an intermediate step that can be used to initialize the session properly.
 */
class BoundState extends FsmState {

    @Override
    public void onEnterState() {
        // Do nothing. If everything is setup correctly. We should now be synchronizing any changes
        // between the local and remote Realm.
    }

    @Override
    public void onExitState() {
        session.unbindActiveConnection();
    }

    @Override
    public void onUnbind() {
        gotoNextState(SessionState.UNBOUND);
    }

    @Override
    public void onStop() {
        gotoNextState(SessionState.STOPPED);
    }

    @Override
    public void onRefresh() {
        // TODO How to replace an access token on an active connection
        session.refreshAccessToken();
        gotoNextState(SessionState.STOPPED);
    }

    @Override
    public void onSetCredentials(Credentials credentials) {
        session.replaceCredentials(credentials);
        gotoNextState(SessionState.BINDING_REALM); // Retry binding immediately
    }
}
