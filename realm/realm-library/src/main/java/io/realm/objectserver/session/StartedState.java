package io.realm.objectserver.session;

import io.realm.objectserver.Error;

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

    @Override
    public void onError(Error error, String errorMessage) {
        // Ignore all errors at this state. None of them would have any impact.
    }
}
