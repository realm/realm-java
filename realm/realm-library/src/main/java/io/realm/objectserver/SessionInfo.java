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

package io.realm.objectserver;

import io.realm.objectserver.credentials.ObjectServerCredentials;

/**
 * This class controls the connection to a Realm Object Server for one Realm. If
 * {@link RealmObjectServerConfiguration#shouldAutoConnect()} returns {@code true}, the session will automatically be
 * managed when opening and closing Realm instances.
 *
 * In particular the Realm will begin syncing depending on the given {@link SyncPolicy} . When a Realm is closed
 * the session will close immediately if there is no local or remote changes that needs to by synced, otherwise it will
 * try to sync all those changes before closing the session.
 *
 *
 */
public class SessionInfo {

    private ObjectServerCredentials credentials;

    /**
     * Connects a local Realm to a remote one by using the credentials provided by the
     * {@link RealmObjectServerConfiguration}.
     *
     * This method will return immediately while the connection is still ongoing on a background thread.
     * {@link SessionInfo#isConnected()} will return {@code true} once the connection is established.
     *
     * @param syncConfig The {@link RealmObjectServerConfiguration} describing this connection.
     * @return a {@link SessionInfo} object used to query and control the connection to the Realm Object Server.
     */
    public void connect() {
        // TODO Rename to bind?

    }

    public void disconnect() {
        // TODO Rename to unbind?

    }

    /**
     * Set the credentials used to bind this Realm to the Realm Object Server. Credentials control access and
     * permissions for that Realm.
     *
     * If a Realm is already connected using older credentials, the connection wil be closed an a new connection
     * will be made using the new credentials.
     *
     * @param credentials credentials to use when authenticating access to the remote Realm.
     */
    public void setCredentials(ObjectServerCredentials credentials) {

    }

    /**
     * Refresh the Access Token. This will happen automatically if it expires, but this allows manual control.
     */
    public void refresh() {

    }


    // IDEAS
    public boolean isAuthenticated() {
        return false;
    }

    public boolean isConnected() {
        return false;
    }


    public boolean isConnecting() {
        return false;
    }
}
