package io.realm.objectserver.session;

import io.realm.objectserver.credentials.Credentials;

/**
 * Abstract class describing all states used in the Session Finite-State-Machine.
 */
abstract class FsmState implements FsmAction {

    /**
     * Entry into the state. This method is also responsible for executing any asynchronous work
     * this state might run.
     */
    public abstract void entry(Session session);

    /**
     * Called just before leaving state.
     */
    public void exit(Session session) {
        // Do nothing
    }

    @Override
    public void onStart(Session session) {
        // Do nothing
    }

    @Override
    public void onBind(Session session) {
        // Do nothing
    }

    @Override
    public void onUnbind(Session session) {
        // Do nothing
    }

    @Override
    public void onStop(Session session) {
        // Do nothing
    }

    @Override
    public void onRefresh(Session session) {
        // Do nothing
    }

    @Override
    public void onSetCredentials(Session session, Credentials credentials) {
        // Do nothing
    }
}
