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
import io.realm.SessionState;

/**
 * UNBOUND State. This is the default state after a session has been started and no attempt at binding the local Realm
 * has been made.
 */
class UnboundState extends FsmState {

    @Override
    public void onEnterState() {
        // We can enter this state from multiple states which might have had an active session.
        // In those cases cleanup any old native session
        session.stopNativeSession();

        // Create the native session so it is ready to be bound.
        session.createNativeSession();
    }

    @Override
    protected void onExitState() {
        // Do nothing.
    }

    @Override
    public void onBind() {
        gotoNextState(SessionState.BINDING);
    }

    @Override
    public void onError(ObjectServerError error) {
        // Ignore all errors at this state. None of them would have any impact.
    }
}
