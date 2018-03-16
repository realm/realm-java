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

import org.hamcrest.CoreMatchers;
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
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.entities.AllTypes;
import io.realm.entities.AnnotationTypes;
import io.realm.entities.CatOwner;
import io.realm.entities.Dog;
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
import io.realm.entities.StringOnlyRequired;
import io.realm.entities.Thread;
import io.realm.entities.migration.MigrationClassRenamed;
import io.realm.entities.migration.MigrationFieldRenameAndAdd;
import io.realm.entities.migration.MigrationFieldRenamed;
import io.realm.entities.migration.MigrationFieldTypeToInt;
import io.realm.entities.migration.MigrationFieldTypeToInteger;
import io.realm.entities.migration.MigrationIndexedFieldRenamed;
import io.realm.entities.migration.MigrationPosteriorIndexOnly;
import io.realm.entities.migration.MigrationPriorIndexOnly;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.OsObjectStore;
import io.realm.internal.Table;
import io.realm.migration.MigrationPrimaryKey;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
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

    private void assertPKField(Realm realm, String className, String expectedName, long expectedIndex) {
        String pkField = OsObjectStore.getPrimaryKeyForObject(realm.sharedRealm, className);
        assertNotNull(pkField);
        RealmObjectSchema objectSchema = realm.getSchema().get(className);
        assertNotNull(objectSchema);
        assertTrue(objectSchema.hasField(expectedName));
        assertEquals(expectedName, pkField);
        //noinspection ConstantConditions
        assertEquals(expectedIndex,
                realm.sharedRealm.getTable(Table.getTableNameForClass(className)).getColumnIndex(pkField));
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
            Realm.deleteRealm(realmConfig); // Deletes old realm.
        }

        // This should recreate the Realm with proper schema.
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

        // Migrates old Realm to proper schema.

        // V1 config
        RealmConfiguration v1Config = configFactory.createConfigurationBuilder()
                .name(MIGRATED_REALM)
                .schema(StringOnly.class)
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
                .schema(StringOnly.class, FieldOrder.class)
                .schemaVersion(2)
                .migration(migration)
                .build();
        oldRealm = Realm.getInstance(v2Config);

        // Creates new Realm which will cause column indices to be recalculated based on the order in the java file
        // instead of the migration.
        RealmConfiguration newConfig = configFactory.createConfigurationBuilder()
                .name(NEW_REALM)
                .schemaVersion(2)
                .schema(StringOnly.class, FieldOrder.class)
                .build();
        Realm newRealm = Realm.getInstance(newConfig);
        newRealm.close();

        // Tries to query migrated realm. With local column indices this will work. With global it will fail.
        assertEquals(0, oldRealm.where(FieldOrder.class).equalTo("field1", true).findAll().size());
        oldRealm.close();
    }

    @Test
    public void notSettingIndexThrows() {

        // Creates v0 of the Realm.
        RealmConfiguration originalConfig = configFactory.createConfigurationBuilder()
                .schema(StringOnly.class)
                .build();
        Realm.getInstance(originalConfig).close();

        // Creates v1 of the Realm.
        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                RealmSchema schema = realm.getSchema();
                schema.create("AnnotationTypes")
                        .addField("id", long.class, FieldAttribute.PRIMARY_KEY)
                        .addField("indexString", String.class) // Forgets to set @Index.
                        .addField("notIndexString", String.class);
            }
        };

        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schemaVersion(1)
                .schema(StringOnly.class, AnnotationTypes.class)
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
    public void addingPrimaryKeyThrows() {

        // Creates v0 of the Realm.
        RealmConfiguration originalConfig = configFactory.createConfigurationBuilder()
                .schema(Thread.class)
                .build();
        Realm.getInstance(originalConfig).close();

        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                RealmSchema schema = realm.getSchema();
                schema.create("AnnotationTypes")
                        .addField("id", long.class) // Forgets to set @PrimaryKey.
                        .addField("indexString", String.class, FieldAttribute.INDEXED)
                        .addField("notIndexString", String.class);
            }
        };

        // Creates v1 of the Realm.
        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schemaVersion(1)
                .schema(Thread.class, AnnotationTypes.class)
                .migration(migration)
                .build();
        try {
            realm = Realm.getInstance(realmConfig);
            fail();
        } catch (RealmMigrationNeededException e) {
            assertThat(e.getMessage(), CoreMatchers.containsString(
                    "Primary Key for class 'AnnotationTypes' has been added"));
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    @Test
    public void removingPrimaryKeyThrows() {

        // Creates v0 of the Realm.
        RealmConfiguration originalConfig = configFactory.createConfigurationBuilder()
                .schema(Thread.class)
                .build();
        Realm.getInstance(originalConfig).close();

        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                RealmSchema schema = realm.getSchema();
                schema.create("StringOnly")
                        .addField("chars", String.class, FieldAttribute.PRIMARY_KEY);
            }
        };

        // Creates v1 of the Realm.
        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schemaVersion(1)
                .schema(Thread.class, StringOnly.class)
                .migration(migration)
                .build();
        try {
            realm = Realm.getInstance(realmConfig);
            fail();
        } catch (RealmMigrationNeededException e) {
            assertThat(e.getMessage(),
                    CoreMatchers.containsString("Primary Key for class 'StringOnly' has been removed."));
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    @Test
    public void changingPrimaryKeyThrows() {

        // Creates v0 of the Realm.
        RealmConfiguration originalConfig = configFactory.createConfigurationBuilder()
                .schema(Thread.class)
                .build();
        Realm.getInstance(originalConfig).close();

        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                RealmSchema schema = realm.getSchema();
                schema.create("PrimaryKeyAsString")
                        .addField("id", long.class, FieldAttribute.PRIMARY_KEY) // Initial @PrimaryKey is on the int.
                        .addField("name", String.class);
            }
        };

        // Creates v1 of the Realm.
        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schemaVersion(1)
                .schema(Thread.class, PrimaryKeyAsString.class)
                .migration(migration)
                .build();
        try {
            realm = Realm.getInstance(realmConfig);
            fail();
        } catch (RealmMigrationNeededException e) {
            assertThat(e.getMessage(), CoreMatchers.containsString(
                    "Primary Key for class 'PrimaryKeyAsString' has changed from 'id' to 'name'."));
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    /**
     * Builds a temporary schema to be modified later in a migration. {@link MigrationPrimaryKey} is
     * the base class when specified.
     *
     * <p>MigrationPrimaryKey is supposed to be a RealmObject, but that would hamper our steps toward
     * testing migrations as Realm looks for it in migration. It is thus set to be an interface.
     *
     * @param className a class whose schema is to be re-created
     * @param createBase create a schema named "MigrationPrimaryKey" instead of {@code className} if {@code true}
     */
    private void buildInitialMigrationSchema(final String className, final boolean createBase) {
        RealmConfiguration config = configFactory.createConfigurationBuilder().build();
        // Init the schema
        Realm.getInstance(config).close();

        DynamicRealm realm = DynamicRealm.getInstance(config);
        realm.beginTransaction();
        // First, removes an existing schema.
        realm.getSchema().remove(className);
        // Then recreates the deleted schema or builds a base schema.
        realm.getSchema()
                .create(createBase ? MigrationPrimaryKey.CLASS_NAME : className)
                .addField(MigrationPrimaryKey.FIELD_FIRST,   Byte.class)
                .addField(MigrationPrimaryKey.FIELD_SECOND,  Short.class)
                .addField(MigrationPrimaryKey.FIELD_PRIMARY, String.class, FieldAttribute.PRIMARY_KEY)
                .addField(MigrationPrimaryKey.FIELD_FOURTH,  Integer.class)
                .addField(MigrationPrimaryKey.FIELD_FIFTH,   Long.class);
        realm.commitTransaction();
        realm.close();
    }

    // Tests to show renaming a class does not hinder its PK field's attribute.
    @Test
    public void renameClassTransferPrimaryKey() {
        buildInitialMigrationSchema(MigrationClassRenamed.CLASS_NAME, true);

        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                realm.getSchema()
                        .rename(MigrationPrimaryKey.CLASS_NAME, MigrationClassRenamed.CLASS_NAME);
            }
        };
        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schemaVersion(1)
                .schema(MigrationClassRenamed.class)
                .migration(migration)
                .build();
        Realm realm = Realm.getInstance(realmConfig);

        Table table = realm.getSchema().getTable(MigrationClassRenamed.class);
        assertEquals(MigrationClassRenamed.DEFAULT_FIELDS_COUNT, table.getColumnCount());
        assertPKField(realm, MigrationClassRenamed.CLASS_NAME, MigrationClassRenamed.FIELD_PRIMARY,
                MigrationClassRenamed.DEFAULT_PRIMARY_INDEX);
        // Old schema does not exist.
        assertNull(realm.getSchema().get(MigrationPrimaryKey.CLASS_NAME));
    }

    @Test
    public void rename_noSimilarPrimaryKeyWithOldSchema() {
        buildInitialMigrationSchema(MigrationClassRenamed.CLASS_NAME, true);

        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                // Let us rename the old schema.
                realm.getSchema()
                        .rename(MigrationPrimaryKey.CLASS_NAME, MigrationClassRenamed.CLASS_NAME);

                // Then recreates the original schema to see if Realm is going to get confused.
                // Unlike the first time with buildInitialMigrationSchema(), we will not have a primary key.
                realm.getSchema()
                        .create(MigrationPrimaryKey.CLASS_NAME)
                        .addField(MigrationPrimaryKey.FIELD_FIRST,   Byte.class)
                        .addField(MigrationPrimaryKey.FIELD_SECOND,  Short.class)
                        .addField(MigrationPrimaryKey.FIELD_PRIMARY, String.class)
                        .addField(MigrationPrimaryKey.FIELD_FOURTH,  Integer.class)
                        .addField(MigrationPrimaryKey.FIELD_FIFTH,   Long.class);
            }
        };
        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schemaVersion(1)
                .schema(MigrationClassRenamed.class)
                .migration(migration)
                .build();
        // Trigger migration
        Realm realm = Realm.getInstance(realmConfig);
        realm.close();

        DynamicRealm dynamicRealm = DynamicRealm.getInstance(realmConfig);
        try {
            assertTrue(dynamicRealm.getSchema().get(MigrationClassRenamed.CLASS_NAME).hasPrimaryKey());
            assertFalse(dynamicRealm.getSchema().get(MigrationPrimaryKey.CLASS_NAME).hasPrimaryKey());
        } finally {
            dynamicRealm.close();
        }
    }

    // Test to show that renaming a class does not effect the primary key.
    @Test
    public void setClassName_transferPrimaryKey() {
        buildInitialMigrationSchema(MigrationClassRenamed.CLASS_NAME, true);

        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                realm.getSchema()
                        .get(MigrationPrimaryKey.CLASS_NAME)
                        .setClassName(MigrationClassRenamed.CLASS_NAME);
            }
        };
        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schemaVersion(1)
                .schema(MigrationClassRenamed.class)
                .migration(migration)
                .build();
        Realm realm = Realm.getInstance(realmConfig);

        Table table = realm.getSchema().getTable(MigrationClassRenamed.class);
        assertEquals(MigrationClassRenamed.DEFAULT_FIELDS_COUNT, table.getColumnCount());
        assertPKField(realm, MigrationClassRenamed.CLASS_NAME, MigrationClassRenamed.FIELD_PRIMARY,
                MigrationClassRenamed.DEFAULT_PRIMARY_INDEX);
        // Old schema does not exist.
        assertNull(realm.getSchema().get(MigrationPrimaryKey.CLASS_NAME));
    }

    @Test
    public void setClassName_noSimilarPrimaryKeyWithOldSchema() {
        buildInitialMigrationSchema(MigrationClassRenamed.CLASS_NAME, true);

        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                // Let us set a new class name.
                realm.getSchema()
                        .get(MigrationPrimaryKey.CLASS_NAME)
                        .setClassName(MigrationClassRenamed.CLASS_NAME);

                // Then recreates the original schema to see if Realm is going to get confused.
                // Unlike the first time with buildInitialMigrationSchema(), we will not have a primary key.
                realm.getSchema()
                        .create(MigrationPrimaryKey.CLASS_NAME)
                        .addField(MigrationPrimaryKey.FIELD_FIRST,   Byte.class)
                        .addField(MigrationPrimaryKey.FIELD_SECOND,  Short.class)
                        .addField(MigrationPrimaryKey.FIELD_PRIMARY, String.class)
                        .addField(MigrationPrimaryKey.FIELD_FOURTH,  Integer.class)
                        .addField(MigrationPrimaryKey.FIELD_FIFTH,   Long.class);
            }
        };
        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schemaVersion(1)
                .schema(MigrationClassRenamed.class)
                .migration(migration)
                .build();
        Realm realm = Realm.getInstance(realmConfig);
        realm.close();

        // We cannot access 'MigrationPrimaryKey' from a typed Realm since it is not part of the pre-defined schema.
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(realmConfig);
        try {
            assertTrue(dynamicRealm.getSchema().get(MigrationClassRenamed.CLASS_NAME).hasPrimaryKey());
            assertFalse(dynamicRealm.getSchema().get(MigrationPrimaryKey.CLASS_NAME).hasPrimaryKey());
        } finally {
            dynamicRealm.close();
        }
    }

    @Test
    public void setClassName_throwOnLongClassName() {
        RealmConfiguration config = configFactory.createConfigurationBuilder().build();
        // Creates the first version of schema.
        Realm.getInstance(config).close();
        DynamicRealm realm = DynamicRealm.getInstance(config);
        realm.beginTransaction();
        realm.getSchema().create(MigrationPrimaryKey.CLASS_NAME);
        realm.commitTransaction();
        realm.close();

        final String tooLongClassName = "MigrationNameIsLongerThan57Char_ThisShouldThrowAnException";
        assertEquals(58, tooLongClassName.length());

        // Gets ready for the 2nd version migration.
        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                realm.getSchema()
                        .get(MigrationPrimaryKey.CLASS_NAME)
                        .setClassName(tooLongClassName);
            }
        };
        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schemaVersion(1)
                .migration(migration)
                .build();

        // Creating Realm instance fails.
        try {
            Realm.getInstance(realmConfig);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals(
                    String.format(Locale.US,
                            "Class name is too long. Limit is %1$d characters: '%2$s' (%3$d)",
                            tooLongClassName.length() - 1,
                            tooLongClassName,
                            tooLongClassName.length()),
                    expected.getMessage());
        }
    }

    // Removing fields before a pk field does not affect the pk.
    @Test
    public void removeFieldsBeforePrimaryKey() {
        buildInitialMigrationSchema(MigrationPosteriorIndexOnly.CLASS_NAME, false);

        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                realm.getSchema().get(MigrationPosteriorIndexOnly.CLASS_NAME)
                        .removeField(MigrationPrimaryKey.FIELD_FIRST)
                        .removeField(MigrationPrimaryKey.FIELD_SECOND);
            }
        };
        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schemaVersion(1)
                .schema(MigrationPosteriorIndexOnly.class)
                .migration(migration)
                .build();
        Realm realm = Realm.getInstance(realmConfig);
        Table table = realm.getSchema().getTable(MigrationPosteriorIndexOnly.class);

        assertEquals(MigrationPosteriorIndexOnly.DEFAULT_FIELDS_COUNT, table.getColumnCount());
        assertPKField(realm, MigrationPosteriorIndexOnly.CLASS_NAME, MigrationPosteriorIndexOnly.FIELD_PRIMARY
                , MigrationPosteriorIndexOnly.DEFAULT_PRIMARY_INDEX);
    }

    // Removing fields after a pk field does not affect the pk.
    @Test
    public void removeFieldsAfterPrimaryKey() {
        buildInitialMigrationSchema(MigrationPriorIndexOnly.CLASS_NAME, false);

        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                realm.getSchema().get(MigrationPriorIndexOnly.CLASS_NAME)
                        .removeField(MigrationPrimaryKey.FIELD_FOURTH)
                        .removeField(MigrationPrimaryKey.FIELD_FIFTH);
            }
        };
        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schemaVersion(1)
                .schema(MigrationPriorIndexOnly.class)
                .migration(migration)
                .build();
        Realm realm = Realm.getInstance(realmConfig);
        Table table = realm.getSchema().getTable(MigrationPriorIndexOnly.class);

        assertEquals(MigrationPriorIndexOnly.DEFAULT_FIELDS_COUNT, table.getColumnCount());
        assertPKField(realm, MigrationPriorIndexOnly.CLASS_NAME, MigrationPriorIndexOnly.FIELD_PRIMARY
                , MigrationPriorIndexOnly.DEFAULT_PRIMARY_INDEX);
    }

    // Renaming the class should also rename the the class entry in the pk metadata table that tracks primary keys.
    @Test
    public void renamePrimaryKeyFieldInMigration() {
        buildInitialMigrationSchema(MigrationFieldRenamed.CLASS_NAME, false);

        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                realm.getSchema().get(MigrationFieldRenamed.CLASS_NAME)
                        .renameField(MigrationPrimaryKey.FIELD_PRIMARY, MigrationFieldRenamed.FIELD_PRIMARY);
            }
        };
        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schemaVersion(1)
                .schema(MigrationFieldRenamed.class)
                .migration(migration)
                .build();
        Realm realm = Realm.getInstance(realmConfig);

        Table table = realm.getSchema().getTable(MigrationFieldRenamed.class);
        assertEquals(MigrationFieldRenamed.DEFAULT_FIELDS_COUNT, table.getColumnCount());
        assertPKField(realm, MigrationFieldRenamed.CLASS_NAME, MigrationFieldRenamed.FIELD_PRIMARY,
                MigrationFieldRenamed.DEFAULT_PRIMARY_INDEX);
    }

    private void createObjectsWithOldPrimaryKey(final String className, final boolean insertNullValue) {
        DynamicRealm realm = DynamicRealm.getInstance(configFactory.createConfigurationBuilder().build());
        try {
            realm.executeTransaction(new DynamicRealm.Transaction() {
                @Override
                public void execute(DynamicRealm realm) {
                    realm.createObject(className, "12");
                    if (insertNullValue) {
                        realm.createObject(className, null);
                    }
                }
            });
        } finally {
            realm.close();
        }
    }

    // This is to test how PK type can change to non-nullable int in migration.
    @Test
    public void modifyPrimaryKeyFieldTypeToIntInMigration() {
        final String TEMP_FIELD_ID = "temp_id";
        buildInitialMigrationSchema(MigrationFieldTypeToInt.CLASS_NAME, false);
        // create objects with the schema provided
        createObjectsWithOldPrimaryKey(MigrationFieldTypeToInt.CLASS_NAME, true);

        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                realm.getSchema().get(MigrationFieldTypeToInt.CLASS_NAME)
                        .addField("temp_id", int.class)
                        .transform(new RealmObjectSchema.Function() {
                            @Override
                            public void apply(DynamicRealmObject obj) {
                                String fieldValue = obj.getString(MigrationPrimaryKey.FIELD_PRIMARY);
                                if (fieldValue != null && fieldValue.length() != 0) {
                                    obj.setInt(TEMP_FIELD_ID, Integer.valueOf(fieldValue).intValue());
                                } else {
                                    // Since this cannot be accepted as proper pk value, we'll delete it.
                                    // *You can modify with some other value such as 0, but that's not
                                    // counted in this scenario.
                                    obj.deleteFromRealm();
                                }
                            }
                        })
                        .removeField(MigrationPrimaryKey.FIELD_PRIMARY)
                        .renameField(TEMP_FIELD_ID, MigrationFieldTypeToInt.FIELD_PRIMARY)
                        .addPrimaryKey(MigrationFieldTypeToInt.FIELD_PRIMARY);
            }
        };
        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schemaVersion(1)
                .schema(MigrationFieldTypeToInt.class)
                .migration(migration)
                .build();
        Realm realm = Realm.getInstance(realmConfig);

        Table table = realm.getSchema().getTable(MigrationFieldTypeToInt.class);
        assertEquals(MigrationFieldTypeToInt.DEFAULT_FIELDS_COUNT, table.getColumnCount());
        assertPKField(realm, MigrationFieldTypeToInt.CLASS_NAME, MigrationFieldTypeToInt.FIELD_PRIMARY,
                MigrationFieldTypeToInt.DEFAULT_PRIMARY_INDEX);

        assertEquals(1, realm.where(MigrationFieldTypeToInt.class).count());
        assertEquals(12, realm.where(MigrationFieldTypeToInt.class).findFirst().fieldIntPrimary);
    }

    // This is to test how PK type can change to nullable Integer in migration.
    @Test
    public void modifyPrimaryKeyFieldTypeToIntegerInMigration() {
        final String TEMP_FIELD_ID = "temp_id";
        buildInitialMigrationSchema(MigrationFieldTypeToInteger.CLASS_NAME, false);
        // Creates objects with the schema provided.
        createObjectsWithOldPrimaryKey(MigrationFieldTypeToInteger.CLASS_NAME, true);

        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                realm.getSchema().get(MigrationFieldTypeToInteger.CLASS_NAME)
                        .addField("temp_id", Integer.class)
                        .transform(new RealmObjectSchema.Function() {
                            @Override
                            public void apply(DynamicRealmObject obj) {
                                String fieldValue = obj.getString(MigrationPrimaryKey.FIELD_PRIMARY);
                                if (fieldValue != null && fieldValue.length() != 0) {
                                    obj.setInt(TEMP_FIELD_ID, Integer.valueOf(fieldValue));
                                } else {
                                    obj.setNull(TEMP_FIELD_ID);
                                }
                            }
                        })
                        .removeField(MigrationPrimaryKey.FIELD_PRIMARY)
                        .renameField(TEMP_FIELD_ID, MigrationFieldTypeToInteger.FIELD_PRIMARY)
                        .addPrimaryKey(MigrationFieldTypeToInteger.FIELD_PRIMARY);
            }
        };
        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schemaVersion(1)
                .schema(MigrationFieldTypeToInteger.class)
                .migration(migration)
                .build();
        Realm realm = Realm.getInstance(realmConfig);

        Table table = realm.getSchema().getTable(MigrationFieldTypeToInteger.class);
        assertEquals(MigrationFieldTypeToInteger.DEFAULT_FIELDS_COUNT, table.getColumnCount());
        assertPKField(realm, MigrationFieldTypeToInteger.CLASS_NAME, MigrationFieldTypeToInteger.FIELD_PRIMARY,
                MigrationFieldTypeToInteger.DEFAULT_PRIMARY_INDEX);

        assertEquals(2, realm.where(MigrationFieldTypeToInteger.class).count());

        // not-null value
        assertEquals(1, realm.where(MigrationFieldTypeToInteger.class)
                             .equalTo(MigrationFieldTypeToInteger.FIELD_PRIMARY, Integer.valueOf(12))
                             .count());

        // null value
        assertEquals(1, realm.where(MigrationFieldTypeToInteger.class)
                             .equalTo(MigrationFieldTypeToInteger.FIELD_PRIMARY, (Integer) null)
                             .count());
    }

    @Test
    public void modifyPrimaryKeyFieldTypeFromIntToStringInMigration() {
        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                RealmObjectSchema objectSchema  = realm.getSchema().get(PrimaryKeyAsString.CLASS_NAME);
                assertNotNull(objectSchema);
                assertEquals(PrimaryKeyAsString.FIELD_ID, objectSchema.getPrimaryKey());
                objectSchema.removePrimaryKey().addPrimaryKey(PrimaryKeyAsString.FIELD_PRIMARY_KEY);
            }
        };

        RealmConfiguration configuration = configFactory.createConfigurationBuilder()
                .schema(PrimaryKeyAsString.class)
                .schemaVersion(1)
                .migration(migration)
                .build();

        // Create the schema and set the int field as primary key
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(configuration);
        dynamicRealm.beginTransaction();
        RealmSchema schema = dynamicRealm.getSchema();
        schema.create(PrimaryKeyAsString.CLASS_NAME)
                .addField(PrimaryKeyAsString.FIELD_ID, long.class, FieldAttribute.PRIMARY_KEY)
                .addField(PrimaryKeyAsString.FIELD_PRIMARY_KEY, String.class);
        dynamicRealm.createObject(PrimaryKeyAsString.CLASS_NAME, 0)
                .setString(PrimaryKeyAsString.FIELD_PRIMARY_KEY, "string0");
        dynamicRealm.createObject(PrimaryKeyAsString.CLASS_NAME, 1)
                .setString(PrimaryKeyAsString.FIELD_PRIMARY_KEY, "string1");
        dynamicRealm.setVersion(0);
        dynamicRealm.commitTransaction();

        // Run migration
        realm = Realm.getInstance(configuration);
        RealmObjectSchema objectSchema = realm.getSchema().get(PrimaryKeyAsString.CLASS_NAME);
        assertNotNull(objectSchema);
        assertEquals(PrimaryKeyAsString.FIELD_PRIMARY_KEY, objectSchema.getPrimaryKey());
        RealmResults<PrimaryKeyAsString> results = realm.where(PrimaryKeyAsString.class)
                .sort(PrimaryKeyAsString.FIELD_ID)
                .findAll();
        assertEquals(2, results.size());
        assertEquals("string0", results.get(0).getName());
        assertEquals("string1", results.get(1).getName());
    }

    @Test
    public void modifyPrimaryKeyFieldTypeFromStringToInt() {
        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                RealmObjectSchema objectSchema  = realm.getSchema().get(PrimaryKeyAsInteger.CLASS_NAME);
                assertNotNull(objectSchema);
                assertEquals(PrimaryKeyAsInteger.FIELD_NAME, objectSchema.getPrimaryKey());
                objectSchema.removePrimaryKey().addPrimaryKey(PrimaryKeyAsInteger.FIELD_ID);
            }
        };

        RealmConfiguration configuration = configFactory.createConfigurationBuilder()
                .schema(PrimaryKeyAsInteger.class)
                .schemaVersion(1)
                .migration(migration)
                .build();

        // Create the schema and set the String field as primary key
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(configuration);
        dynamicRealm.beginTransaction();
        RealmSchema schema = dynamicRealm.getSchema();
        schema.create(PrimaryKeyAsInteger.CLASS_NAME)
                .addField(PrimaryKeyAsInteger.FIELD_ID, int.class)
                .addField(PrimaryKeyAsInteger.FIELD_NAME, String.class, FieldAttribute.PRIMARY_KEY);
        dynamicRealm.createObject(PrimaryKeyAsInteger.CLASS_NAME, "string0")
                .setInt(PrimaryKeyAsInteger.FIELD_ID, 0);
        dynamicRealm.createObject(PrimaryKeyAsInteger.CLASS_NAME, "string1")
                .setInt(PrimaryKeyAsInteger.FIELD_ID, 1);
        dynamicRealm.setVersion(0);
        dynamicRealm.commitTransaction();

        // Run migration
        realm = Realm.getInstance(configuration);

        RealmObjectSchema objectSchema = realm.getSchema().get(PrimaryKeyAsInteger.CLASS_NAME);
        assertNotNull(objectSchema);
        assertEquals(PrimaryKeyAsInteger.FIELD_ID, objectSchema.getPrimaryKey());
        RealmResults<PrimaryKeyAsInteger> results = realm.where(PrimaryKeyAsInteger.class)
                .sort(PrimaryKeyAsInteger.FIELD_ID)
                .findAll();
        assertEquals(2, results.size());
        assertEquals(0, results.get(0).getId());
        assertEquals(1, results.get(1).getId());
    }

    @Test
    public void settingPrimaryKeyWithObjectSchema() {
        // Creates v0 of the Realm.
        RealmConfiguration originalConfig = configFactory.createConfigurationBuilder()
                .schema(StringOnly.class)
                .build();
        Realm.getInstance(originalConfig).close();

        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                RealmSchema schema = realm.getSchema();
                schema.create("AnnotationTypes")
                        .addField("id", long.class)
                        .addPrimaryKey("id")    // Uses addPrimaryKey() instead of adding FieldAttribute.PrimaryKey.
                        .addField("indexString", String.class)
                        .addIndex("indexString") // Uses addIndex() instead of FieldAttribute.Index.
                        .addField("notIndexString", String.class);
            }
        };

        // Creates v1 of the Realm.
        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schemaVersion(1)
                .schema(StringOnly.class, AnnotationTypes.class)
                .migration(migration)
                .build();

        realm = Realm.getInstance(realmConfig);
        RealmObjectSchema schema = realm.getSchema().get("AnnotationTypes");
        assertTrue(schema.hasPrimaryKey());
        assertTrue(schema.hasIndex("id"));
        realm.close();
    }

    @Test
    public void setAnnotations() {

        // Creates v0 of the Realm.
        RealmConfiguration originalConfig = configFactory.createConfigurationBuilder()
                .schema(StringOnly.class)
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
                .schema(StringOnly.class, AnnotationTypes.class)
                .migration(migration)
                .build();

        realm = Realm.getInstance(realmConfig);
        Table table = realm.getTable(AnnotationTypes.class);
        assertEquals(3, table.getColumnCount());
        assertEquals("id", OsObjectStore.getPrimaryKeyForObject(realm.getSharedRealm(), "AnnotationTypes"));
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

    // Check if the RealmList type change can trigger a RealmMigrationNeededException.
    @Test
    public void migrationException_realmListChanged() throws IOException {
        RealmConfiguration config = configFactory.createConfiguration();
        // Initialize the schema with RealmList<Cat>
        Realm.getInstance(configFactory.createConfiguration()).close();

        DynamicRealm dynamicRealm = DynamicRealm.getInstance(config);
        dynamicRealm.beginTransaction();
        // Change the RealmList type to RealmList<Dog>
        RealmObjectSchema dogSchema = dynamicRealm.getSchema().get(Dog.CLASS_NAME);
        RealmObjectSchema ownerSchema = dynamicRealm.getSchema().get(CatOwner.CLASS_NAME);
        ownerSchema.removeField(CatOwner.FIELD_CATS);
        ownerSchema.addRealmListField(CatOwner.FIELD_CATS, dogSchema);
        dynamicRealm.commitTransaction();
        dynamicRealm.close();

        try {
            realm = Realm.getInstance(config);
            fail();
        } catch (RealmMigrationNeededException ignored) {
            assertThat(ignored.getMessage(),
                    CoreMatchers.containsString("Property 'CatOwner.cats' has been changed from 'array<Dog>' to 'array<Cat>'"));
        }
    }

    // Pre-null Realms will leave columns not-nullable after the underlying storage engine has
    // migrated the file format. But @Required must be added, and forgetting so will give you
    // a RealmMigrationNeeded exception.
    @Test
    public void openPreNullRealmRequiredMissing() throws IOException {
        configFactory.copyRealmFromAssets(context,
                "string-only-pre-null-0.82.2.realm", Realm.DEFAULT_REALM_NAME);
        RealmMigration realmMigration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                // Intentionally lefts empty.
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
            assertThat(e.getMessage(), CoreMatchers.containsString(
                    "Property 'StringOnly.chars' has been made optional"));
        }
    }

    // Pre-null Realms will leave columns not-nullable after the underlying storage engine has
    // migrated the file format. An explicit migration step to convert to nullable, and the
    // old class (without @Required) can be used,
    @Test
    public void migratePreNull() throws IOException {
        final AtomicBoolean migrationCalled = new AtomicBoolean(false);
        configFactory.copyRealmFromAssets(context,
                "string-only-pre-null-0.82.2.realm", Realm.DEFAULT_REALM_NAME);
        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                RealmObjectSchema objectSchema = realm.getSchema().get(StringOnly.CLASS_NAME);
                objectSchema.setRequired(StringOnly.FIELD_CHARS, false);
                migrationCalled.set(true);
            }
        };

        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schemaVersion(1)
                .schema(StringOnly.class)
                .migration(migration)
                .build();
        Realm realm = Realm.getInstance(realmConfig);
        assertTrue(migrationCalled.get());

        StringOnly stringOnly = realm.where(StringOnly.class).findFirst();
        assertNotNull(stringOnly);
        // This object was created with 0.82.2
        assertEquals("String_set_with_0.82.2", stringOnly.getChars());
        realm.beginTransaction();
        stringOnly.setChars(null);
        realm.commitTransaction();
        realm.close();
    }

    // Pre-null Realms will leave columns not-nullable after the underlying storage engine has
    // migrated the file format. If the user adds the @Required annotation to a field and does not
    // change the schema version, no migration is needed. But then, null cannot be used as a value.
    @Test
    public void openPreNullWithRequired() throws IOException {
        configFactory.copyRealmFromAssets(context, "string-only-required-pre-null-0.82.2.realm", Realm.DEFAULT_REALM_NAME);
        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schemaVersion(0)
                .schema(StringOnlyRequired.class)
                .build();
        Realm realm = Realm.getInstance(realmConfig);

        StringOnlyRequired stringOnlyRequired = realm.where(StringOnlyRequired.class).findFirst();
        assertNotNull(stringOnlyRequired);
        // This object was created with 0.82.2
        assertEquals("String_set_with_0.82.2", stringOnlyRequired.getChars());

        realm.beginTransaction();
        try {
            stringOnlyRequired.setChars(null);
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(),
                    CoreMatchers.containsString("Trying to set non-nullable field 'chars' to null."));
        }
        realm.cancelTransaction();

        realm.close();
    }

    // If a required field was nullable before, a RealmMigrationNeededException should be thrown.
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
                        RealmObjectSchema nullTypesSchema = realm.getSchema().get(NullTypes.CLASS_NAME);
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
                    .name(field)
                    .schema(NullTypes.class)
                    .migration(migration)
                    .build();
            Realm.deleteRealm(realmConfig);
            // Prepares the version 0 db.
            DynamicRealm dynamicRealm = DynamicRealm.getInstance(realmConfig);
            TestHelper.initNullTypesTableExcludes(dynamicRealm, field);
            dynamicRealm.close();

            try {
                realm = Realm.getInstance(realmConfig);
                fail("Failed on " + field);
            } catch (RealmMigrationNeededException e) {
                assertThat(e.getMessage(), CoreMatchers.containsString(
                        String.format(Locale.US, "Property 'NullTypes.%s' has been made required", field)));
            }
        }
    }

    // If a field is not required but was not nullable before, a RealmMigrationNeededException should be thrown.
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
                        RealmObjectSchema nullTypesSchema = realm.getSchema().get(NullTypes.CLASS_NAME);
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
                    .name(field)
                    .schema(NullTypes.class)
                    .migration(migration)
                    .build();
            Realm.deleteRealm(realmConfig);
            // Prepares the version 0 db.
            DynamicRealm dynamicRealm = DynamicRealm.getInstance(realmConfig);
            TestHelper.initNullTypesTableExcludes(dynamicRealm, field);
            dynamicRealm.close();

            try {
                realm = Realm.getInstance(realmConfig);
                fail("Failed on " + field);
            } catch (RealmMigrationNeededException e) {
                assertThat(e.getMessage(), CoreMatchers.containsString(
                        String.format(Locale.US, "Property 'NullTypes.%s' has been made optional", field)));
            }
        }
    }

    // Tests older Realms for setting Boxed type primary keys fields nullable in migration process to support Realm Version 0.89+.
    @Test
    public void settingNullableToPrimaryKey() throws IOException {
        final long SCHEMA_VERSION = 67;
        final Class[] classes = {PrimaryKeyAsBoxedByte.class, PrimaryKeyAsBoxedShort.class, PrimaryKeyAsBoxedInteger.class, PrimaryKeyAsBoxedLong.class, PrimaryKeyAsString.class};
        for (final Class clazz : classes) {
            final AtomicBoolean didMigrate = new AtomicBoolean(false);
            RealmMigration migration = new RealmMigration() {
                @Override
                public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                    RealmObjectSchema schema = realm.getSchema().get(clazz.getSimpleName());
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
            RealmObjectSchema schema = realm.getSchema().get(clazz.getSimpleName());
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

    // Not-setting older boxed type PrimaryKey field nullable to see if migration fails in order to support Realm version 0.89+.
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
                                // Intentionally lefts empty to preserve not-nullablility of PrimaryKey on old schema.
                            }
                        })
                        .build();
                Realm realm = Realm.getInstance(realmConfig);
                realm.close();
                fail();
            } catch (RealmMigrationNeededException expected) {
                String pkFieldName = "id";
                if (clazz == PrimaryKeyAsString.class) {
                    pkFieldName = "name";
                }
                assertThat(expected.getMessage(), CoreMatchers.containsString(String.format(Locale.US,
                        "Property '%s.%s' has been made optional", clazz.getSimpleName(), pkFieldName)));
            }
        }
    }

    // Migrates a nullable field containing null value to non-nullable PrimaryKey field throws Realm version 0.89+.
    @Test
    public void migrating_nullableField_toward_notNullable_PrimaryKeyThrows() throws IOException {
        configFactory.copyRealmFromAssets(context, "default-nullable-primarykey.realm", Realm.DEFAULT_REALM_NAME);
        final Class[] classes = {PrimaryKeyAsByte.class, PrimaryKeyAsShort.class, PrimaryKeyAsInteger.class, PrimaryKeyAsLong.class};
        for (final Class clazz : classes) {
            try {
                RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                        .schema(clazz)
                        .build();
                Realm realm = Realm.getInstance(realmConfig);
                realm.close();
                fail();
            } catch (RealmMigrationNeededException expected) {
                assertThat(expected.getMessage(), CoreMatchers.containsString(
                        String.format("Property '%s.%s' has been made required", clazz.getSimpleName(), "id")));
            }
        }
    }

    @Test
    public void realmOpenBeforeMigrationThrows() throws FileNotFoundException {
        RealmConfiguration config = configFactory.createConfiguration();
        realm = Realm.getInstance(config);

        try {
            // Triggers manual migration. This can potentially change the schema, so should only be allowed when
            // no-one else is working on the Realm.
            Realm.migrateRealm(config, new RealmMigration() {
                @Override
                public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                    // Does nothing.
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

    @Test
    public void renameAndAddField() {
        final Class<MigrationFieldRenameAndAdd> schemaClass = MigrationFieldRenameAndAdd.class;

        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                realm.getSchema().get(schemaClass.getSimpleName())
                        .renameField("string1", "string2")
                        .addField("string1", String.class);
            }
        };

        RealmConfiguration config = configFactory.createConfigurationBuilder()
                .schema(schemaClass)
                .schemaVersion(2)
                .migration(migration)
                .assetFile("rename-and-add.realm")
                .build();
        Realm realm = Realm.getInstance(config);

        RealmObjectSchema schema = realm.getSchema().get(schemaClass.getSimpleName());
        assertTrue(schema.hasField("string1"));
        assertTrue(schema.hasField("string2"));
        realm.close();
    }

    @Test
    public void renameAndAddIndexedField() {
        final Class<MigrationIndexedFieldRenamed> schemaClass = MigrationIndexedFieldRenamed.class;
        final int oldTestVal = 7;
        final Long testVal = Long.valueOf(293);

        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                realm.getSchema().get(schemaClass.getSimpleName())
                        .renameField("testField", "oldTestField")
                        .addField("testField", Long.class);
            }
        };

        RealmConfiguration config = configFactory.createConfigurationBuilder()
                .schema(schemaClass)
                .schemaVersion(2)
                .migration(migration)
                .assetFile("rename-and-add-indexed.realm")
                .build();
        realm = Realm.getInstance(config);

        realm.beginTransaction();
        MigrationIndexedFieldRenamed obj = realm.createObject(schemaClass, 2);
        obj.oldTestField = oldTestVal;
        obj.testField = testVal;
        realm.commitTransaction();

        RealmObjectSchema schema = realm.getSchema().get(schemaClass.getSimpleName());
        assertTrue(schema.hasField("testField"));
        assertTrue(schema.hasField("oldTestField"));
        assertTrue(schema.hasIndex("oldTestField"));

        RealmResults<MigrationIndexedFieldRenamed> result = realm.where(schemaClass).equalTo("id", 2).findAll();
        assertEquals("There should be an object with PK=2", 1, result.size());
        assertEquals("Unexpected oldTestField value", oldTestVal, result.first().oldTestField);
        assertEquals("Unexpected testField value", testVal, result.first().testField);

        realm.close();
    }

    // Tests that if a migration is required and no migration block was provided, then the
    // original RealmMigrationNeededException is thrown instead of IllegalArgumentException
    @Test
    public void migrationRequired_throwsOriginalException() {
        RealmConfiguration config = configFactory.createConfigurationBuilder()
                // .migration() No migration block provided, but one is required
                .assetFile("default0.realm") // This Realm does not have the correct schema
                .build();

        Realm realm = null;
        try {
            realm = Realm.getInstance(config);
            fail();
        } catch (RealmMigrationNeededException ignored) {
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    private void createEmptyRealmVersion0(RealmConfiguration configuration)  {
        assertFalse(new File(configuration.getPath()).exists());

        DynamicRealm realm = DynamicRealm.getInstance(configuration);
        realm.beginTransaction();
        realm.setVersion(0);
        realm.commitTransaction();
        realm.close();
    }

    @Test
    public void migrationRequired_throwsExceptionInTheMigrationBlock() {
        final RuntimeException exception = new RuntimeException("TEST");

        RealmMigration migration = new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                // The transaction should be canceled and this model should not be created.
                RealmObjectSchema objectSchema = realm.getSchema().create(StringOnly.CLASS_NAME);
                objectSchema.addField(StringOnly.FIELD_CHARS, String.class);
                throw exception;
            }
        };
        RealmConfiguration config = configFactory.createConfigurationBuilder()
                .migration(migration)
                .schemaVersion(1)
                .schema(StringOnly.class)
                .build();
        createEmptyRealmVersion0(config);

        Realm realm = null;
        try {
            realm = Realm.getInstance(config);
            fail();
        } catch (RuntimeException expected) {
            assertSame(exception, expected);
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        DynamicRealm dynamicRealm = DynamicRealm.getInstance(config);
        try {
            assertEquals(0, dynamicRealm.getVersion());
            assertNull(dynamicRealm.getSchema().get(StringOnly.CLASS_NAME));
        } finally {
            dynamicRealm.close();
        }
    }

    // TODO Add unit tests for default nullability
    // TODO Add unit tests for default Indexing for Primary keys
}
