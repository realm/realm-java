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

package io.realm.internal.objectserver;

import io.realm.ObjectServerError;
import io.realm.SyncSession;
import io.realm.SessionState;
import io.realm.internal.network.NetworkStateReceiver;
import io.realm.log.RealmLog;

/**
 * AUTHENTICATING State. This step is needed if the user does not have proper access or credentials to access the
 * Realm when attempting to bind it. Reasons for not having proper access or invalid credentials include:
 *
 * <ol>
 *     <li>
 *          <b>Refresh token has expired:</b>
 *          This effectively means the user has been logged out from the Realm Object Server and credentials have
 *          to be re-verified by the Authentication Server. Since verification involves creating a new User object,
 *          this session will be stopped and an error reported.
 *     </li>
 *     <li>
 *          <b>Access token has expired:</b>
 *          In this case, the token is automatically refreshed and will retry binding the Realm.
 *     </li>
 *     <li>
 *          <b>Access token does not exist:</b>
 *          This state means the user has logged in, but not yet gained a specific access token for the Realm.
 *          The access token will automatically be fetched and binding the Realm is retried.
 *      </li>
 * </ol>
 */
class AuthenticatingState extends FsmState {

    @Override
    public void onEnterState() {
        if (NetworkStateReceiver.isOnline(SyncObjectServerFacade.getApplicationContext())) {
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

    private synchronized void authenticate(final ObjectServerSession session) {
        session.authenticateRealm(new Runnable() {
            @Override
            public void run() {
                RealmLog.debug("Session[%s]: Access token acquired", session.getConfiguration().getPath());
                gotoNextState(SessionState.BINDING);
            }
        }, new SyncSession.ErrorHandler() {
            @Override
            public void onError(SyncSession s, ObjectServerError error) {
                RealmLog.debug("Session[%s]: Failed to get access token (%d)", session.getConfiguration().getPath(), error.getErrorCode());
                session.onError(error);
            }
        });
    }
}
