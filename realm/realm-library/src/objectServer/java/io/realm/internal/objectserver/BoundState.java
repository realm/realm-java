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

import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.SessionState;

/**
 * BOUND State. In this state the local Realm is bound to the remote Realm and changes are sent in both
 * directions immediately.
 */
class BoundState extends FsmState {

    @Override
    public void onEnterState() {
        // Do nothing. If everything is setup correctly. We should now be synchronizing any changes
        // between the local and remote Realm.
    }

    @Override
    public void onExitState() {
        // Do nothing. Entry states will stop the session if needed.
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
    public void onError(ObjectServerError error) {
        // If a Realms access token has expired, trigger a rebind. If the user is still valid it will automatically
        // refresh it.
        if (error.getErrorCode() == ErrorCode.TOKEN_EXPIRED) {
            //  the server can send a 202 (expired access token) even if the client
            //  still consider this token to be valid (based on timestamps for example)
            //
            //  this may cause the server to send a fatal error (203 bad refresh) if we try to bind
            //  the session with this token. To be safe we remove the token that has been considered by the
            //  the server to be invalid.

            // stop the session to avoid sending a bind to the server which will cause it to return
            // a fatal 203 (bad refresh)
            session.stopNativeSession();
            session.removeAccessToken();

            // Create a new session & bind it
            session.createNativeSession();
            gotoNextState(SessionState.BINDING);

        } else {
            switch (error.getCategory()) {
                case FATAL: gotoNextState(SessionState.STOPPED); break;
                case RECOVERABLE: gotoNextState(SessionState.UNBOUND); break;
            }
        }
    }
}
