/*
 * Copyright 2015 Realm Inc.
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

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.entities.AllTypes;
import io.realm.entities.AnnotationTypes;
import io.realm.entities.FieldOrder;
import io.realm.entities.NullTypes;
import io.realm.entities.PrimaryKeyAsBoxedByte;
import io.realm.entities.PrimaryKeyAsBoxedInteger;
import io.realm.entities.PrimaryKeyAsBoxedLong;
import io.realm.entities.PrimaryKeyAsBoxedShort;
import io.realm.entities.PrimaryKeyAsByte;
import io.realm.entities.PrimaryKeyAsInteger;
import io.realm.entities.PrimaryKeyAsLong;
import io.realm.entities.PrimaryKeyAsShort;
import io.realm.entities.PrimaryKeyAsString;
import io.realm.entities.StringOnly;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.Table;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmMigrationTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Realm realm;
    private Context context;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getContext();
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    @Test
    public void getInstance_realmClosedAfterMigrationException() throws IOException {
        String REALM_NAME = "default0.realm";
        RealmConfiguration realmConfig = configFactory.createConfiguration(REALM_NAME);
        configFactory.copyRealmFromAssets(context, REALM_NAME, REALM_NAME);
        try {
            Realm.getInstance(realmConfig);
            fail("A migration should be triggered");
        } catch (RealmMigrationNeededException expected) {
            Realm.deleteRealm(realmConfig); // Delete old realm
        }

        // This should recreate the Realm with proper schema
        Realm realm = Realm.getInstance(realmConfig);
        int result = realm.where(AllTypes.class).equalTo("columnString", "Foo").findAll().size();
        assertEquals(0, result);
        realm.close();
    }

    // If a migration creates a different ordering of columns on Realm A, while another ordering is generated by
    // creating a new Realm B. Global column indices will not work. They must be calculated for each Realm.
    @Test
    public void localColumnIndices() throws IOException {
        String MIGRATED_REALM = "migrated.realm";
        String NEW_REALM = "new.realm";

        // Migrate old Realm to proper schema

        // V1 config
        RealmConfiguration v1Config = configFactory.createConfigurationBuilder()
                .name(MIGRATED_REALM)
                .schema(AllTypes.class)
                .schemaVersion(1)
                .build();
        Realm oldRealm = Realm.getInstance(v1Config);
        oldRealm.close();

        // V2 config
        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                RealmSchema schema = realm.getSchema();
                schema.create("FieldOrder")
                        .addField("field2", int.class)
                        .addField("field1", boolean.class);
            }
        };

        RealmConfiguration v2Config = configFactory.createConfigurationBuilder()
                .name(MIGRATED_REALM)
                .schema(AllTypes.class, FieldOrder.class)
                .schemaVersion(2)
                .migration(migration)
                .build();
        oldRealm = Realm.getInstance(v2Config);

        // Create new Realm which will cause column indices to be recalculated based on the order in the java file
        // instead of the migration
        RealmConfiguration newConfig = configFactory.createConfigurationBuilder()
                .name(NEW_REALM)
                .schemaVersion(2)
                .schema(AllTypes.class, FieldOrder.class)
                .build();
        Realm newRealm = Realm.getInstance(newConfig);
        newRealm.close();

        // Try to query migrated realm. With local column indices this will work. With global it will fail.
        assertEquals(0, oldRealm.where(FieldOrder.class).equalTo("field1", true).findAll().size());
        oldRealm.close();
    }

    @Test
    public void notSettingIndexThrows() {

        // Create v0 of the Realm
        RealmConfiguration originalConfig = configFactory.createConfigurationBuilder()
                .schema(AllTypes.class)
                .build();
        Realm.getInstance(originalConfig).close();

        // Create v1 of the Realm
        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                RealmSchema schema = realm.getSchema();
                schema.create("AnnotationTypes")
                        .addField("id", long.class, FieldAttribute.PRIMARY_KEY)
                        .addField("indexString", String.class) // Forget to set @Index
                        .addField("notIndexString", String.class);
            }
        };

        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schemaVersion(1)
                .schema(AllTypes.class, AnnotationTypes.class)
                .migration(migration)
                .build();
        try {
            realm = Realm.getInstance(realmConfig);
            fail();
        } catch (RealmMigrationNeededException ignored) {
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    @Test
    public void notSettingPrimaryKeyThrows() {

        // Create v0 of the Realm
        RealmConfiguration originalConfig = configFactory.createConfigurationBuilder()
                .schema(AllTypes.class)
                .build();
        Realm.getInstance(originalConfig).close();

        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                RealmSchema schema = realm.getSchema();
                schema.create("AnnotationTypes")
                        .addField("id", long.class) // Forget to set @PrimaryKey
                        .addField("indexString", String.class, FieldAttribute.INDEXED)
                        .addField("notIndexString", String.class);
            }
        };

        // Create v1 of the Realm
        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schemaVersion(1)
                .schema(AllTypes.class, AnnotationTypes.class)
                .migration(migration)
                .build();
        try {
            realm = Realm.getInstance(realmConfig);
            fail();
        } catch (RealmMigrationNeededException e) {
            if (!e.getMessage().equals("Primary key not defined for field 'id' in existing Realm file. Add @PrimaryKey.")) {
                fail(e.toString());
            }
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    @Test
    public void settingPrimaryKeyWithObjectSchema() {
        // Create v0 of the Realm
        RealmConfiguration originalConfig = configFactory.createConfigurationBuilder()
                .schema(AllTypes.class)
                .build();
        Realm.getInstance(originalConfig).close();

        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                RealmSchema schema = realm.getSchema();
                schema.create("AnnotationTypes")
                        .addField("id", long.class)
                        .addPrimaryKey("id")    // use addPrimaryKey() instead of adding FieldAttribute.PrimaryKey
                        .addField("indexString", String.class)
                        .addIndex("indexString") // use addIndex() instead of FieldAttribute.Index
                        .addField("notIndexString", String.class);
            }
        };

        // Create v1 of the Realm
        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schemaVersion(1)
                .schema(AllTypes.class, AnnotationTypes.class)
                .migration(migration)
                .build();

        realm = Realm.getInstance(realmConfig);
        RealmObjectSchema schema = realm.getSchema().getSchemaForClass(AnnotationTypes.class);
        assertTrue(schema.hasPrimaryKey());
        assertTrue(schema.hasIndex("id"));
        realm.close();
    }

    // adding search index is idempotent
    @Test
    public void addingSearchIndexTwice() throws IOException {
        final Class[] classes = {PrimaryKeyAsLong.class, PrimaryKeyAsString.class};

        for (final Class clazz : classes) {
            final AtomicBoolean didMigrate = new AtomicBoolean(false);

            RealmMigration migration = new RealmMigration() {
                @Override
                public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                    RealmObjectSchema schema = realm.getSchema().getSchemaForClass(clazz.getSimpleName());
                    schema.addIndex("id");
                    // @PrimaryKey fields in PrimaryKeyAsLong and PrimaryKeyAsString.class should be set 'nullable'.
                    schema.setNullable("name", true);
                    didMigrate.set(true);
                }
            };
            RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                    .schemaVersion(42)
                    .schema(clazz)
                    .migration(migration)
                    .build();
            Realm.deleteRealm(realmConfig);
            configFactory.copyRealmFromAssets(context, "default-before-migration.realm", Realm.DEFAULT_REALM_NAME);
            Realm.migrateRealm(realmConfig);
            realm = Realm.getInstance(realmConfig);
            assertEquals(42, realm.getVersion());
            assertTrue(didMigrate.get());
            Table table = realm.getTable(clazz);
            assertEquals(true, table.hasSearchIndex(table.getColumnIndex("id")));
            realm.close();
        }
    }

    @Test
    public void setAnnotations() {

        // Create v0 of the Realm
        RealmConfiguration originalConfig = configFactory.createConfigurationBuilder()
                .schema(AllTypes.class)
                .build();
        Realm.getInstance(originalConfig).close();

        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                RealmSchema schema = realm.getSchema();
                schema.create("AnnotationTypes")
                        .addField("id", long.class, FieldAttribute.PRIMARY_KEY)
                        .addField("indexString", String.class, FieldAttribute.INDEXED)
                        .addField("notIndexString", String.class);
            }
        };

        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schemaVersion(1)
                .schema(AllTypes.class, AnnotationTypes.class)
                .migration(migration)
                .build();

        realm = Realm.getInstance(realmConfig);
        Table table = realm.getTable(AnnotationTypes.class);
        assertEquals(3, table.getColumnCount());
        assertTrue(table.hasPrimaryKey());
        assertTrue(table.hasSearchIndex(table.getColumnIndex("id")));
        assertTrue(table.hasSearchIndex(table.getColumnIndex("indexString")));
    }

    @Test
    public void migrationException_getPath() throws IOException {
        configFactory.copyRealmFromAssets(context, "default0.realm", Realm.DEFAULT_REALM_NAME);
        File realm = new File(configFactory.getRoot(), Realm.DEFAULT_REALM_NAME);
        try {
            Realm.getInstance(configFactory.createConfiguration());
            fail();
        } catch (RealmMigrationNeededException expected) {
            assertEquals(expected.getPath(), realm.getCanonicalPath());
        }
    }

    // In default-before-migration.realm, CatOwner has a RealmList<Dog> field.
    // This is changed to RealmList<Cat> and getInstance() must throw an exception.
    @Test
    public void migrationException_realmListChanged() throws IOException {
        configFactory.copyRealmFromAssets(context,
                "default-before-migration.realm", Realm.DEFAULT_REALM_NAME);
        try {
            realm = Realm.getInstance(configFactory.createConfiguration());
            fail();
        } catch (RealmMigrationNeededException ignored) {
        }
    }

    // Pre-null Realms will leave columns not-nullable after the underlying storage engine has
    // migrated the file format. But @Required must be added, and forgetting so will give you
    // a RealmMigrationNeeded exception.
    @Test
    public void openPreNullRealmRequiredMissing() throws IOException {
        configFactory.copyRealmFromAssets(context,
                "default-before-migration.realm", Realm.DEFAULT_REALM_NAME);
        RealmMigration realmMigration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                // intentionally left empty
            }
        };

        try {
            RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                    .schemaVersion(0)
                    .schema(StringOnly.class)
                    .migration(realmMigration)
                    .build();
            Realm realm = Realm.getInstance(realmConfig);
            realm.close();
            fail();
        } catch (RealmMigrationNeededException e) {
            assertEquals("Field 'chars' is required. Either set @Required to field 'chars' or migrate using RealmObjectSchema.setNullable().",
                    e.getMessage());
        }
    }

    // Pre-null Realms will leave columns not-nullable after the underlying storage engine has
    // migrated the file format. An explicit migration step to convert to nullable, and the
    // old class (without @Required) can be used,
    @Test
    public void migratePreNull() throws IOException {
        configFactory.copyRealmFromAssets(context,
                "default-before-migration.realm", Realm.DEFAULT_REALM_NAME);
        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                Table table = realm.schema.getTable(StringOnly.class);
                table.convertColumnToNullable(table.getColumnIndex("chars"));
            }
        };

        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schemaVersion(1)
                .schema(StringOnly.class)
                .migration(migration)
                .build();
        Realm realm = Realm.getInstance(realmConfig);
        realm.beginTransaction();
        StringOnly stringOnly = realm.createObject(StringOnly.class);
        stringOnly.setChars(null);
        realm.commitTransaction();
        realm.close();
    }

    // Pre-null Realms will leave columns not-nullable after the underlying storage engine has
    // migrated the file format. If the user adds the @Required annotation to a field and does not
    // change the schema version, no migration is needed. But then, null cannot be used as a value.
    @Test
    public void openPreNullWithRequired() throws IOException {
        configFactory.copyRealmFromAssets(context,
                "default-before-migration.realm", Realm.DEFAULT_REALM_NAME);
        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schemaVersion(0)
                .schema(AllTypes.class)
                .build();
        Realm realm = Realm.getInstance(realmConfig);

        realm.beginTransaction();
        try {
            AllTypes allTypes = realm.createObject(AllTypes.class);
            allTypes.setColumnString(null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        realm.cancelTransaction();

        realm.close();
    }

    // If a required field was nullable before, a RealmMigrationNeededException should be thrown
    @Test
    public void notSettingRequiredForNotNullableThrows() {
        String[] notNullableFields = {NullTypes.FIELD_STRING_NOT_NULL, NullTypes.FIELD_BYTES_NOT_NULL,
                NullTypes.FIELD_BOOLEAN_NOT_NULL, NullTypes.FIELD_BYTE_NOT_NULL, NullTypes.FIELD_SHORT_NOT_NULL,
                NullTypes.FIELD_INTEGER_NOT_NULL, NullTypes.FIELD_LONG_NOT_NULL, NullTypes.FIELD_FLOAT_NOT_NULL,
                NullTypes.FIELD_DOUBLE_NOT_NULL, NullTypes.FIELD_DATE_NOT_NULL};
        for (final String field : notNullableFields) {
            final RealmMigration migration = new RealmMigration() {
                @Override
                public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                    if (oldVersion == 0) { // 0 after initNullTypesTableExcludes
                        // No @Required for not nullable field
                        RealmObjectSchema nullTypesSchema = realm.getSchema().getSchemaForClass(NullTypes.CLASS_NAME);
                        if (field.equals(NullTypes.FIELD_STRING_NOT_NULL)) {
                            // 1 String
                            nullTypesSchema.addField(field, String.class);
                        } else if (field.equals(NullTypes.FIELD_BYTES_NOT_NULL)) {
                            // 2 Bytes
                            nullTypesSchema.addField(field, byte[].class);
                        } else if (field.equals(NullTypes.FIELD_BOOLEAN_NOT_NULL)) {
                            // 3 Boolean
                            nullTypesSchema.addField(field, Boolean.class);
                            //table.addColumn(RealmFieldType.BOOLEAN, field, Table.NULLABLE);
                        } else if (field.equals(NullTypes.FIELD_BYTE_NOT_NULL) ||
                                field.equals(NullTypes.FIELD_SHORT_NOT_NULL) ||
                                field.equals(NullTypes.FIELD_INTEGER_NOT_NULL) ||
                                field.equals(NullTypes.FIELD_LONG_NOT_NULL)) {
                            // 4 Byte 5 Short 6 Integer 7 Long
                            nullTypesSchema.addField(field, Integer.class);
                        } else if (field.equals(NullTypes.FIELD_FLOAT_NOT_NULL)) {
                            // 8 Float
                            nullTypesSchema.addField(field, Float.class);
                        } else if (field.equals(NullTypes.FIELD_DOUBLE_NOT_NULL)) {
                            // 9 Double
                            nullTypesSchema.addField(field, Double.class);
                        } else if (field.equals(NullTypes.FIELD_DATE_NOT_NULL)) {
                            // 10 Date
                            nullTypesSchema.addField(field, Date.class);
                        }
                        // 11 Object skipped
                    }
                }
            };

            @SuppressWarnings("unchecked")
            RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                    .schemaVersion(1)
                    .schema(NullTypes.class)
                    .migration(migration)
                    .build();
            Realm.deleteRealm(realmConfig);
            // Prepare the version 0 db
            DynamicRealm dynamicRealm = DynamicRealm.getInstance(realmConfig);
            TestHelper.initNullTypesTableExcludes(dynamicRealm, field);
            dynamicRealm.close();

            try {
                realm = Realm.getInstance(realmConfig);
                fail("Failed on " + field);
            } catch (RealmMigrationNeededException e) {
                assertEquals("Field '" + field + "' does support null values in the existing Realm file." +
                        " Remove @Required or @PrimaryKey from field '" + field + "' " +
                        "or migrate using RealmObjectSchema.setNullable().",
                        e.getMessage());
            }
        }
    }

    // If a field is not required but was not nullable before, a RealmMigrationNeededException should be thrown
    @Test
    public void settingRequiredForNullableThrows() {
        String[] notNullableFields = {NullTypes.FIELD_STRING_NULL, NullTypes.FIELD_BYTES_NULL,
                NullTypes.FIELD_BOOLEAN_NULL, NullTypes.FIELD_BYTE_NULL, NullTypes.FIELD_SHORT_NULL,
                NullTypes.FIELD_INTEGER_NULL, NullTypes.FIELD_LONG_NULL, NullTypes.FIELD_FLOAT_NULL,
                NullTypes.FIELD_DOUBLE_NULL, NullTypes.FIELD_DATE_NULL};
        for (final String field : notNullableFields) {
            final RealmMigration migration = new RealmMigration() {
                @Override
                public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                    if (oldVersion == 0) { // 0 after initNullTypesTableExcludes
                        // No @Required for not nullable field
                        RealmObjectSchema nullTypesSchema = realm.getSchema().getSchemaForClass(NullTypes.CLASS_NAME);
                        if (field.equals(NullTypes.FIELD_STRING_NULL)) {
                            // 1 String
                            nullTypesSchema.addField(field, String.class, FieldAttribute.REQUIRED);
                        } else if (field.equals(NullTypes.FIELD_BYTES_NULL)) {
                            // 2 Bytes
                            nullTypesSchema.addField(field, byte[].class, FieldAttribute.REQUIRED);
                        } else if (field.equals(NullTypes.FIELD_BOOLEAN_NULL)) {
                            // 3 Boolean
                            nullTypesSchema.addField(field, boolean.class);
                        } else if (field.equals(NullTypes.FIELD_BYTE_NULL) ||
                                field.equals(NullTypes.FIELD_SHORT_NULL) ||
                                field.equals(NullTypes.FIELD_INTEGER_NULL) ||
                                field.equals(NullTypes.FIELD_LONG_NULL)) {
                            // 4 Byte 5 Short 6 Integer 7 Long
                            nullTypesSchema.addField(field, int.class);
                        } else if (field.equals(NullTypes.FIELD_FLOAT_NULL)) {
                            // 8 Float
                            nullTypesSchema.addField(field, float.class);
                        } else if (field.equals(NullTypes.FIELD_DOUBLE_NULL)) {
                            // 9 Double
                            nullTypesSchema.addField(field, double.class);
                        } else if (field.equals(NullTypes.FIELD_DATE_NULL)) {
                            // 10 Date
                            nullTypesSchema.addField(field, Date.class, FieldAttribute.REQUIRED);
                        }
                        // 11 Object skipped
                    }
                }
            };

            @SuppressWarnings("unchecked")
            RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                    .schemaVersion(1)
                    .schema(NullTypes.class)
                    .migration(migration)
                    .build();
            Realm.deleteRealm(realmConfig);
            // Prepare the version 0 db
            DynamicRealm dynamicRealm = DynamicRealm.getInstance(realmConfig);
            TestHelper.initNullTypesTableExcludes(dynamicRealm, field);
            dynamicRealm.close();

            try {
                realm = Realm.getInstance(realmConfig);
                fail("Failed on " + field);
            } catch (RealmMigrationNeededException e) {
                if (field.equals(NullTypes.FIELD_STRING_NULL) || field.equals(NullTypes.FIELD_BYTES_NULL) ||
                        field.equals(NullTypes.FIELD_DATE_NULL)) {
                    assertEquals("Field '" + field + "' is required. Either set @Required to field '" +
                            field + "' " +
                            "or migrate using RealmObjectSchema.setNullable().", e.getMessage());
                } else {
                    assertEquals("Field '" + field + "' does not support null values in the existing Realm file."
                                    + " Either set @Required, use the primitive type for field '"
                                    + field + "' or migrate using RealmObjectSchema.setNullable().",  e.getMessage());
                }
            }
        }
    }

    // Testing older Realms for setting Boxed type primary keys fields nullable in migration process to support Realm Version 0.89+
    @Test
    public void settingNullableToPrimaryKey() throws IOException {
        final long SCHEMA_VERSION = 67;
        final Class[] classes = {PrimaryKeyAsBoxedByte.class, PrimaryKeyAsBoxedShort.class, PrimaryKeyAsBoxedInteger.class, PrimaryKeyAsBoxedLong.class, PrimaryKeyAsString.class};
        for (final Class clazz : classes) {
            final AtomicBoolean didMigrate = new AtomicBoolean(false);
            RealmMigration migration = new RealmMigration() {
                @Override
                public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                    RealmObjectSchema schema = realm.getSchema().getSchemaForClass(clazz.getSimpleName());
                    if (clazz == PrimaryKeyAsString.class) {
                        schema.setNullable("name", true);
                    } else {
                        schema.setNullable("id", true);
                    }
                    didMigrate.set(true);
                }
            };
            RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                    .schemaVersion(SCHEMA_VERSION)
                    .schema(clazz)
                    .migration(migration)
                    .build();
            Realm.deleteRealm(realmConfig);
            configFactory.copyRealmFromAssets(context, "default-notnullable-primarykey.realm", Realm.DEFAULT_REALM_NAME);
            Realm.migrateRealm(realmConfig);
            realm = Realm.getInstance(realmConfig);
            RealmObjectSchema schema = realm.getSchema().getSchemaForClass(clazz);
            assertEquals(SCHEMA_VERSION, realm.getVersion());
            assertTrue(didMigrate.get());
            if (clazz == PrimaryKeyAsString.class) {
                assertEquals(true, schema.isNullable(PrimaryKeyAsString.FIELD_PRIMARY_KEY));
            } else {
                assertEquals(true, schema.isNullable("id"));
            }
            realm.close();
        }
    }

    // Not-setting older boxed type PrimaryKey field nullable to see if migration fails in order to support Realm version 0.89+
    @Test
    public void notSettingNullableToPrimaryKeyThrows() throws IOException {
        configFactory.copyRealmFromAssets(context, "default-notnullable-primarykey.realm", Realm.DEFAULT_REALM_NAME);
        final Class[] classes = {PrimaryKeyAsString.class, PrimaryKeyAsBoxedByte.class, PrimaryKeyAsBoxedShort.class, PrimaryKeyAsBoxedInteger.class, PrimaryKeyAsBoxedLong.class};
        for (final Class clazz : classes) {
            try {
                RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                        .schemaVersion(0)
                        .schema(clazz)
                        .migration(new RealmMigration() {
                            @Override
                            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                                // intentionally left empty to preserve not-nullablility of PrimaryKey on old schema.
                            }
                        })
                        .build();
                Realm realm = Realm.getInstance(realmConfig);
                realm.close();
                fail();
            } catch (RealmMigrationNeededException expected) {
                if (clazz == PrimaryKeyAsString.class) {
                    assertEquals("@PrimaryKey field 'name' does not support null values in the existing Realm file. Migrate using RealmObjectSchema.setNullable(), or mark the field as @Required.",
                            expected.getMessage());
                } else {
                    assertEquals("@PrimaryKey field 'id' does not support null values in the existing Realm file. Migrate using RealmObjectSchema.setNullable(), or mark the field as @Required.",
                            expected.getMessage());
                }
            }
        }
    }

    // Migrate a nullable field containing null value to non-nullable PrimaryKey field throws Realm version 0.89+
    @Test
    public void migrating_nullableField_toward_notNullable_PrimaryKeyThrows() throws IOException {
        configFactory.copyRealmFromAssets(context, "default-nullable-primarykey.realm", Realm.DEFAULT_REALM_NAME);
        final Class[] classes = {PrimaryKeyAsByte.class, PrimaryKeyAsShort.class, PrimaryKeyAsInteger.class, PrimaryKeyAsLong.class};
        for (final Class clazz : classes) {
            try {
                RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                        .schemaVersion(0)
                        .schema(clazz)
                        .migration(new RealmMigration() {
                            @Override
                            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                                // intentionally left empty to demonstrate incompatibilities between nullable/not-nullable PrimaryKeys.
                            }
                        })
                        .build();
                Realm realm = Realm.getInstance(realmConfig);
                realm.close();
                fail();
            } catch (IllegalStateException expected) {
                assertEquals("Cannot migrate an object with null value in field 'id'. Either maintain the same type for primary key field 'id', or remove the object with null value before migration.",
                        expected.getMessage());
            }
        }
    }

    @Test
    public void realmOpenBeforeMigrationThrows() throws FileNotFoundException {
        RealmConfiguration config = configFactory.createConfiguration();
        realm = Realm.getInstance(config);

        try {
            // Trigger manual migration. This can potentially change the schema, so should only be allowed when
            // no-one else is working on the Realm.
            Realm.migrateRealm(config, new RealmMigration() {
                @Override
                public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                    // Do nothing
                }
            });
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    public void migrateRealm_config_nonExistingRealmFile() throws FileNotFoundException {
        RealmConfiguration config = configFactory.createConfigurationBuilder().migration(new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

            }
        }).build();
        thrown.expect(FileNotFoundException.class);
        Realm.migrateRealm(config);
    }

    @Test
    public void migrateRealm_configMigration_nonExistingRealmFile() throws FileNotFoundException {
        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

            }
        };
        RealmConfiguration config = configFactory.createConfiguration();
        thrown.expect(FileNotFoundException.class);
        Realm.migrateRealm(config, migration);
    }

    // TODO Add unit tests for default nullability
    // TODO Add unit tests for default Indexing for Primary keys
}
