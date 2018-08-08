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
 * Enum describing the states of the underlying connection used by a {@link SyncSession}.
 */
public enum ConnectionState {

    /**
     * No connection to the server exists. No data is being transferred even if the session
     * is {@link SyncSession.State#ACTIVE}. If the connection entered this state due to an error, this
     * error will be reported to the {@link SyncSession.ErrorHandler}.
     */
    DISCONNECTED(SyncSession.CONNECTION_VALUE_DISCONNECTED),

    /**
     * A connection is currently in progress of being established. If successful the next
     * state is {@link #CONNECTED}. If the connection fails it will be {@link #DISCONNECTED}.
     */
    CONNECTING(SyncSession.CONNECTION_VALUE_CONNECTING),

    /**
     * A connection was successfully established to the server. If the SyncSession is {@link SyncSession.State#ACTIVE}
     * data will now be transferred between the device and the server.
     */
    CONNECTED(SyncSession.CONNECTION_VALUE_CONNECTED);

    final int value;

    ConnectionState(int value) {
        this.value = value;
    }

    static ConnectionState fromNativeValue(long value) {
        ConnectionState[] stateCodes = values();
        for (ConnectionState state : stateCodes) {
            if (state.value == value) {
                return state;
            }
        }

        throw new IllegalArgumentException("Unknown connection state code: " + value);
    }
}
