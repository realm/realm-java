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
