package io.realm.objectserver.session;

import java.util.concurrent.TimeUnit;

import io.realm.BaseRealm;
import io.realm.RealmAsyncTask;
import io.realm.internal.IOException;
import io.realm.internal.objectserver.network.AuthenticateResponse;
import io.realm.internal.objectserver.network.AuthentificationServer;
import io.realm.internal.objectserver.network.NetworkStateReceiver;
import io.realm.objectserver.credentials.Credentials;

/**
 * STARTED State. This is just an intermediate step that can be used to initialize the session properly.
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

        // Authenticate in a background thread. This allows incremental backoff and retries in a safe manner.
        // TODO: This is a potentially very long-lived thread. Should we use a seperate thread pool?
        final RealmAsyncTask networkTask = new RealmAsyncTask(BaseRealm.ASYNC_TASK_EXECUTOR.submitNetworkRequest(new Runnable() {
            @Override
            public void run() {
                // FIXME Align how many credentials are supported. Just assume 1 for now.
                Credentials credentials = session.configuration.getCredentials().get(0);
                AuthentificationServer authServer = session.authServer;
                int attempt = 0;
                boolean success = false;

                while (true) {
                    attempt++;
                    long sleep = calculateExponentialDelay(attempt - 1, TimeUnit.MINUTES.toMillis(5));
                    if (sleep > 0) {
                        try {
                            Thread.sleep(sleep);
                        } catch (InterruptedException e) {
                            return; // Abort authentication if interrupted.
                        }
                    }

                    try {
                        AuthenticateResponse response = authServer.authenticate(credentials);
                        if (response.isValid()) {
                            // TODO Save tokens
                            success = true;
                        } else {
                            // TODO Report bad credentials. How?
                            break;
                        }

                    } catch (IOException e) {
                        // Timeouts, bad network connection etc. Just schedule retry without logging anything
                        // TODO Should we log something?
                    }
                }

                if (success) {
                    gotoNextState(SessionState.BOUND);
                } else {
                    gotoNextState(SessionState.UNBOUND);
                }
            }
        }));
        session.networkRequest = networkTask;
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

    public static long calculateExponentialDelay(int failedAttempts, long maxDelayInMs) {
        // https://en.wikipedia.org/wiki/Exponential_backoff
        //Attempt 1     0s     0s
        //Attempt 2     2s     2s
        //Attempt 3     4s     4s
        //Attempt 4     8s     8s
        //Attempt 5     16s    16s
        //Attempt 6     32s    32s
        //Attempt 7     64s    1m 4s
        //Attempt 8     128s   2m 8s
        //Attempt 9     256s   4m 16s
        //Attempt 10    512    8m 32s
        //Attempt 11    1024   17m 4s
        //Attempt 12    2048   34m 8s
        //Attempt 13    4096   1h 8m 16s
        //Attempt 14    8192   2h 16m 32s
        //Attempt 15    16384  4h 33m 4s
        double SCALE = 1.0D; // Scale the exponential backoff
        double delayInMs = ((Math.pow(2.0D, failedAttempts) - 1d) / 2.0D) * 1000 * SCALE;

        // Just use maximum back-off value. We are not afraid of many threads using this value
        // to trigger at once.
        return maxDelayInMs < delayInMs ? maxDelayInMs : (long) delayInMs;
    }
}
