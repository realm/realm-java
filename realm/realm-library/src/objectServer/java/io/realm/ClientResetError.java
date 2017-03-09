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

package io.realm;

import java.io.File;

/**
 * An error that indicates the Realm needs to be reset.
 *
 * A synced Realm may need to be reset because the Realm Object Server encountered an error and had
 * to be restored from a backup. If the backup copy of the remote Realm is of an earlier version
 * than the local copy of the Realm, the server will ask the client to reset the Realm.
 *
 * The reset process is as follows: the local copy of the Realm is copied into a recovery directory
 * for safekeeping, and then deleted from the original location. The next time the Realm for that
 * URL is opened, the Realm will automatically be re-downloaded from the Realm Object Server, and
 * can be used as normal.
 *
 * Data written to the Realm after the local copy of the Realm diverged from the backup remote copy
 * will be present in the local recovery copy of the Realm file. The re-downloaded Realm will
 * initially contain only the data present at the time the Realm was backed up on the server.
 *
 * The client reset process can be initiated in one of two ways.
 *
 * <ol>
 *     <li>Call </li>
 *
 *
 * </ol>
 *
 * The block provided in the
 * `userInfo` dictionary under `kRLMSyncInitiateClientResetBlockKey` can be called to
 * initiate the reset process. This block can be called any time after the error is
 * received, but should only be called if and when your app closes and invalidates every
 * instance of the offending Realm on all threads (note that autorelease pools may make this
 * difficult to guarantee).
 *
 * If the block is not called, the client reset process will be automatically carried out
 * the next time the app is launched and the `RLMSyncManager` singleton is accessed.
 * The value for the `kRLMSyncPathOfRealmBackupCopyKey` key in the `userInfo` dictionary
 * describes the path of the recovered copy of the Realm. This copy will not actually be
 * created until the client reset process is initiated.
 */
public class ClientResetError extends ObjectServerError {

    public ClientResetError(ErrorCode errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    /**
     * Calling this method will execute the Client Reset manually instead of waiting until next
     * app restart. This will only be possible if all instances of that Realm has been closed,
     * otherwise a {@link IllegalStateException} will be thrown.
     *
     * After the backup is complete, the file can be found in the location returned by
     * {@link #getBackupFile()}.
     *
     * This method can be called from any thread.
     *
     * @throws IllegalStateException if not all instances have been closed.
     */
    public void executeClientReset()  {
        // TODO Should we make this method Async?
        // TODO
    }

    /**
     * Returns the location of the backed up file. The file will not be present until the Client
     * Reset has been executed.
     *
     * @return a reference to the location of the backup file once Client Reset has been executed.
     *         Use {@code file.exists()}
     *
     */
    public File getBackupFile() {
        // TODO
        return null;
    }
}
