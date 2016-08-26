package io.realm.objectserver.session;

import io.realm.objectserver.Credentials;
import io.realm.objectserver.ErrorCode;

/**
 * STARTED State. This is just an intermediate step that can be used to initialize the session properly.
 */
class BoundState extends FsmState {

    @Override
    public void onEnterState() {
        // Do nothing. If everything is setup correctly. We should now be synchronizing any changes
        // between the local and remote Realm.
    }

    @Override
    public void onExitState() {
        session.unbindActiveConnection();
    }

    @Override
    public void onUnbind() {
        gotoNextState(SessionState.UNBOUND);
    }

    @Override
    public void onStop() {
        gotoNextState(SessionState.STOPPED);
    }

    @Override
    public void onRefresh() {
        // TODO How to replace an access token on an active connection
        gotoNextState(SessionState.STOPPED); // TODO: Stop? Really?
    }

    @Override
    public void onSetCredentials(Credentials credentials) {
        session.replaceCredentials(credentials);
        gotoNextState(SessionState.BINDING_REALM); // Retry binding immediately
    }

    @Override
    public void onError(ErrorCode errorCode, String errorMessage) {
        switch(errorCode) {
            // Auth protocol errors (should not happen). If credentials are being replaced
            case IO_ERROR:
            case UNEXPECTED_JSON_FORMAT:
            case REALM_PROBLEM:
            case INVALID_PARAMETERS:
            case MISSING_PARAMETERS:
            case INVALID_CREDENTIALS:
            case UNKNOWN_ACCOUNT:
            case EXISTING_ACCOUNT:
            case ACCESS_DENIED:
            case INVALID_REFRESH_TOKEN:
            case EXPIRED_REFRESH_TOKEN:
            case INTERNAL_SERVER_ERROR:
                throw new IllegalStateException("Authentication protocol errors should not happen: " + errorCode.toString());

            // Ignore Network client errors (irrelevant)
            case CONNECTION_CLOSED:
            case OTHER_ERROR:
            case UNKNOWN_MESSAGE:
            case BAD_SYNTAX:
            case LIMITS_EXCEEDED:
            case WRONG_PROTOCOL_VERSION:
            case BAD_SESSION_IDENT:
            case REUSE_OF_SESSION_IDENT:
            case BOUND_IN_OTHER_SESSION:
            case BAD_MESSAGE_ORDER:
                return;

            // Session errors:
            // FIXME: Which of these are just INFO and which can we actually do something about? Right now treat all as fatal
            case SESSION_CLOSED:
            case OTHER_SESSION_ERROR:
                gotoNextState(SessionState.STOPPED);

            case TOKEN_EXPIRED:
                // Only known case we can actually work around.
                // Trigger a rebind which will cause access token to be refreshed.
                gotoNextState(SessionState.BINDING_REALM);

            case BAD_AUTHENTICATION:
            case ILLEGAL_REALM_PATH:
            case NO_SUCH_PATH:
            case PERMISSION_DENIED:
            case BAD_SERVER_FILE_IDENT:
            case BAD_CLIENT_FILE_IDENT:
            case BAD_SERVER_VERSION:
            case BAD_CLIENT_VERSION:
            case DIVERGING_HISTORIES:
            case BAD_CHANGESET:
                gotoNextState(SessionState.STOPPED);
                break;
        }
    }
}
