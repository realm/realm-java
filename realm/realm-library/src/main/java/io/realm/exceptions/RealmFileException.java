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
package io.realm.exceptions;

import java.util.Locale;

import io.realm.internal.Keep;
import io.realm.internal.OsSharedRealm;


/**
 * Class for reporting problems when accessing the Realm related files.
 */
@Keep
public class RealmFileException extends RuntimeException {
    /**
     * The specific kind of this {@link RealmFileException}.
     */
    @Keep
    public enum Kind {
        /**
         * Thrown for any I/O related exception scenarios when a Realm is opened.
         */
        ACCESS_ERROR,
        /**
         * Thrown if the history type of the on-disk Realm is unexpected or incompatible.
         */
        BAD_HISTORY,
        /**
         * Thrown if the user does not have permission to open or create the specified file in the specified access
         * mode when the Realm is opened.
         */
        PERMISSION_DENIED,
        /**
         * Thrown if the destination file exists but it is not supposed to.
         */
        EXISTS,
        /**
         * Thrown if the relevant file cannot be found.
         */
        NOT_FOUND,
        /**
         * Thrown if the database file is currently open in another process which cannot share with the current process
         * due to an architecture mismatch.
         */
        INCOMPATIBLE_LOCK_FILE,
        /**
         * Thrown if the file needs to be upgraded to a new format, but upgrades have been explicitly disabled.
         */
        FORMAT_UPGRADE_REQUIRED,
        /**
         * Thrown if an attempt was made to open an Realm file created with Realm Object Server 1.*, which is
         * not compatible with Realm Object Server 2.*. This exception should automatically be handled by Realm.
         */
        INCOMPATIBLE_SYNC_FILE;

        // Created from byte values by JNI.
        static Kind getKind(byte value) {
            switch (value) {
                case OsSharedRealm.FILE_EXCEPTION_KIND_ACCESS_ERROR:
                    return ACCESS_ERROR;
                case OsSharedRealm.FILE_EXCEPTION_KIND_PERMISSION_DENIED:
                    return PERMISSION_DENIED;
                case OsSharedRealm.FILE_EXCEPTION_KIND_EXISTS:
                    return EXISTS;
                case OsSharedRealm.FILE_EXCEPTION_KIND_NOT_FOUND:
                    return NOT_FOUND;
                case OsSharedRealm.FILE_EXCEPTION_KIND_INCOMPATIBLE_LOCK_FILE:
                    return INCOMPATIBLE_LOCK_FILE;
                case OsSharedRealm.FILE_EXCEPTION_KIND_FORMAT_UPGRADE_REQUIRED:
                    return FORMAT_UPGRADE_REQUIRED;
                case OsSharedRealm.FILE_EXCEPTION_KIND_BAD_HISTORY:
                    return BAD_HISTORY;
                case OsSharedRealm.FILE_EXCEPTION_INCOMPATIBLE_SYNC_FILE:
                    return INCOMPATIBLE_SYNC_FILE;
                default:
                    throw new RuntimeException("Unknown value for RealmFileException kind.");
            }
        }
    }

    private final Kind kind;

    // Called by JNI
    @SuppressWarnings("unused")
    public RealmFileException(byte value, String message) {
        super(message);
        kind = Kind.getKind(value);
    }

    public RealmFileException(Kind kind, String message) {
        super(message);
        this.kind = kind;
    }

    public RealmFileException(Kind kind, Throwable cause) {
        super(cause);
        this.kind = kind;
    }

    public RealmFileException(Kind kind, String message, Throwable cause) {
        super(message, cause);
        this.kind = kind;
    }

    /**
     * Gets the {@link #kind} of this exception.
     *
     * @return the {@link #kind} of this exception.
     */
    public Kind getKind() {
        return kind;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%s Kind: %s.", super.toString(), kind);
    }
}
