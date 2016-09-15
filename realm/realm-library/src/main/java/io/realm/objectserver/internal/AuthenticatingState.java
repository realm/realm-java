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

package io.realm.objectserver.internal;

import io.realm.objectserver.*;
import io.realm.objectserver.internal.network.NetworkStateReceiver;

/**
 * AUTHENTICATING State. This step is needed if the user does not have proper access or credentials to access this
 * Realm when attempting to bind it. This can happen in 3 ways:
 *
 * <ol>
 *     <li>
 *          <b>Refresh token has expired:</b>
 *          This effectively means the user has been logged out from the Realm Object Server and credentials has
 *          to be re-verified on the Authentication Server. Since this involves creating a new User object object,
 *          this session will be stopped and and error reported.
 *     </li>
 *     <li>
 *          <b>Access token has expired:</b>
 *          This state will automatically refresh it and retry binding the Realm.
 *     </li>
 *     <li>
 *          <b>Access token does not exists:</b>
 *          This state means the user has logged in, but not yet gained a specific access token for this Realm.
 *          This state will automatically fetch the access token and retry binding the Realm.
 *      </li>
 * </ol>
 */
class AuthenticatingState extends FsmState {

    @Override
    public void onEnterState() {
        if (NetworkStateReceiver.isOnline(session.configuration.getContext())) {
            authenticate(session);
        } else {
            // Wait for connection to become available, before trying again.
            // The Session might potentially stay in this state for the lifetime of the application.
            // This is acceptable.
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

    private synchronized void authenticate(final SyncSession session) {
        session.authenticateRealm(new Runnable() {
            @Override
            public void run() {
                gotoNextState(SessionState.BINDING);
            }
        }, new io.realm.objectserver.Session.ErrorHandler() {
            @Override
            public void onError(io.realm.objectserver.Session session, ObjectServerError error) {
                // FIXME For critical errors, got directly to STOPPED
                gotoNextState(SessionState.UNBOUND);
            }
        });
    }

    @Override
    public void onBind() {
        gotoNextState(SessionState.BINDING); // Equivalent to forcing a retry
    }

    @Override
    public void onUnbind() {
        gotoNextState(SessionState.UNBOUND); // Treat this as user wanting to exit a binding in progress.
    }

    @Override
    public void onStop() {
        gotoNextState(SessionState.STOPPED);
    }
}
