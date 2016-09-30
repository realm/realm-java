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
import java.util.Iterator;
import java.util.Map;

import io.realm.Session;
import io.realm.SyncManager;
import io.realm.SyncConfiguration;

/**
 * Private class for keeping track of sessions.
 * If {@link Session} and {@link SyncSession} are combined at some point, this class can
 * be folded into {@link SyncManager};
 */
public class SessionStore {

    // Map of between a local Realm path and any associated sessionInfo
    private static HashMap<String, Session> sessions = new HashMap<String, Session>();
    private static HashMap<String, SyncSession> privateSessions = new HashMap<String, SyncSession>();

    static synchronized void removeSession(Session session) {
        if (session == null) {
            return;
        }

        Iterator<Map.Entry<String, Session>> it = sessions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Session> entry = it.next();
            if (entry.getValue().equals(session)) {
                it.remove();
                break;
            }
        }
    }

    public static synchronized void addSession(Session publicSession, SyncSession internalSession) {
        String localPath = publicSession.getConfiguration().getPath();
        sessions.put(localPath, publicSession);
        privateSessions.put(localPath, internalSession);
    }

    public static synchronized boolean hasSession(SyncConfiguration config) {
        String localPath = config.getPath();
        return sessions.containsKey(localPath);
    }

    public static synchronized Session getPublicSession(SyncConfiguration config) {
        String localPath = config.getPath();
        return sessions.get(localPath);
    }

    public static synchronized SyncSession getPrivateSession(Session session) {
        String localPath = session.getConfiguration().getPath();
        return privateSessions.get(localPath);
    }

    public static Collection<SyncSession> getAllSessions() {
        return privateSessions.values();
    }

}


