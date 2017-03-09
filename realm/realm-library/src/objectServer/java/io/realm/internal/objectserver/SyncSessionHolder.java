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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.realm.SyncConfiguration;
import io.realm.SyncSession;

/**
 * Helper class to hide access to sync sessions, so public API shouldn't expose
 * sensitive operations like {@link #remove(SyncConfiguration)}.
 */
public class SyncSessionHolder {

    // keeps track of SyncSession, using 'realm_path'. Java interface with the ObjectStore using the 'realm_path'
    private static Map<String, SyncSession> sessions = new HashMap<String, SyncSession>();

    /**
     * Return a {@link SyncSession} identified by Realm path.
     * @param realmPath path of the Realm.
     * @return a {@link SyncSession} or {@code null} if no SyncSession found.
     */
    public static synchronized SyncSession get(String realmPath) {
        return sessions.get(realmPath);
    }

    /**
     * @return Return all {@link SyncSession} for iteration purpose, for example.
     */
    public static synchronized Collection<SyncSession> getAll() {
        return sessions.values();
    }

    /**
     * Associate a Realm path with a {@link SyncSession}.
     * @param realmPath path of the Realm.
     * @param session {@link SyncSession}.
     */
    public static synchronized void set(String realmPath, SyncSession session) {
        sessions.put(realmPath, session);
    }

    /**
     * Remove the wrapped Java session.
     * @param syncConfiguration configuration object for the synchronized Realm.
     */
    public static synchronized void remove(SyncConfiguration syncConfiguration) {
        if (syncConfiguration == null) {
            throw new IllegalArgumentException("A non-empty 'syncConfiguration' is required.");
        }
        SyncSession syncSession = sessions.remove(syncConfiguration.getPath());
        if (syncSession != null) {
            syncSession.close();
        }
    }

    /**
     * Clear all previously associated {@link SyncSession}.
     */
    public static void clearAll() {
        sessions.clear();
    }
}
