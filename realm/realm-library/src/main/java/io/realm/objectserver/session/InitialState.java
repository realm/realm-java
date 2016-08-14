package io.realm.objectserver.session;

/**
 * INITIAL State. Starting point for the Session Finite-State-Machine.
 */
class InitialState extends FsmState {

    @Override
    public void onEnterState() {
        // Do nothing. We start here
    }

    @Override
    protected void onExitState() {
        // Do nothing.
    }

    @Override
    public void onStart() {
        gotoNextState(SessionState.STARTED);
    }
}
