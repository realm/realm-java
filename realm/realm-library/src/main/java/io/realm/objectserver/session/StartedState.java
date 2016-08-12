package io.realm.objectserver.session;

/**
 * STARTED State. This is just an intermediate step that can be used to initialize the session properly.
 */
class StartedState extends FsmState {

    @Override
    public void entry(Session session) {
        session.initialize();
        // This is just an intermediate step, so goto next straight away.
        session.nextState(SessionState.UNBOUND);
    }
}
