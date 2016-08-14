package io.realm.objectserver.session;

import io.realm.objectserver.credentials.Credentials;

/**
 * Abstract class describing all states used in the Session Finite-State-Machine.
 */
abstract class FsmState implements FsmAction {

    volatile Session session; // This is non-null when this state is active.
    private boolean exiting;

    /**
     * Entry into the state. This method is also responsible for executing any asynchronous work
     * this state might run.
     *
     * This should only be called from {@link Session}.
     */
    public void entry(Session session) {
        this.session = session;
        this.exiting = false;
        onEnterState();
    }

    /**
     * Called just before leaving state. Once this method is called no more state changes can be triggered from
     * this state until {@link #entry(Session)} has been called again.
     *
     * This should only be called from {@link Session}.
     */
    public void exit() {
        exiting = true;
        onExitState();
    }

    public void gotoNextState(SessionState state) {
        if (!exiting) {
            session.nextState(state);
        }
    }

    protected abstract void onEnterState();
    protected abstract void onExitState();

    @Override
    public void onStart() {
        // Do nothing
    }

    @Override
    public void onBind() {
        // Do nothing
    }

    @Override
    public void onUnbind() {
        // Do nothing
    }

    @Override
    public void onStop() {
        // Do nothing
    }

    @Override
    public void onRefresh() {
        // Do nothing
    }

    @Override
    public void onSetCredentials(Credentials credentials) {
        // Do nothing
    }
}
