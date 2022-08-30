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

import io.realm.mongodb.ErrorCode;

/**
 * Strategy to manually resolve a Client Reset, determined by the error code
 * {@link ErrorCode#CLIENT_RESET}.
 * <p>
 * A synced Realm may need to be reset because the MongoDB Realm Server encountered an error and had
 * to be restored from a backup or because it has been too long since the client connected to the
 * server so the server has rotated the logs.
 * <p>
 * The Client Reset thus occurs because the server does not have the full information required to
 * bring the Client fully up to date.
 * <p>
 * The manual reset process is as follows: the local copy of the Realm is copied into a recovery directory
 * for safekeeping, and then deleted from the original location. The next time the Realm for that
 * URL is opened, the Realm will automatically be re-downloaded from MongoDB Realm, and
 * can be used as normal.
 * <p>
 * Data written to the Realm after the local copy of the Realm diverged from the backup remote copy
 * will be present in the local recovery copy of the Realm file. The re-downloaded Realm will
 * initially contain only the data present at the time the Realm was backed up on the server.
 * <p>
 * The client reset process can be initiated in one of two ways:
 * <ol>
 *     <li>
 *         Run {@link ClientResetRequiredError#executeClientReset()} manually. All Realm instances must be
 *         closed before this method is called.
 *     </li>
 *     <li>
 *         If Client Reset isn't executed manually, it will automatically be carried out the next time all
 *         Realm instances have been closed and re-opened. This will most likely be
 *         when the app is restarted.
 *     </li>
 * </ol>
 *
 * <b>WARNING:</b>
 * Any writes to the Realm file between this callback and Client Reset has been executed, will not be
 * synchronized to MongoDB Realm. Those changes will only be present in the backed up file. It is therefore
 * recommended to close all open Realm instances as soon as possible.
 */
public interface ManuallyRecoverUnsyncedChangesStrategy extends SyncClientResetStrategy {
    /**
     * Callback that indicates a Client Reset has happened. This should be handled as quickly as
     * possible as any further changes to the Realm will not be synchronized with the server and
     * must be moved manually from the backup Realm to the new one.
     *
     * @param session {@link SyncSession} this error happened on.
     * @param error   {@link ClientResetRequiredError} the specific Client Reset error.
     */
    void onClientReset(SyncSession session, ClientResetRequiredError error);
}
