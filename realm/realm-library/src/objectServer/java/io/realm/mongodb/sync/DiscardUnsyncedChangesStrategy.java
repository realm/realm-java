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

import io.realm.Realm;

/**
 * Strategy that automatically resolves a Client Reset by discarding any unsynced data. Unlike
 * {@link ManuallyRecoverUnsyncedChangesStrategy} there is no need to close Realm instances before
 * performing the client reset. Once completed changes will be reported through the normal collection and
 * object notifications.
 * <p>
 * A synced Realm may need to be reset because the MongoDB Realm Server encountered an error and had
 * to be restored from a backup or because it has been too long since the client connected to the
 * server so the server has rotated the logs.
 * <p>
 * The Client Reset thus occurs because the server does not have the full information required to
 * bring the Client fully up to date.
 * <p>
 * The discard unsynced changes process is as follows: when a client reset is received by the client
 * the {@link #onBeforeReset(Realm)} callback is invoked, then the client would be reset. Once the reset
 * has concluded the callback {@link #onAfterReset(Realm, Realm)} would be invoked if the changes have
 * been discarded successfully.
 * <p>
 * In the event that the client reset could not discard the unsynced data the
 * {@link #onError(SyncSession, ClientResetRequiredError)} would be invoked. It allows to manually
 * resolve the reset as it would have been done in
 * {@link ManuallyRecoverUnsyncedChangesStrategy#onClientReset(SyncSession, ClientResetRequiredError)}.
 */
public interface DiscardUnsyncedChangesStrategy extends AutomaticClientResetStrategy {

    /**
     * {@inheritDoc}
     */
    @Override
    void onBeforeReset(Realm realm);

    /**
     * Callback invoked once the Client Reset has discarded the unsynced changes because. It provides
     * two Realm instances, a frozen one displaying the state before the reset and a regular Realm
     * displaying the current state that can be used to recover objects from the reset.
     *
     * @param before {@link Realm} frozen Realm in the before after the reset.
     * @param after  {@link Realm} Realm after the reset.
     */
    void onAfterReset(Realm before, Realm after);

    /**
     * {@inheritDoc}
     */
    @Override
    void onError(SyncSession session, ClientResetRequiredError error);
}
