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

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileNotFoundException;
import java.io.IOException;

import io.realm.entities.IndexedFields;
import io.realm.entities.PrimaryKeyAsInteger;
import io.realm.entities.PrimaryKeyAsString;
import io.realm.entities.StringOnly;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.rule.TestSyncConfigurationFactory;
import io.realm.util.SyncTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Testing methods around migrations for Realms using a {@link SyncConfiguration}.
 */
@RunWith(AndroidJUnit4.class)
public class SyncedRealmMigrationTests {

    @Rule
    public final TestSyncConfigurationFactory configFactory = new TestSyncConfigurationFactory();

    @Test
    public void migrateRealm_syncConfigurationThrows() {
        SyncConfiguration config = configFactory.createSyncConfigurationBuilder(SyncTestUtils.createTestUser(), "http://foo.com/auth").build();
        try {
            Realm.migrateRealm(config);
            fail();
        } catch (FileNotFoundException e) {
            fail(e.toString());
        } catch (IllegalArgumentException ignored) {
        }
    }

    // Check that the Realm can still be opened even if the ondisk schema are missing fields. These will be added
    // automatically.
    @Test
    public void addField_worksWithMigrationError() {
        SyncConfiguration config = configFactory.createSyncConfigurationBuilder(SyncTestUtils.createTestUser(), "http://foo.com/auth")
                .schema(StringOnly.class)
                .build();

        // Setup initial Realm schema (with missing fields)
        String className = StringOnly.class.getSimpleName();
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(config);
        RealmSchema schema = dynamicRealm.getSchema();
        dynamicRealm.beginTransaction();
        schema.create(className); // Create empty class
        dynamicRealm.commitTransaction();
        dynamicRealm.close();

        // Open typed Realm, which will validate the schema
        Realm realm = Realm.getInstance(config);
        RealmObjectSchema stringOnlySchema = realm.getSchema().get(className);
        try {
            assertTrue(stringOnlySchema.hasField(StringOnly.FIELD_CHARS)); // Field has been added
        } finally {
            realm.close();
        }
    }

    // Check that the Realm can still be opened even if the ondisk schema has more fields than in the model class.
    // The underlying field should not be deleted, just hidden.
    @Test
    public void missingFields_hiddenSilently() {
        SyncConfiguration config = configFactory.createSyncConfigurationBuilder(SyncTestUtils.createTestUser(), "http://foo.com/auth")
                .schema(StringOnly.class)
                .build();

        // Setup initial Realm schema (with too many fields)
        String className = StringOnly.class.getSimpleName();
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(config);
        RealmSchema schema = dynamicRealm.getSchema();
        dynamicRealm.beginTransaction();
        schema.create(className)
                .addField(StringOnly.FIELD_CHARS, String.class)
                .addField("newField", String.class);
        // A schema version has to be set otherwise Object Store will try to initialize the schema again and reach an
        // error branch. That is not a real case.
        dynamicRealm.setVersion(0);
        dynamicRealm.commitTransaction();
        dynamicRealm.close();

        // Open typed Realm, which will validate the schema
        Realm realm = Realm.getInstance(config);
        RealmObjectSchema stringOnlySchema = realm.getSchema().get(className);
        try {
            assertTrue(stringOnlySchema.hasField(StringOnly.FIELD_CHARS));
            assertTrue(stringOnlySchema.hasField("newField"));
            assertEquals(2, stringOnlySchema.getFieldNames().size());
        } finally {
            realm.close();
        }
    }

