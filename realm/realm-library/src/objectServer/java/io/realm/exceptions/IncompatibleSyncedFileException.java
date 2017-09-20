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
import io.realm.SyncConfiguration;

/**
 * //TODO copy from .NET descr
 */
public class IncompatibleSyncedFileException extends RealmFileException {
    private final String path;

    public IncompatibleSyncedFileException(String message, String recoveryPath) {
        super(Kind.INCOMPATIBLE_SYNCED_FILE, message);
        this.path = recoveryPath;
    }

    public RealmConfiguration getBackupRealmConfiguration() {
        return SyncConfiguration.forRecovery(path, null);
    }

    public RealmConfiguration getBackupRealmConfiguration(@Nullable byte[] encryptionKey) {
        return SyncConfiguration.forRecovery(path, encryptionKey);
    }

    public RealmConfiguration getBackupRealmConfiguration(@Nullable byte[] encryptionKey, Object... modules) {
        // TODO this should not return from SyncConfiguration
        // but a classic RealmConfiguration where we can specify only the path
        // need the same method RealmConfiguration.forRecovery
        return SyncConfiguration.forRecovery(path, encryptionKey, modules);
    }

    public String getRecoveryPath() {
        return path;
    }
}
