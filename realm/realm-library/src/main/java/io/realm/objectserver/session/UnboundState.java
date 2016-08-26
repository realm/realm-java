package io.realm.objectserver.session;

import io.realm.objectserver.Credentials;
import io.realm.objectserver.ErrorCode;

/**
 * UNBOUND State. This is the default state after a session has been started and no attempt at binding the local Realm
 * has been made.
 */
public class UnboundState extends FsmState {

    @Override
    public void onEnterState() {
        // Do nothing. Just wait for further user action.
        session.applySyncPolicy();
    }

    @Override
    protected void onExitState() {
        // Do nothing.
    }

    @Override
    public void onSetCredentials(Credentials credentials) {
        // Just replace current credentials and wait for further action.
        session.replaceCredentials(credentials);
    }

    @Override
    public void onBind() {
        gotoNextState(SessionState.BINDING_REALM);
    }

    @Override
    public void onError(ErrorCode errorCode, String errorMessage) {
        // Ignore all errors at this state. None of them would have any impact.
    }
}
