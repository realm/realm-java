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

package io.realm;

import io.realm.internal.migration.Migration;

/**
 * The RealmMigration class is used to describe the migration of one Realm schema to another.
 * The schema for a Realm is defined by all classes in a project that extend
 * {@link RealmObject}, so any changes to these classes will require a migration.
 *
 * To support migrations from any previous version to the newest, the following pattern is
 * recommended when writing a migration:
 *
 * <pre>
 * public class CustomMigration implements RealmMigration {
 *   \@Override
 *   public long execute(Realm realm, long version) {
 *     if (version == 0) {
 *       // Migrate from v0 to v1
 *       version++;
 *     }
 *
 *     if (version == 0) {
 *       // Migrate from v0 to v1
 *       version++;
 *     }
 *
 *     return version;
 *   }
 * }
 * </pre>
 *
 * During development when model classes can change frequently, it is possible to use
 * {@link Realm#deleteRealmFile(android.content.Context)}. This will delete the database
 * file and eliminate the need for any migrations.
 *
 * @see io.realm.Realm#migrateRealmAtPath(String, byte[], io.realm.DirectRealmMigration)
 * @see io.realm.Realm#migrateRealmAtPath(String, io.realm.DirectRealmMigration)
 */
public interface DirectRealmMigration {

    /**
     * Implement this method to perform a migration
     * @return the new RealmSpec that we should migrate to
     */
    public long migrate(int version, Migration currentSpec);
}

