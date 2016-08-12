package io.realm.objectserver.session;

import io.realm.objectserver.credentials.Credentials;

/**
 * STOPPED State. This is the final state for a {@link Session}. After this, all actions will throw an
 * {@link IllegalStateException}.
 */
class StoppedState extends FsmState {

    public static final String SESSION_STOPPED_MSG = "Session has been stopped. Not further actions are possible.";

    @Override
    public void entry(Session session) {
        session.resetCredentials();
    }

    @Override
    public void onStart(Session session) {
        throw new IllegalStateException(SESSION_STOPPED_MSG);
    }

    @Override
    public void onBind(Session session) {
        // To harsh to to throw here as any SyncPolicy might not have been made aware
        // that the Session is stopped. Just ignore the call instead.
    }

    @Override
    public void onUnbind(Session session) {
        // To harsh to to throw here as any SyncPolicy might not have been made aware
        // that the Session is stopped. Just ignore the call instead.
    }

    @Override
    public void onStop(Session session) {
        throw new IllegalStateException(SESSION_STOPPED_MSG);
    }

    @Override
    public void onRefresh(Session session) {
        throw new IllegalStateException(SESSION_STOPPED_MSG);
    }

    @Override
    public void onSetCredentials(Session session, Credentials credentials) {
        throw new IllegalStateException(SESSION_STOPPED_MSG);
    }
}
