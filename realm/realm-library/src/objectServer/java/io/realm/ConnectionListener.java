/*
 * Copyright 2018 Realm Inc.
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

/**
 * Interface used when reporting changes that happened to the connection used by the session.
 * <p>
 * Multiple sessions might re-use the same connection. In that case, any connection
 * change will be reported to all sessions.
 * <p>
 * If a disconnect happened due to an error, that error will be reported to the sessions
 * {@link io.realm.SyncSession.ErrorHandler}.
 *
 * @see SyncSession#isConnected()
 * @see SyncConfiguration.Builder#errorHandler(SyncSession.ErrorHandler)
 */
public interface ConnectionListener {

    /**
     * A change in the connection to the server was detected.
     *
     * @param oldState the state the connection transitioned from.
     * @param newState the state the connection transitioned to.
     */
    void onChange(ConnectionState oldState, ConnectionState newState);
}
