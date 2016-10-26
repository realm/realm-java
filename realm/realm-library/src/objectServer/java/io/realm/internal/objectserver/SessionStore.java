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

import io.realm.SyncSession;
import io.realm.SyncManager;
import io.realm.SyncConfiguration;

/**
 * Private class for keeping track of sessions.
 * If {@link SyncSession} and {@link ObjectServerSession} are combined at some point, this class can
 * be folded into {@link SyncManager};
 */
public class SessionStore {

    // Map of between a local Realm path and any associated sessionInfo
    private static HashMap<String, SyncSession> sessions = new HashMap<String, SyncSession>();
    private static HashMap<String, ObjectServerSession> privateSessions = new HashMap<String, ObjectServerSession>();

    static synchronized void removeSession(SyncSession session) {
        if (session == null) {
            return;
        }

        Iterator<Map.Entry<String, SyncSession>> it = sessions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, SyncSession> entry = it.next();
            if (entry.getValue().equals(session)) {
                it.remove();
                break;
            }
        }
    }

    public static synchronized void addSession(SyncSession publicSession, ObjectServerSession internalSession) {
        String localPath = publicSession.getConfiguration().getPath();
        sessions.put(localPath, publicSession);
        privateSessions.put(localPath, internalSession);
    }

    public static synchronized boolean hasSession(SyncConfiguration config) {
        String localPath = config.getPath();
        return sessions.containsKey(localPath);
    }

    public static synchronized SyncSession getPublicSession(SyncConfiguration config) {
        String localPath = config.getPath();
        return sessions.get(localPath);
    }

    public static synchronized ObjectServerSession getPrivateSession(SyncSession session) {
        String localPath = session.getConfiguration().getPath();
        return privateSessions.get(localPath);
    }

    public static Collection<ObjectServerSession> getAllSessions() {
        return privateSessions.values();
    }

}


