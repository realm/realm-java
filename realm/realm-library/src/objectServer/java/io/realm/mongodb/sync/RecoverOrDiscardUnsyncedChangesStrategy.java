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
 * Strategy that tries to automatically recover any unsynced changes during a Client Reset.
 * <p>
 * A synced Realm may need to be reset because the MongoDB Realm Server encountered an error and had
 * to be restored from a backup or because it has been too long since the client connected to the
 * server so the server has rotated the logs.
 * <p>
 * The Client Reset thus occurs because the server does not have the full information required to
 * bring the Client fully up to date.
 * <p>
 * In the event that the client reset cannot be automatically recover the unsynced data, it would
 * try to discard any unsynced changes.
 * <p>
 * The discard unsynced changes reset process is as follows: when a client reset is triggered
 * the {@link #onBeforeReset(Realm)} callback is invoked, providing read-only instances of the
 * realm before and after the reset. Once the recovery has completed 
 * successfully the callback {@link #onAfterRecovery(Realm, Realm)} would be invoked. Calling  
 * {@link #onAfterDiscard(Realm, Realm)} (Realm, Realm)} the changes had to be discarded.
 * <p>
 * If discarding the unsynced data is not enough to resolve the reset the
 * {@link #onError(SyncSession, ClientResetRequiredError)} would be invoked. This allows to manually
 * resolve the reset as it would be done in
 * {@link ManuallyRecoverUnsyncedChangesStrategy#onClientReset(SyncSession, ClientResetRequiredError)}.
 */
public interface RecoverOrDiscardUnsyncedChangesStrategy extends SyncClientResetStrategy {

    /**
     * Callback that indicates a Client Reset is about to happen. It provides with a frozen instance
     * of the Realm that is will be reset.
     *
     * @param realm frozen {@link Realm} in its state before the reset.
     *
     */
    void onBeforeReset(Realm realm);

    /**
     * Callback invoked once the Client Reset has recovered the unsynced changes successfully.
     * It provides of two Realm instances, a frozen one displaying the state before the reset and a
     * regular Realm with the current state.
     *
     * @param before {@link Realm} frozen Realm in the before after the reset.
     * @param after  {@link Realm} Realm after the reset.
     */
    void onAfterRecovery(Realm before, Realm after);

    /**
     * Callback invoked once the Client Reset has discarded the unsynced changes because it couldn't
     * recover them. It provides of two Realm instances, a frozen one displaying the state before the
     * reset and a regular Realm with the current state, that can be used to recover objects from the reset.
     *
     * @param before {@link Realm} frozen Realm in the before after the reset.
     * @param after  {@link Realm} Realm after the reset.
     */
    void onAfterDiscard(Realm before, Realm after);

    /**
     * Callback that indicates the seamless Client reset couldn't complete. It should be handled
     * as {@link ManuallyRecoverUnsyncedChangesStrategy#onClientReset(SyncSession, ClientResetRequiredError)}.
     *
     * @param session {@link SyncSession} this error happened on.
     * @param error   {@link ClientResetRequiredError} the specific Client Reset error.
     */
    void onError(SyncSession session, ClientResetRequiredError error);
}
