/*
 * Copyright 2016 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.objectserver.session;

import io.realm.internal.objectserver.network.NetworkStateReceiver;
import io.realm.objectserver.Credentials;

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
        session.authenticateRealm(new Runnable() {
            @Override
            public void run() {
                gotoNextState(SessionState.BINDING_REALM);
            }
        }, new Runnable() {
            @Override
            public void run() {
                gotoNextState(SessionState.STOPPED);
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
