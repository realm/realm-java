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
import io.realm.internal.objectserver.ObjectServerSession;

/**
 * Interface describing a given synchronization policy with the Realm Object Server.
 * <p>
 * The sole purpose of classes implementing this interface is to call {@link ObjectServerSession#bind()} and
 * {@link ObjectServerSession#unbind()} as needed, which will control when changes are synchronized between a local and
 * remote Realm.
 *
 * The SyncPolicy is not responsible for managing the lifecycle of the {@link ObjectServerSession} in general. So any
 * implementation of this class should avoid calling {@link ObjectServerSession#stop()} and
 * {@link ObjectServerSession#start()}.
 *
 * If a session is stopped, {@link ObjectServerSession#unbind()} is automatically called and any further calls to
 * {@link ObjectServerSession#bind()} and {@link ObjectServerSession#unbind()} are ignored.
 * {@link #onSessionStopped(ObjectServerSession)} ()} will then be called so the sync policy have a chance to clean up
 * any resources it might be using.
 */
// Internal until we are sure this is the API we want
public interface SyncPolicy {

    /**
     * Called when the session object is created. At this point it is possible to register any relevant error and event
     * listeners in either the Android framework or for the session itself.
     *
     * {@link ObjectServerSession#start()} will be automatically called after this method.
     *
     * @param session the {@link SyncSession} just created. It has not yet been started.
     */
    void onSessionCreated(ObjectServerSession session);

    /**
     * The {@link ObjectServerSession} has been stopped and will ignore any further calls to
     * {@link ObjectServerSession#bind()} and {@link ObjectServerSession#unbind()}. All external resources should be
     * cleaned up.
     *
     * @param session {@link ObjectServerSession} that has been stopped.
     */
    void onSessionStopped(ObjectServerSession session);

    /**
     * Called the first time a Realm is opened on any thread.
     *
     * @param session {@link ObjectServerSession} associated with this Realm.
     */
    void onRealmOpened(ObjectServerSession session);

    /**
     * Called when the last Realm instance across all threads have been closed.
     *
     * @param session {@link ObjectServerSession} associated with this Realm.
     */
    void onRealmClosed(ObjectServerSession session);

    /**
     * Called if an error occurred in the underlying session. In many cases this has caused the session to become
     * unbound.
     *
     * @param error {@link ObjectServerError} object describing the error.
     * @return {@code true} if the error was handled, or {@code false} if it should be propagated further out to the
     * SyncConfigurations error handler.
     *
     * This method is always called from a background thread, never the UI thread.
     *
     * @see SyncConfiguration.Builder#errorHandler(SyncSession.ErrorHandler)
     */
    boolean onError(ObjectServerSession session, ObjectServerError error);
}
