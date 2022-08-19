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
 * Strategy that attempts to automatically recover any unsynced changes during a Client Reset, if the 
 * recovery fails the changes would be discarded.
 * <p>
 * A synced Realm may need to be reset because the MongoDB Realm Server encountered an error and had
 * to be restored from a backup or because it has been too long since the client connected to the
 * server so the server has rotated the logs.
 * <p>
 * The Client Reset thus occurs because the server does not have the full information required to
 * bring the Client fully up to date.
 * <p>
 * The recover or discard unsynced changes process is as follows: when a client reset is received by 
 * the client the {@link #onBeforeReset(Realm)} callback is invoked, then the client would be reset. 
 * Once the reset has concluded the callback {@link #onAfterRecovery(Realm, Realm)} (Realm, Realm)} 
 * would be invoked if the changes have been recovered successfully, otherwise the changes would be 
 * discarded and {@link #onAfterDiscard(Realm, Realm)} (Realm, Realm)} (Realm, Realm)} would be invoked.
 * <p>
 * In the event that the client reset could not discard the unsynced data the
 * {@link #onManualResetFallback(SyncSession, ClientResetRequiredError)} would be invoked. It allows to manually
 * resolve the reset as it would have been done in
 * {@link ManuallyRecoverUnsyncedChangesStrategy#onClientReset(SyncSession, ClientResetRequiredError)}.
 */
public interface RecoverOrDiscardUnsyncedChangesStrategy extends AutomaticClientResetStrategy {
    /**
     * {@inheritDoc}
     */
    @Override
    void onBeforeReset(Realm realm);

    /**
     * Callback invoked once the Client Reset has recovered the unsynced changes successfully.
     * It provides two Realm instances, a frozen one displaying the state before the reset and a
     * regular Realm with the current state.
     *
     * @param before {@link Realm} frozen Realm in the state before the reset.
     * @param after  {@link Realm} Realm after the reset.
     */
    void onAfterRecovery(Realm before, Realm after);

    /**
     * Callback invoked before the Client Reset discards any unsynced changes because the recovery
     * failed. It provides two Realm instances, a frozen one displaying the state before the reset
     * and a regular Realm displaying the current state that can be used to recover objects from the
     * reset.
     *
     * @param before {@link Realm} frozen Realm in the state before the reset.
     * @param after  {@link Realm} Realm after the reset.
     */
    void onAfterDiscard(Realm before, Realm after);

    /**
     * {@inheritDoc}
     */
    @Override
    void onManualResetFallback(SyncSession session, ClientResetRequiredError error);
}
