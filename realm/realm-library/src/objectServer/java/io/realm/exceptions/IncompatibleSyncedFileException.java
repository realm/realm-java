/*
 * Copyright 2017 Realm Inc.
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

package io.realm.exceptions;

import javax.annotation.Nullable;

import io.realm.RealmConfiguration;
import io.realm.RealmModel;
import io.realm.SyncConfiguration;
import io.realm.internal.Keep;

/**
 * An exception thrown when attempting to open an incompatible Synchronized Realm file. This usually happens
 * when the Realm file was created with an older version of the SDK and automatic migration to the current version
 * is not possible. When such an exception occurs, the original file is moved to a backup location and a new file is
 * created instead. If you wish to migrate any data from the backup location, you can use {@link #getBackupRealmConfiguration()}
 * to obtain a {@link RealmConfiguration} that can then be used to open the backup Realm. After that, retry
 * opening the original Realm file (which now should be recreated as an empty file) and copy all data from the backup file to the new one.
 * <pre>
 * {@code
 *  SyncConfiguration syncConfig = new SyncConfiguration.Builder(user, serverUri).build();
 *  try {
 *      Realm realm = Realm.getInstance(syncConfig);
 *  } catch (IncompatibleSyncedFileException exception) {
 *      RealmConfiguration backupConfig = exception.getBackupRealmConfiguration();
 *      Realm backupRealm = Realm.getInstance(backupConfig);
 *      realm = Realm.GetInstance(syncConfig);
 *  }
 * }
 * </pre>
 */
@Keep
public class IncompatibleSyncedFileException extends RealmFileException {
    private final String path;

    public IncompatibleSyncedFileException(String message, String recoveryPath) {
        super(Kind.INCOMPATIBLE_SYNC_FILE, message);
        this.path = recoveryPath;
    }

    /**
     * Gets a {@link RealmConfiguration} instance that can be used to open the backup Realm file.
     *
     * Note: This will use the default Realm module (composed of all {@link RealmModel}), and
     * assume no encryption should be used as well.
     *
     * @return A configuration object for the backup Realm.
     */
    public RealmConfiguration getBackupRealmConfiguration() {
        return SyncConfiguration.forRecovery(path, null);
    }

    /**
     * Gets a {@link RealmConfiguration} instance that can be used to open the backup Realm file.
     *
     * Note: This will use the default Realm module (composed of all {@link RealmModel}).
     *
     * @param encryptionKey Optional encryption key that was used to encrypt the original Realm file.
     * @return A configuration object for the backup Realm.
     */
    public RealmConfiguration getBackupRealmConfiguration(@Nullable byte[] encryptionKey) {
        return SyncConfiguration.forRecovery(path, encryptionKey);
    }

    /**
     * Gets a {@link RealmConfiguration} instance that can be used to open the backup Realm file.
     *
     * @param encryptionKey Optional encryption key that was used to encrypt the original Realm file.
     * @param modules restricts Realm schema to the provided module.
     * @return A configuration object for the backup Realm.
     */
    public RealmConfiguration getBackupRealmConfiguration(@Nullable byte[] encryptionKey, Object... modules) {
        return SyncConfiguration.forRecovery(path, encryptionKey, modules);
    }

    /**
     * @return Absolute path to the backup Realm file.
     */
    public String getRecoveryPath() {
        return path;
    }
}
