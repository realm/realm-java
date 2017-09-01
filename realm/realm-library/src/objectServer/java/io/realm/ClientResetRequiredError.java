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

package io.realm;

import java.io.File;

/**
 * Class encapsulating information needed for handling a Client Reset event.
 *
 * @see io.realm.SyncSession.ErrorHandler#onError(SyncSession, ObjectServerError) for more information
 *      about when and why Client Reset occurs and how to deal with it.
 */
public class ClientResetRequiredError extends ObjectServerError {

    private final SyncConfiguration originalConfiguration;
    private final RealmConfiguration backupConfiguration;
    private final File backupFile;
    private final File originalFile;

    ClientResetRequiredError(ErrorCode errorCode, String errorMessage, SyncConfiguration originalConfiguration, RealmConfiguration backupConfiguration) {
        super(errorCode, errorMessage);
        this.originalConfiguration = originalConfiguration;
        this.backupConfiguration = backupConfiguration;
        this.backupFile = new File(backupConfiguration.getPath());
        this.originalFile = new File(originalConfiguration.getPath());
    }

    /**
     * Calling this method will execute the Client Reset manually instead of waiting until next app restart. This will
     * only be possible if all instances of that Realm have been closed, otherwise a {@link IllegalStateException} will
     * be thrown.
     * <p>
     * After this method returns, the backup file can be found in the location returned by {@link #getBackupFile()}.
     * The file at {@link #getOriginalFile()} have been deleted, but will be recreated from scratch next time a
     * Realm instance is opened.
     *
     * @throws IllegalStateException if not all instances have been closed.
     */
    public void executeClientReset()  {
        synchronized (Realm.class) {
            if (Realm.getGlobalInstanceCount(originalConfiguration) > 0) {
                throw new IllegalStateException("Realm has not been fully closed. Client Reset cannot run before all " +
                        "instances have been closed.");
            }
            nativeExecuteClientReset(originalConfiguration.getPath());
        }
    }

    /**
     * Returns the location of the backed up Realm file. The file will not be present until the Client Reset has been
     * fully executed.
     *
     * @return a reference to the location of the backup file once Client Reset has been executed.
     *         Use {@code file.exists()} to check if the file exists or not.
     *
     */
    public File getBackupFile() {
        return backupFile;
    }

    /**
     * @return the configuration that can be used to open the backup Realm offline.
     */
    public RealmConfiguration getBackupRealmConfiguration() {
        return backupConfiguration;
    }
    /**
     * Returns the location of the original Realm file. After the Client Reset has completed, the file at this location
     * will be deleted.
     *
     * @return a reference to the location of the original Realm file. After Client Reset has been executed this file
     *         will no longer exists. Use {@code file.exists()} to check this.
     */
    public File getOriginalFile() {
        return originalFile;
    }

    // PRECONDITION: All Realm instances for this path must have been closed.
    private native void nativeExecuteClientReset(String originalPath);
}
