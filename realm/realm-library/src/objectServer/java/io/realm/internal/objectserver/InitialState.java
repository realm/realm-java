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
 * INITIAL State. Starting point for the Session Finite-State-Machine.
 */
class InitialState extends FsmState {

    @Override
    public void onEnterState() {
        // Do nothing. We start here
    }

    @Override
    protected void onExitState() {
        // Do nothing. Right now the underlying Realm Core session cannot bound/unbind multiple times, so instead
        // we create a new session object each time the Session becomes unbound.
    }

    @Override
    public void onStart() {
        gotoNextState(SessionState.UNBOUND);
    }

    @Override
    public void onError(ObjectServerError error) {
        // Ignore all errors at this state. None of them would have any impact.
    }
}
