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

package io.realm.objectserver.internal.syncpolicy;

import io.realm.objectserver.ObjectServerError;
import io.realm.objectserver.Session;
import io.realm.objectserver.internal.SyncSession;

/**
 * Interface describing a given synchronization policy with the Realm Object Server.
 * <p>
 * The sole purpose of classes implementing this interface is to call {@link SyncSession#bind()} and
 * {@link SyncSession#unbind()} as needed, which will control when changes are synchronized between a local and
 * remote Realm.
 *
 * The SyncPolicy is not responsible for managing the lifecycle of the {@link SyncSession} in general. So any
 * implementation of this class should avoid calling {@link SyncSession#stop()} and
 * {@link SyncSession#start()}.
 *
 * If a session is stopped, {@link SyncSession#unbind()} is automatically called and any further calls to
 * {@link SyncSession#bind()} and {@link SyncSession#unbind()} are ignored.
 * {@link #onSessionStopped(SyncSession)} ()} will then be called so the sync policy have a chance to clean up
 * any resources it might be using.
 */
// Internal until we are sure this is the API we want
public interface SyncPolicy {

    /**
     * Called when the session object is created. At this point it is possible to register any relevant error and event
     * listeners in either the Android framework or for the session itself.
     *
     * {@link SyncSession#start()} will be automatically called after this method.
     *
     * @param session the {@link Session} just created. It has not yet been started.
     */
    void onSessionCreated(SyncSession session);

    /**
     * The {@link SyncSession} has been stopped and will ignore any further calls to
     * {@link SyncSession#bind()} and {@link SyncSession#unbind()}. All external resources should be
     * cleaned up.
     *
     * @param session {@link SyncSession} that has been stopped.
     */
    void onSessionStopped(SyncSession session);

    /**
     * Called the first time a Realm is opened on any thread.
     *
     * @param session {@link SyncSession} associated with this Realm.
     */
    void onRealmOpened(SyncSession session);

    /**
     * Called when the last Realm instance across all threads have been closed.
     *
     * @param session {@link SyncSession} associated with this Realm.
     */
    void onRealmClosed(SyncSession session);

    /**
     * Called if an error occurred in the underlying session. In many cases this has caused the session to become
     * unbound.
     *
     * @param error {@link io.realm.objectserver.ObjectServerError} object describing the error.
     * @return {@code true} if the error was handled, or {@code false} if it should be propagated further out to the
     * SyncConfigurations error handler.
     *
     * This method is always called from a background thread, never the UI thread.
     *
     * @see io.realm.objectserver.SyncConfiguration.Builder#errorHandler(Session.ErrorHandler)
     */
    boolean onError(SyncSession session, ObjectServerError error);
}
