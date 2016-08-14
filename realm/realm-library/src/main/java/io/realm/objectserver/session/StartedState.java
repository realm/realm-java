package io.realm.objectserver.session;

/**
 * STARTED State. This is just an intermediate step that can be used to initialize the session properly.
 */
class StartedState extends FsmState {

    @Override
    public void onEnterState() {
        session.initialize();
        // This is just an intermediate step, so goto next straight away.
        gotoNextState(SessionState.UNBOUND);
    }

    @Override
    protected void onExitState() {
        // Do nothing
    }
}
