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

import io.realm.objectserver.ObjectServerError;
import io.realm.objectserver.SessionState;

/**
 * BOUND State. At this state the local Realm is bound to the remote Realm and changes is sent in both
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
        session.stopNativeSession();
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
        switch(error.errorCode()) {
            // FIXME: Regenerate this
            // Auth protocol errors (should not happen).
            case IO_EXCEPTION:
            case JSON_EXCEPTION:
            case INVALID_PARAMETERS:
            case MISSING_PARAMETERS:
            case INVALID_CREDENTIALS:
            case UNKNOWN_ACCOUNT:
            case EXISTING_ACCOUNT:
            case ACCESS_DENIED:
            case EXPIRED_REFRESH_TOKEN:
                throw new IllegalStateException("Authentication protocol errors should not happen: " + error.toString());

            // Ignore Network client errors (irrelevant)
            // FIXME: Not accurate: https://github.com/realm/realm-sync/issues/659 How should these be handled?
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
                break;

            case TOKEN_EXPIRED:
                // Only known case we can actually work around.
                // Trigger a rebind which will cause access token to be refreshed.
                gotoNextState(SessionState.BINDING);
                break;

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

            default:
                throw new IllegalArgumentException("Unknown error code:" + error.errorCode());
        }
    }
}
