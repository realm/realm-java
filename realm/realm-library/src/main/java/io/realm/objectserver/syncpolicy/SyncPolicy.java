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

package io.realm.objectserver.syncpolicy;

import io.realm.objectserver.session.Session;

/**
 * Interface describing a given synchronization policy with the Realm Object Server.
 * The sole purpose of this class is to call {@link Session#bind()} and {@link Session#unbind()} as needed,
 * which controls when changes can be synchronized between an local and remote Realm.
 *
 * The SyncPolicy is not responsible for managing the lifecycle of the {@link Session} in general. So any
 * implementation of this class should not call {@link Session#stop()} or {@link Session#start()}.
 *
 * When {@link Session#stop()} is called by whatever controls the session, {@link Session#unbind()} is
 * automatically called and any further calls to {@link Session#bind()} and {@link Session#unbind()} are
 * ignored. {@link #stop()} will then be called so the sync policy have a chance to
 * clean up any resources it might be using.
 */
public interface SyncPolicy {

    /**
     * Start applying any given synchronization policy, by calling {@code session.bind()} and {@code session.unbind()}
     * as needed.
     *
     * @param session the session to control.
     */
    void apply(Session session);

    /**
     * The local Realm is no longer able to synchronize changes with the remote one. Any code running as part of
     * {@link #apply(Session)} should be stopped and cleaned up.
     */
    void stop();
}
