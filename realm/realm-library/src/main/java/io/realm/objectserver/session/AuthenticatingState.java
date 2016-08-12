package io.realm.objectserver.session;

import io.realm.internal.objectserver.network.AuthentificationServer;
import io.realm.objectserver.credentials.Credentials;

/**
 * STARTED State. This is just an intermediate step that can be used to initialize the session properly.
 */
class AuthenticatingState extends FsmState {

    private AuthentificationServer authServer;

    @Override
    public void entry(Session session) {
        authServer = session.authServer;
    }

    @Override
    public void exit(Session session) {
//        authServer.abort(session.authCall);
    }

    @Override
    public void onStart(Session session) {
        super.onStart(session);
    }

    @Override
    public void onBind(Session session) {
        super.onBind(session);
    }

    @Override
    public void onUnbind(Session session) {
        super.onUnbind(session);
    }

    @Override
    public void onStop(Session session) {
        super.onStop(session);
    }

    @Override
    public void onRefresh(Session session) {
        super.onRefresh(session);
    }

    @Override
    public void onSetCredentials(Session session, Credentials credentials) {
        super.onSetCredentials(session, credentials);
    }
}
