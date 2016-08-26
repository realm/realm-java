package io.realm.objectserver.session;

import io.realm.objectserver.ErrorCode;

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

    @Override
    public void onError(ErrorCode errorCode, String errorMessage) {
        // Ignore all errors at this state. None of them would have any impact.
    }
}
