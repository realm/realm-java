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

package io.realm.internal.syncpolicy;

import io.realm.ObjectServerError;
import io.realm.SyncSession;
import io.realm.SyncConfiguration;
import io.realm.annotations.Beta;

/**
 * Interface describing a given synchronization policy with the Realm Object Server.
 * <p>
 * The sole purpose of classes implementing this interface is to call {@link SyncSession#start()}
 * and {@link SyncSession#stop()} as needed, which will control when changes are synchronized
 * between a local and remote Realm.
 *
 * If a session is fully destroyed, {@link #onSessionDestroyed(SyncSession)} will be called so
 * any external resources can be cleaned up.
 */
@Beta // Internal until we know if this is the API we want.
public interface SyncPolicy {

    /**
     * Called when the session object is created. At this point it is possible to register any
     * relevant error and event listeners in either the Android framework or for the session itself.
     *
     * @param session the {@link SyncSession} just created. It has not yet been started.
     */
    void onSessionCreated(SyncSession session);

    /**
     * The {@link SyncSession} has been stopped and will not synchronize further changes.
     *
     * @param session The stopped {@link SyncSession}.
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
     * Called when the Session object is fully closed by Realm indicating that it cannot be used
     * any more. This will only happen if all references to the underlying Realm has been released
     * and the Session is stopped. Use this callback for cleaning up any external resources
     * held by the SyncPolicy.
     *
     * @param session {@link SyncSession} about to be fully closed
     */
    void onSessionDestroyed(SyncSession session);

    /**
     * Called if an error occurred in the underlying session. In many cases this has caused the
     * session to become unbound.
     *
     * @param error {@link ObjectServerError} object describing the error.
     * @return {@code true} if the error was handled, or {@code false} if it should be propagated
     * further out to the SyncConfigurations error handler.
     *
     * This method is always called from a background thread, never the UI thread.
     *
     * @see SyncConfiguration.Builder#errorHandler(SyncSession.ErrorHandler)
     */
    boolean onError(SyncSession session, ObjectServerError error);
}
