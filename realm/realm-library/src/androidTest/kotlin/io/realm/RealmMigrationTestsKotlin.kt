/*
 * Copyright 2020 Realm Inc.
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

package io.realm

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.realm.entities.StringAndInt
import io.realm.entities.StringOnly
import io.realm.exceptions.RealmMigrationNeededException
import io.realm.rule.TestRealmConfigurationFactory
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.IllegalArgumentException
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
// TODO Migrate Java test cases from RealmMigrationTests and remove Kotlin suffix when done.
class RealmMigrationTestsKotlin {

    @get:Rule
    val configFactory = TestRealmConfigurationFactory()

    private open class NoOpMigration : RealmMigration {
        var triggered: Boolean = false
        var oldVersion: Long = 0
        var newVersion: Long = 0
        override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
            this.triggered = true
            this.oldVersion = oldVersion
            this.newVersion = newVersion
        }
    }

    @Test
    fun migrationNotTriggeredForInitialCreation() {
        val migration = NoOpMigration()
        val configV5: RealmConfiguration = configFactory.createConfigurationBuilder()
                .schemaVersion(5, migration)
                .build()
        Realm.getInstance(configV5).close()
        assertFalse(migration.triggered)
    }

    @Test
    fun explicitMigrationWithoutMigrationThrows() {
        val configV0: RealmConfiguration = configFactory.createConfigurationBuilder()
                .build()
        Realm.getInstance(configV0).close()
        assertFailsWith<RealmMigrationNeededException> {
            Realm.migrateRealm(configV0)
        }
    }

    @Test
    fun migrationNotTriggeredIfVersionIsNotUpdated() {
        val configV0: RealmConfiguration = configFactory.createConfigurationBuilder()
                .schema(StringAndInt::class.java)
                .build()
        Realm.getInstance(configV0).close()

        val migration = NoOpMigration()
        val configV0Updated: RealmConfiguration = configFactory.createConfigurationBuilder()
                .schemaVersion(0, migration)
                .schema(StringAndInt::class.java, StringOnly::class.java)
                .build()
        // Neither through implicit migration
        assertFailsWith<RealmMigrationNeededException> {
            Realm.getInstance(configV0Updated).close()
        }
        // nor through explicit migration
        assertFailsWith<RealmMigrationNeededException> {
            Realm.migrateRealm(configV0Updated, migration)
        }
        assertFalse(migration.triggered)
    }

    // Test that
    // - updating version number without migration and schema change will silently accept new
    //   scheme version
    @Test
    fun silentVersionUpdate() {
        val realmConfigV0: RealmConfiguration = configFactory.createConfigurationBuilder().build()
        Realm.getInstance(realmConfigV0).close()
        val newVersion = 2L
        val realmConfigV1: RealmConfiguration = configFactory.createConfigurationBuilder()
                .schemaVersion(newVersion)
                .build()
        Realm.getInstance(realmConfigV1).use {
            assertEquals(newVersion, it.version)
        }
    }

    // Test that
    // - migration is invoked when updating version, even though scheme has not been updated
    @Test
    fun migrationOnSchemaVersionOnlyUpdate() {
        val realmConfigV0: RealmConfiguration = configFactory.createConfigurationBuilder().build()
        Realm.getInstance(realmConfigV0).close()
        val newVersion = 2L
        val migration = NoOpMigration()
        val realmConfigV1: RealmConfiguration = configFactory.createConfigurationBuilder()
                .schemaVersion(newVersion, migration)
                .build()
        Realm.getInstance(realmConfigV1).use {
            assertEquals(newVersion, it.version)
        }
        assertTrue(migration.triggered)
    }

}