    // Check that a Realm cannot be opened if it contain breaking schema changes, like changing a primary key
    @Test
    @Ignore("This test will throw earlier when trying to add a PK field. That case is already covered by" +
            " SchemaTest.addField_withPrimaryKeyModifier_notAllowed(). Although this test will still be valuable for" +
            "Object Store schema integration.")
    // FIXME: Enabled this after OS schema integration.
    public void breakingSchemaChange_throws() {
        SyncConfiguration config = configFactory.createSyncConfigurationBuilder(SyncTestUtils.createTestUser(), "http://foo.com/auth")
                .schema(PrimaryKeyAsString.class)
                .build();

        // Setup initial Realm schema (with a different primary key)
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(config);
        RealmSchema schema = dynamicRealm.getSchema();
        dynamicRealm.beginTransaction();
        schema.create(PrimaryKeyAsString.class.getSimpleName())
                .addField(PrimaryKeyAsString.FIELD_PRIMARY_KEY, String.class)
                .addField(PrimaryKeyAsString.FIELD_ID, long.class, FieldAttribute.PRIMARY_KEY);
        dynamicRealm.commitTransaction();
        dynamicRealm.close();

        try {
            Realm.getInstance(config);
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    // Check that indexes are not being added if the schema version is the same
    @Test
    public void sameSchemaVersion_doNotRebuildIndexes() {

        SyncConfiguration config = configFactory.createSyncConfigurationBuilder(SyncTestUtils.createTestUser(), "http://foo.com/auth")
                .schema(IndexedFields.class)
                .schemaVersion(42)
                .build();

        // Setup initial Realm schema (with no indexes)
        String className = IndexedFields.class.getSimpleName();
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(config);
        RealmSchema schema = dynamicRealm.getSchema();
        dynamicRealm.beginTransaction();
        schema.create(className)
                .addField(IndexedFields.FIELD_INDEXED_STRING, String.class) // No index
                .addField(IndexedFields.FIELD_NON_INDEXED_STRING, String.class);
        dynamicRealm.setVersion(42);
        dynamicRealm.commitTransaction();
        dynamicRealm.close();

        Realm realm = Realm.getInstance(config); // Opening at same schema version (42) will not rebuild indexes

        RealmObjectSchema indexedFieldsSchema = realm.getSchema().get(className);
        try {
            assertFalse(indexedFieldsSchema.hasIndex(IndexedFields.FIELD_INDEXED_STRING));
            assertFalse(indexedFieldsSchema.hasIndex(IndexedFields.FIELD_NON_INDEXED_STRING));
        } finally {
            realm.close();
        }
    }

    // Check that indexes are being added if the schema version is different
    @Test
    public void differentSchemaVersions_rebuildIndexes() {

        SyncConfiguration config = configFactory.createSyncConfigurationBuilder(SyncTestUtils.createTestUser(), "http://foo.com/auth")
                .schema(IndexedFields.class)
                .schemaVersion(42)
                .build();

        // Setup initial Realm schema (with no indexes)
        String className = IndexedFields.class.getSimpleName();
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(config);
        RealmSchema schema = dynamicRealm.getSchema();
        dynamicRealm.beginTransaction();
        schema.create(className)
                .addField(IndexedFields.FIELD_INDEXED_STRING, String.class) // No index
                .addField(IndexedFields.FIELD_NON_INDEXED_STRING, String.class);
        dynamicRealm.setVersion(43);
        dynamicRealm.commitTransaction();
        dynamicRealm.close();

        Realm realm = Realm.getInstance(config); // Opening at different schema version (42) should rebuild indexes
        try {
            RealmObjectSchema indexedFieldsSchema = realm.getSchema().get(className);
            assertNotNull(indexedFieldsSchema);
            assertTrue(indexedFieldsSchema.hasIndex(IndexedFields.FIELD_INDEXED_STRING));
            assertFalse(indexedFieldsSchema.hasIndex(IndexedFields.FIELD_NON_INDEXED_STRING));
        } finally {
            realm.close();
        }
    }

    // Check that indexes are being added if other fields are being added as well
    @Test
    public void addingFields_rebuildIndexes() {

        SyncConfiguration config = configFactory.createSyncConfigurationBuilder(SyncTestUtils.createTestUser(), "http://foo.com/auth")
                .schema(IndexedFields.class)
                .schemaVersion(42)
                .build();

        // Setup initial Realm schema (with no indexes)
        String className = IndexedFields.class.getSimpleName();
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(config);
        RealmSchema schema = dynamicRealm.getSchema();
        dynamicRealm.beginTransaction();
        schema.create(className)
                .addField(IndexedFields.FIELD_INDEXED_STRING, String.class); // No index
                // .addField(IndexedFields.FIELD_NON_INDEXED_STRING, String.class); // Missing field
        dynamicRealm.setVersion(41);
        dynamicRealm.commitTransaction();
        dynamicRealm.close();

        // Opening at different schema version (42) should add field and rebuild indexes
        Realm realm = Realm.getInstance(config);
        try {
            assertTrue(realm.getSchema().get(className).hasField(IndexedFields.FIELD_NON_INDEXED_STRING));
            assertTrue(realm.getSchema().get(className).hasIndex(IndexedFields.FIELD_INDEXED_STRING));
        } finally {
            realm.close();
        }
    }

    @Test
    public void schemaVersionUpgradedWhenMigrating() {
        SyncConfiguration config = configFactory.createSyncConfigurationBuilder(SyncTestUtils.createTestUser(), "http://foo.com/auth")
                .schemaVersion(42)
                .build();

        // Setup initial Realm schema (with missing fields)
        String className = StringOnly.class.getSimpleName();
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(config);
        RealmSchema schema = dynamicRealm.getSchema();
        dynamicRealm.beginTransaction();
        schema.create(className); // Create empty class
        dynamicRealm.setVersion(1);
        dynamicRealm.commitTransaction();
        dynamicRealm.close();

        // Open typed Realm, which will validate the schema
        Realm realm = Realm.getInstance(config);
        try {
            assertEquals(42, realm.getVersion());
        } finally {
            realm.close();
        }
    }

    // The remote Realm containing more field than the local typed Realm defined is allowed.
    @Test
    public void moreFieldsThanExpectedIsAllowed() {
        SyncConfiguration config = configFactory
                .createSyncConfigurationBuilder(SyncTestUtils.createTestUser(), "http://foo.com/auth")
                .schema(StringOnly.class)
                .build();

        // Initialize schema
        Realm.getInstance(config).close();
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(config);
        dynamicRealm.beginTransaction();
        RealmObjectSchema objectSchema = dynamicRealm.getSchema().get(StringOnly.CLASS_NAME);
        // Add one extra field which doesn't exist in the typed Realm.
        objectSchema.addField("oneMoreField", int.class);
        dynamicRealm.commitTransaction();
        // Clear column indices cache.
        dynamicRealm.close();

        // Verify schema again.
        Realm realm = Realm.getInstance(config);
        realm.close();
    }

    // The stable_id_migration.realm is created with sync v1.8.5 with one object created for each object schema.
    @Test
    @Ignore("Not supported by sync right now.")
    public void stableIDMigrationCauseClientReset() throws IOException {
        SyncConfiguration config = configFactory
                .createSyncConfigurationBuilder(SyncTestUtils.createTestUser(), "http://foo.com/auth")
                .schema(StringOnly.class, PrimaryKeyAsString.class, PrimaryKeyAsInteger.class)
                .name("stable_id_migration.realm")
                .build();
        configFactory.copyRealmFromAssets(InstrumentationRegistry.getContext(), "stable_id_migration.realm", config);
        Realm realm = Realm.getInstance(config);
        // TODO: Should the local realm be cleaned? It contains one object for each object schema in the realm.
        realm.close();
    }
}
