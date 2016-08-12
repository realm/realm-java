package io.realm.objectserver.session;

/**
 * INITIAL State. Starting point for the Session Finite-State-Machine.
 */
class InitialState extends FsmState {

    @Override
    public void entry(Session session) {
        // Do nothing. We start here
    }

    @Override
    public void onStart(Session session) {
        session.nextState(SessionState.STARTED);
    }
}
