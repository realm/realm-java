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
import io.realm.mongodb.SyncTestUtils.Companion.createTestUser
import io.realm.entities.IndexedFields
import io.realm.entities.PrimaryKeyAsString
import io.realm.entities.StringOnly
import io.realm.internal.OsObjectSchemaInfo
import io.realm.internal.OsRealmConfig
import io.realm.internal.OsSchemaInfo
import io.realm.internal.OsSharedRealm
import io.realm.mongodb.close
import io.realm.mongodb.sync.testSchema
import io.realm.util.assertFailsWithMessage
import org.hamcrest.CoreMatchers
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import kotlin.test.assertFailsWith

/**
 * Testing methods around migrations for Realms using a [SyncConfiguration].
 */
@RunWith(AndroidJUnit4::class)
class SyncedRealmMigrationTests {

    @get:Rule
    val configFactory = TestSyncConfigurationFactory()

    private lateinit var app: TestApp

    @Before
    fun setUp() {
        app = TestApp()
    }

    @After
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    @Test
    fun migrateRealm_syncConfigurationThrows() {
        val config = configFactory.createSyncConfigurationBuilder(createTestUser(app)).build()
        assertFailsWith<IllegalArgumentException> {
            Realm.migrateRealm(config)
        }
    }

    // Check that the Realm can still be opened even if the ondisk schema are missing fields. These will be added
    // automatically.
    @Test
    fun addField_worksWithMigrationError() {
        val config = configFactory.createSyncConfigurationBuilder(createTestUser(app))
                .testSchema(StringOnly::class.java)
                .build()

        // Setup initial Realm schema (with missing fields)
        val className = StringOnly::class.java.simpleName
        DynamicRealm.getInstance(config).use { dynamicRealm ->
            val schema = dynamicRealm.schema
            dynamicRealm.executeTransaction {
                schema.create(className) // Create empty class
            }
        }

        // Open typed Realm, which will validate the schema
        Realm.getInstance(config).use { realm ->
            assertTrue(realm.schema[className]!!.hasField(StringOnly.FIELD_CHARS)) // Field has been added
        }
    }

    // Check that the Realm can still be opened even if the ondisk schema has more fields than in the model class.
    // The underlying field should not be deleted, just hidden.
    @Test
    fun missingFields_hiddenSilently() {
        val config = configFactory.createSyncConfigurationBuilder(createTestUser(app))
                .testSchema(StringOnly::class.java)
                .build()

        // Setup initial Realm schema (with too many fields)
        val className = StringOnly::class.java.simpleName
        DynamicRealm.getInstance(config).use { dynamicRealm ->
            val schema = dynamicRealm.schema
            dynamicRealm.executeTransaction {
                schema.create(className)
                        .addField(StringOnly.FIELD_CHARS, String::class.java)
                        .addField("newField", String::class.java)
                // A schema version has to be set otherwise Object Store will try to initialize the schema again and reach an
                // error branch. That is not a real case.
                dynamicRealm.version = 0
            }
        }

        // Open typed Realm, which will validate the schema
        Realm.getInstance(config).use { realm ->
            val stringOnlySchema = realm.schema[className]!!
            assertTrue(stringOnlySchema.hasField(StringOnly.FIELD_CHARS))
            assertTrue(stringOnlySchema.hasField("newField"))
            assertEquals(2, stringOnlySchema.fieldNames.size.toLong())
        }
    }

    // Check that a Realm cannot be opened if it contain breaking schema changes, like changing a primary key
    @Test
    fun breakingSchemaChange_throws() {
        val config = configFactory.createSyncConfigurationBuilder(createTestUser(app))
                .testSchema(PrimaryKeyAsString::class.java)
                .build()

        // Setup initial Realm schema (with a different primary key)
        val expectedObjectSchema = OsObjectSchemaInfo.Builder(PrimaryKeyAsString.CLASS_NAME, false,2, 0)
                .addPersistedProperty(PrimaryKeyAsString.FIELD_PRIMARY_KEY, RealmFieldType.STRING, false, true, false)
                .addPersistedProperty(PrimaryKeyAsString.FIELD_ID, RealmFieldType.INTEGER, true, true, true)
                .build()
        val schemaInfo = OsSchemaInfo(listOf(expectedObjectSchema))
        val configBuilder = OsRealmConfig.Builder(config).schemaInfo(schemaInfo)
        OsSharedRealm.getInstance(configBuilder, OsSharedRealm.VersionID.LIVE).close()
        assertFailsWithMessage<java.lang.IllegalStateException>(
                CoreMatchers.containsString("The following changes cannot be made in additive-only schema mode:")
        ) {
            Realm.getInstance(config).close()
        }
    }

