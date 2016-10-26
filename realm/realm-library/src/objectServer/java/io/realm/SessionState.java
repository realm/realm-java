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

package io.realm;

import io.realm.annotations.Beta;

/**
 * @Beta
 * Enum describing the various states the Session Finite-State-Machine can be in.
 */
@Beta
public enum SessionState {
    INITIAL,          // Initial starting state
    UNBOUND,          // Start done, Realm is unbound.
    BINDING,          // bind() has been called. Can take a while.
    AUTHENTICATING,   // Trying to authenticate credentials. Can take a while.
    BOUND,            // Local realm was successfully bound to the remote Realm. Changes are being synchronized.
    STOPPED           // Terminal state. Session can no longer be used.
}


