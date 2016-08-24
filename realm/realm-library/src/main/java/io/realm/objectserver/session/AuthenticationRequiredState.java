package io.realm.objectserver.session;

import io.realm.internal.objectserver.network.NetworkStateReceiver;
import io.realm.objectserver.Credentials;

/**
 * AUTHENTICATING_REQUIRED State. This step is entered if authentication fails for some reason. This means that an
 * error has been reported to the user but no new access token has been provided. The session is halted until a new
 * valid access token is provided.
 */
class AuthenticationRequiredState extends FsmState {

    @Override
    public void onEnterState() {
        if (NetworkStateReceiver.isOnline(session.configuration.getContext())) {
            authenticate(session);
        } else {
            // Wait for connection to become available, before trying again.
            // This might potentially block for the lifetime of the application,
            // which is fine.
            session.networkListener = new NetworkStateReceiver.ConnectionListener() {
                @Override
                public void onChange(boolean connectionAvailable) {
                    if (connectionAvailable) {
                        authenticate(session);
                        NetworkStateReceiver.removeListener(this);
                    }
                }
            };
            NetworkStateReceiver.addListener(session.networkListener);
        }
    }

    @Override
    public void onExitState() {
        // Abort any current network request.
        if (session.networkRequest != null) {
            session.networkRequest.cancel();
            session.networkRequest = null;
        }

        // Release listener if we were waiting for network to become available.
        if (session.networkListener != null) {
            NetworkStateReceiver.removeListener(session.networkListener);
            session.networkListener = null;
        }
    }

    private synchronized void authenticate(final Session session) {
        session.authenticateRealm(new Runnable() {
            @Override
            public void run() {
                gotoNextState(SessionState.BOUND);
            }
        }, new Runnable() {
            @Override
            public void run() {
                gotoNextState(SessionState.UNBOUND);
            }
        });
    }

    @Override
    public void onBind() {
        gotoNextState(SessionState.BINDING_REALM); // Equivalent to forcing a retry
    }

    @Override
    public void onUnbind() {
        gotoNextState(SessionState.UNBOUND); // Treat this as user wanting to exit a binding in progress.
    }

    @Override
    public void onStop() {
        gotoNextState(SessionState.STOPPED);
    }

    @Override
    public void onSetCredentials(Credentials credentials) {
        session.replaceCredentials(credentials);
        gotoNextState(SessionState.BINDING_REALM);
    }
}
