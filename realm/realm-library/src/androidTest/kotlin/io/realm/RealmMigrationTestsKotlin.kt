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
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class RealmMigrationTestsKotlin {

    @get:Rule
    val configFactory = TestRealmConfigurationFactory()

    @Before
    fun setup() {
    }

    @After
    fun tearDown() {
    }

    private open class TestRealmMigration : RealmMigration {
        var triggered: Boolean = false
        var oldVersion: Long = 0
        var newVersion: Long = 0
        override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
            this.triggered = true
            this.oldVersion = oldVersion
            this.newVersion = newVersion
        }
    }

    // Test that
    // - supplying updated version and migration for schema change we actually trigger migration
    //   with out errors
    @Test
    fun standardMigration() {
        val configV1: RealmConfiguration = configFactory.createConfigurationBuilder()
                .schema(StringAndInt::class.java)
                .schemaVersion(1).build()
        Realm.getInstance(configV1).close()

        val migration = object : TestRealmMigration() {
            override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
                super.migrate(realm, oldVersion, newVersion)
                realm.schema.create("StringOnly")
                        .addField("chars", String::class.java);
            }
        }
        val configV2: RealmConfiguration = configFactory.createConfigurationBuilder()
                .schemaVersion(2)
                .schema(StringAndInt::class.java, StringOnly::class.java)
                .migration(migration)
                .build()
        Realm.getInstance(configV2).close()

        assertTrue(migration.triggered)
    }

    // Test that
    // - we never reach migration f version is not specified, not even when triggering
    //   RealmMigrationNeededException by scheme updates
    @Test
    fun noMigrationWithoutVersion() {
        val migration = TestRealmMigration()

        val configDefault: RealmConfiguration = configFactory.createConfigurationBuilder()
                .schema(StringAndInt::class.java)
                .migration(migration)
                .build()
        // No migration on initial default realm
        Realm.getInstance(configDefault).close()
        assertFalse(migration.triggered)

        val configV0: RealmConfiguration = configFactory.createConfigurationBuilder()
                .migration(migration)
                .schema(StringAndInt::class.java, StringOnly::class.java)
                .build()
        // No migration on actual scheme changes if we did not specify version
        assertFailsWith<RealmMigrationNeededException> {
            Realm.getInstance(configV0).close()
        }
        assertFalse(migration.triggered)

        // Bump version silently
        val realmConfigV1: RealmConfiguration = configFactory.createConfigurationBuilder()
                .schema(StringAndInt::class.java)
                .schemaVersion(1)
                .build()
        Realm.getInstance(realmConfigV1).close()

        val updatingDefaultConfig: RealmConfiguration = configFactory.createConfigurationBuilder()
            .migration(migration)
            .schema(StringAndInt::class.java, StringOnly::class.java)
            .build()
        // No migration if not specifying version on previously non-default version realm
        assertFailsWith<IllegalArgumentException> {
            Realm.getInstance(updatingDefaultConfig).close()
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
        val migration = TestRealmMigration()
        val realmConfigV1: RealmConfiguration = configFactory.createConfigurationBuilder()
                .schemaVersion(newVersion)
                .migration(migration)
                .build()
        Realm.getInstance(realmConfigV1).use {
            assertEquals(newVersion, it.version)
        }
        assertTrue(migration.triggered)
    }

}
