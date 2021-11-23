/*
 * Copyright 2021 Realm Inc.
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
package io.realm.mongodb.sync;

import javax.annotation.Nonnull;
import io.realm.Realm;

/**
 * Strategy that automatically resolves a Client Reset by discarding any unsynced data, but otherwise keep the Realm open. Any changes will be reported through the normal collection and object notifications.
 * <p>
 * A synced Realm may need to be reset because the MongoDB Realm Server encountered an error and had
 * to be restored from a backup or because it has been too long since the client connected to the
 * server so the server has rotated the logs.
 * <p>
 * The Client Reset thus occurs because the server does not have the full information required to
 * bring the Client fully up to date.
 * <p>
 * The discard unsynced changes reset process is as follows: when a client reset is triggered
 * the {@link #onBeforeReset(Realm, Realm)} callback is invoked, providing an instance of the
 * Realm before the reset and another after the reset, both read-only. Once the reset has concluded
 * the callback {@link #onAfterReset(Realm)} would be invoked with an instance of the final Realm.
 * <p>
 * In the event that discarding the unsynced data is not enough to resolve the reset the
 * {@link #onError(SyncSession, ClientResetRequiredError)} would be invoked, it allows to manually
 * resolve the reset as it would be done in
 * {@link ManuallyRecoverUnsyncedChangesStrategy#onClientReset(SyncSession, ClientResetRequiredError)}.
 */
public interface DiscardUnsyncedChangesStrategy extends SyncClientResetStrategy {
    /**
     * Callback that indicates a Client Reset is about to happen, provides of two read-only
     * Realm instances one of the current state and the other of the state to be.
     *
     * @param before {@link Realm} read-only Realm instance in its state before the reset.
     * @param after  {@link Realm} read-only Realm instance of the state to become after the reset.
     */
    void onBeforeReset(@Nonnull Realm before, @Nonnull Realm after);

    /**
     * Callback that indicates a Client Reset just happened, provides of a read-only Realm instance
     * of the state after the reset.
     *
     * @param realm {@link Realm} read-only Realm instance in the state after the reset.
     */
    void onAfterReset(@Nonnull Realm realm);

    /**
     * Callback that indicates the seamless Client reset couldn't complete. It should be handled
     * as {@link ManuallyRecoverUnsyncedChangesStrategy#onClientReset(SyncSession, ClientResetRequiredError)}.
     *
     * @param session {@link SyncSession} this error happened on.
     * @param error   {@link ClientResetRequiredError} the specific Client Reset error.
     */
    void onError(@Nonnull SyncSession session, @Nonnull ClientResetRequiredError error);
}
