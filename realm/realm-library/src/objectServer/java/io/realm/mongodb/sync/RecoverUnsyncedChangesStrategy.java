/*
 * Copyright 2022 Realm Inc.
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
 * Strategy that attempts to automatically recover any unsynced changes during a Client Reset.
 * <p>
 * A synced Realm may need to be reset because the MongoDB Realm Server encountered an error and had
 * to be restored from a backup or because it has been too long since the client connected to the
 * server so the server has rotated the logs.
 * <p>
 * The Client Reset thus occurs because the server does not have the full information required to
 * bring the Client fully up to date.
 * <p>
 * The recover unsynced changes process is as follows: when a client reset is received by the client
 * the {@link #onBeforeReset(Realm)} callback is invoked, then the client would be reset. Once the reset
 * has concluded the callback {@link #onAfterReset(Realm, Realm)} would be invoked if the changes have
 * been recovered successfully.
 * <p>
 * In the event that the client reset could not automatically recover the unsynced data the
 * {@link #onError(SyncSession, ClientResetRequiredError)} would be invoked. It allows to manually
 * resolve the reset as it would have been done in
 * {@link ManuallyRecoverUnsyncedChangesStrategy#onClientReset(SyncSession, ClientResetRequiredError)}.

 */
public interface RecoverUnsyncedChangesStrategy extends AutomaticClientResetStrategy {
    /**
     * {@inheritDoc}
     */
    @Override
    void onBeforeReset(Realm realm);

    /**
     * Callback invoked after a client reset has recovered the unsynced changes successfully. It provides
     * two realm instances, a frozen one displaying the state before the reset and a regular realm with
     * the current state.
     *
     * @param before the frozen realm from before the reset.
     * @param after  {@link Realm} Realm after the reset.
     */
    void onAfterReset(Realm before, Realm after);

    /**
     * {@inheritDoc}
     */
    @Override
    void onError(SyncSession session, ClientResetRequiredError error);
}
