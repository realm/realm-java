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

/**
 * The RealmMigration class is used to describe the migration of one Realm schema to another.
 * The schema for a Realm is defined by all classes in a project that extend
 * {@link io.realm.RealmObject}, so any changes to these classes will require a migration.
 *
 * To support migrations from any previous schemaVersion to the newest, the following pattern is
 * recommended when writing a migration:
 *
 * <pre>
 * public class CustomMigration implements RealmMigration {
 *   \@Override
 *   public long execute(Realm realm, long schemaVersion) {
 *     if (schemaVersion == 0) {
 *       // Migrate from v0 to v1
 *       schemaVersion++;
 *     }
 *
 *     if (schemaVersion == 0) {
 *       // Migrate from v0 to v1
 *       schemaVersion++;
 *     }
 *
 *     return schemaVersion;
 *   }
 * }
 * </pre>
 *
 * During development when model classes can change frequently, it is possible to use
 * {@link io.realm.Realm#deleteRealm(RealmConfiguration)}. This will delete the database
 * file and eliminate the need for any migrations.
 *
 * @see Realm#migrateRealm(RealmConfiguration)
 * @see Realm#migrateRealm(RealmConfiguration, RealmMigration)
 */
public interface RealmMigration {

    /**
     * Implement this method in your subclass to perform migration.
     *
     * @param realm The Realm on which to perform the migration.
     * @param version The schemaVersion of the Realm at the start of the migration.
     * @return The schemaVersion of the Realm after executing the migration.
     */
    long execute(Realm realm, long version);
}

