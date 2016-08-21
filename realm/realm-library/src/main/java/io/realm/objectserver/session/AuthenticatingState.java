package io.realm.objectserver.session;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.realm.RealmAsyncTask;
import io.realm.internal.IOException;
import io.realm.internal.Util;
import io.realm.internal.objectserver.network.AuthenticateResponse;
import io.realm.internal.objectserver.network.AuthentificationServer;
import io.realm.internal.objectserver.network.NetworkStateReceiver;
import io.realm.objectserver.Credentials;
import io.realm.objectserver.SyncManager;

/**
 * AUTHENTICATING State. This step is needed if the user does not have proper access or credentials to access this
 * Realm. This can happen in 2 ways:
 *
 * <ol>
 *     <li>
 *          <b>Refresh token has expired:</b> This effectively means the user has been logged out and credentials has
 *          to be re-verified on the Authentication Server. Refreshing this token should happen automatically in the
 *          background, but could be delayed for a number of reasons.
 *     </li>
 *     <li>
 *          <b>Access token has expired:</b>
 *          The access token has expired. This state will refresh it
 *     </li>
 *     <li>
 *          <b>Access token does not exists:</b>
 *          This state will acquire an access token and attach it to the user.
 *     </li>
 * </ol>
 */
class AuthenticatingState extends FsmState {

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
        session.authenticate(new Runnable() {
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