    // Check that indexes are not being added if the schema version is the same
    @Test
    fun sameSchemaVersion_doNotRebuildIndexes() {
        val config = configFactory.createSyncConfigurationBuilder(createTestUser(app))
                .testSchema(IndexedFields::class.java)
                .schemaVersion(42)
                .build()

        // Setup initial Realm schema (with no indexes)
        val className = IndexedFields::class.java.simpleName

        DynamicRealm.getInstance(config).use { dynamicRealm ->
            val schema = dynamicRealm.schema
            dynamicRealm.executeTransaction {
                schema.create(className)
                        .addField(IndexedFields.FIELD_INDEXED_STRING, String::class.java) // No index
                        .addField(IndexedFields.FIELD_NON_INDEXED_STRING, String::class.java)
                dynamicRealm.version = 42
            }
        }

        Realm.getInstance(config).use { realm ->
            // Opening at same schema version (42) will not rebuild indexes
            val indexedFieldsSchema = realm.schema[className]!!
            assertFalse(indexedFieldsSchema.hasIndex(IndexedFields.FIELD_INDEXED_STRING))
            assertFalse(indexedFieldsSchema.hasIndex(IndexedFields.FIELD_NON_INDEXED_STRING))
        }
    }

    // Check that indexes are being added if the schema version is different
    @Test
    fun differentSchemaVersions_rebuildIndexes() {
        val config = configFactory.createSyncConfigurationBuilder(createTestUser(app))
                .testSchema(IndexedFields::class.java)
                .schemaVersion(42)
                .build()

        // Setup initial Realm schema (with no indexes)
        val className = IndexedFields::class.java.simpleName
        DynamicRealm.getInstance(config).use { dynamicRealm ->
            val schema = dynamicRealm.schema
            dynamicRealm.executeTransaction {
                schema.create(className)
                        .addField(IndexedFields.FIELD_INDEXED_STRING, String::class.java) // No index
                        .addField(IndexedFields.FIELD_NON_INDEXED_STRING, String::class.java)
                dynamicRealm.version = 43
            }
        }

        Realm.getInstance(config).use { realm ->
            // Opening at different schema version (42) should rebuild indexes
            val indexedFieldsSchema = realm.schema[className]!!
            assertNotNull(indexedFieldsSchema)
            assertTrue(indexedFieldsSchema.hasIndex(IndexedFields.FIELD_INDEXED_STRING))
            assertFalse(indexedFieldsSchema.hasIndex(IndexedFields.FIELD_NON_INDEXED_STRING))
        }
    }

    // Check that indexes are being added if other fields are being added as well
    @Test
    fun addingFields_rebuildIndexes() {
        val config = configFactory.createSyncConfigurationBuilder(createTestUser(app))
                .testSchema(IndexedFields::class.java)
                .schemaVersion(42)
                .build()

        // Setup initial Realm schema (with no indexes)
        val className = IndexedFields::class.java.simpleName
        DynamicRealm.getInstance(config).use { dynamicRealm ->
            val schema = dynamicRealm.schema
            dynamicRealm.executeTransaction {
                schema.create(className)
                        .addField(IndexedFields.FIELD_INDEXED_STRING, String::class.java) // No index
                // .addField(IndexedFields.FIELD_NON_INDEXED_STRING, String.class); // Missing field
                dynamicRealm.version = 41
            }
        }

        // Opening at different schema version (42) should add field and rebuild indexes
        Realm.getInstance(config).use { realm ->
            val realmObjectSchema = realm.schema[className]!!
            assertTrue(realmObjectSchema.hasField(IndexedFields.FIELD_NON_INDEXED_STRING))
            assertTrue(realmObjectSchema.hasIndex(IndexedFields.FIELD_INDEXED_STRING))
        }
    }

    @Test
    fun schemaVersionUpgradedWhenMigrating() {
        val config = configFactory.createSyncConfigurationBuilder(createTestUser(app))
                .schemaVersion(42)
                .build()

        // Setup initial Realm schema (with missing fields)
        DynamicRealm.getInstance(config).use { dynamicRealm ->
            val className = StringOnly::class.java.simpleName
            val schema = dynamicRealm.schema
            dynamicRealm.executeTransaction {
                schema.create(className) // Create empty class
                dynamicRealm.version = 1
            }
        }

        // Open typed Realm, which will validate the schema
        Realm.getInstance(config).use { realm ->
            assertEquals(42, realm.version)
        }
    }

    // The remote Realm containing more field than the local typed Realm defined is allowed.
    @Test
    fun moreFieldsThanExpectedIsAllowed() {
        val config = configFactory
                .createSyncConfigurationBuilder(createTestUser(app))
                .testSchema(StringOnly::class.java)
                .build()

        // Initialize schema
        Realm.getInstance(config).close()
        DynamicRealm.getInstance(config).use { dynamicRealm ->
            dynamicRealm.executeTransaction {
                val objectSchema = dynamicRealm.schema[StringOnly.CLASS_NAME]!!
                // Add one extra field which doesn't exist in the typed Realm.
                objectSchema.addField("oneMoreField", Integer::class.java)
            }
            // Column keys cache are cleared when closing
        }

        // Verify schema again.
        Realm.getInstance(config).close()
    }

}
