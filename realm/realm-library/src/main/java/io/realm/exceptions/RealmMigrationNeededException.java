/*
 * Copyright 2014 Realm Inc.
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

import java.io.File;

public class RealmMigrationNeededException extends RuntimeException {

    private final String canonicalRealmPath;

    public RealmMigrationNeededException(String canonicalRealmPath, String detailMessage) {
        super(detailMessage);
        this.canonicalRealmPath = canonicalRealmPath;
    }

    public RealmMigrationNeededException(String canonicalRealmPath, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.canonicalRealmPath = canonicalRealmPath;
    }

    /**
     * Returns the canonical path to the Realm file that needs to be migrated.
     *
     * This can be used for easy reference during a migration:
     *
     * <pre>
     * try {
     *   Realm.getInstance(context);
     * } catch (RealmMigrationNeededException e) {
     *   Realm.migrateRealmAtPath(e.getRealmPath(), new CustomMigration());
     * }
     * </pre>
     *
     * @return Canonical path to the Realm file.
     * @see File#getCanonicalPath()
     */
    public String getPath() {
        return canonicalRealmPath;
    }
}
