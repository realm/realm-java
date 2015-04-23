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

public class RealmMigrationNeededException extends RuntimeException {

    private final String absoluteRealmPath;

    public RealmMigrationNeededException(String absoluteRealmPath, String detailMessage) {
        super(detailMessage);
        this.absoluteRealmPath = absoluteRealmPath;
    }

    public RealmMigrationNeededException(String absoluteRealmPath, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.absoluteRealmPath = absoluteRealmPath;
    }

    /**
     * Returns the absolute path to the Realm file that needs to be migrated.
     *
     * This can be used for easy reference during a migration:
     *
     * <pre>
     * try {
     *   Realm.getInstance(context);
     * } cathc (RealmMigrationNeededException e) {
     *   Realm.migrateRealmAtPath(e.getRealmPath(), new CustomMigration());
     * }
     * </pre>
     *
     * @return Absolute path to the Realm file
     */
    public String getPath() {
        return absoluteRealmPath;
    }
}
