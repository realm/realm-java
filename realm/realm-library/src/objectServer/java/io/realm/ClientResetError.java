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

public class ClientResetError extends ObjectServerError {

    private final File backupFile;
    private final File originalFile;

    public ClientResetError(ErrorCode errorCode, String errorMessage, String backupFilePath, String originalFilePath) {
        super(errorCode, errorMessage);
        this.backupFile = new File(backupFilePath);
        this.originalFile = new File(originalFilePath);
    }

    /**
     * Calling this method will execute the Client Reset manually instead of waiting until next
     * app restart. This will only be possible if all instances of that Realm has been closed,
     * otherwise a {@link IllegalStateException} will be thrown.
     *
     * After the backup is complete, the file can be found in the location returned by
     * {@link #getBackupFileLocation()}.
     *
     * This method can be called from any thread.
     *
     * @throws IllegalStateException if not all instances have been closed.
     */
    public void executeClientReset()  {
        nativeExecuteClientReset(originalFile.getAbsolutePath());
    }

    /**
     * Returns the location of the backed up file. The file will not be present until the Client
     * Reset has been executed.
     *
     * @return a reference to the location of the backup file once Client Reset has been executed.
     *         Use {@code file.exists()}
     *
     */
    public File getBackupFileLocation() {
        return backupFile;
    }

    /**
     * Returns the location of the original file. After the Client Reset has completed, the file at this location
     * will be deleted.
     *
     * @return a reference to the location of the original Realm file. After Client Reset has been executed this file
     *         will no longer exists. Use {@code file.exists()} to check this.
     */
    public File getOriginalFileLocation() {
        return originalFile;
    }


    private native void nativeExecuteClientReset(String originalPath);
}
