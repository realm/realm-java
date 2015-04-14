package io.realm;

import android.test.AndroidTestCase;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import io.realm.dynamic.DynamicRealmObject;
import io.realm.dynamic.RealmModifier;
import io.realm.dynamic.RealmSchema;
import io.realm.entities.AllTypes;
import io.realm.entities.Cat;
import io.realm.entities.Dog;
import io.realm.entities.DogPrimaryKey;
import io.realm.entities.Owner;
import io.realm.entities.OwnerPrimaryKey;
import io.realm.exceptions.RealmException;
import io.realm.entities.FieldOrder;
import io.realm.entities.AnnotationTypes;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnType;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.SharedGroup;
import io.realm.internal.Table;

public class RealmMigrationTests extends AndroidTestCase {

    private ImplicitTransaction realm;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Realm.setSchema(null);
        Realm.deleteRealmFile(getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (realm != null) {
            realm.close();
        }
    }

    private ImplicitTransaction getDefaultSharedGroup() {
        String path = new File(getContext().getFilesDir(), "default.realm").getAbsolutePath();
        SharedGroup sharedGroup = new SharedGroup(path, SharedGroup.Durability.FULL, null);
        return sharedGroup.beginImplicitTransaction();
    }

    private void assertColumn(Table table, String columnName, int columnIndex, ColumnType columnType) {
        long index = table.getColumnIndex(columnName);
        assertEquals(columnIndex, index);
        assertEquals(columnType, table.getColumnType(index));
    }

    public void testRealmClosedAfterMigrationException() throws IOException {
        String REALM_NAME = "default0.realm";
        Realm.deleteRealmFile(getContext(), REALM_NAME);
        TestHelper.copyRealmFromAssets(getContext(), REALM_NAME, REALM_NAME);
        try {
            Realm.getInstance(getContext(), REALM_NAME);
            fail("A migration should be triggered");
        } catch (RealmMigrationNeededException expected) {
            Realm.deleteRealmFile(getContext(), REALM_NAME); // Delete old realm
        }

        // This should recreate the Realm with proper schema
        Realm realm = Realm.getInstance(getContext(), REALM_NAME);
        int result = realm.where(AllTypes.class).equalTo("columnString", "Foo").findAll().size();
        assertEquals(0, result);
    }

    // Create a Realm file with no Realm classes
    private void createDefaultRealm() {
        Realm.setSchema(AllTypes.class);
        Realm.deleteRealmFile(getContext());
        Realm realm = Realm.getInstance(getContext());
        realm.close();
    }

    private void createSimpleRealm() {
        Realm.setSchema(Owner.class, Dog.class, Cat.class);
        Realm.deleteRealmFile(getContext());
        Realm realm = Realm.getInstance(getContext());
        realm.close();
    }

    private void createSimpleRealmWithPrimaryKey() {
        Realm.setSchema(OwnerPrimaryKey.class, DogPrimaryKey.class, Dog.class);
        Realm.deleteRealmFile(getContext());
        Realm realm = Realm.getInstance(getContext());
        realm.close();
    }


    private String getDefaultRealmPath() {
        return new File(getContext().getFilesDir(), "default.realm").getAbsolutePath();
    }

    public void testAddEmptyClassThrows() {
        createDefaultRealm();
        expectIllegalArgumentException(new MigrationBlock() {
            @Override
            public void migrationCode(RealmSchema schema) {
                schema.addClass(null);
            }
        });
    }

    public void testAddExistingClassThrows() {
        createDefaultRealm();
        expectIllegalArgumentException(new MigrationBlock() {
            @Override
            public void migrationCode(RealmSchema schema) {
                schema.addClass("AllTypes");
            }
        });
    }

    public void testAddClass() {
        createDefaultRealm();
        Realm.migrateRealmAtPath(getDefaultRealmPath(), new RealmMigration() {
            @Override
            public void migrate(RealmSchema realm, long oldVersion, long newVersion) {
                realm.addClass("Foo");
            }
        });
        realm = getDefaultSharedGroup();
        assertTrue(realm.hasTable("class_Foo"));
    }

    public void testRemoveEmptyClassThrows() {
        createDefaultRealm();
        expectIllegalArgumentException(new MigrationBlock() {
            @Override
            public void migrationCode(RealmSchema schema) {
                schema.removeClass(null);
            }
        });
    }

    public void testRemoveLinkedClassThrows() {
        createSimpleRealm();
        try {
            Realm.migrateRealmAtPath(getDefaultRealmPath(), new RealmMigration() {
                @Override
                public void migrate(RealmSchema schema, long oldVersion, long newVersion) {
                    schema.removeClass("Owner");
                }
            });
            fail();
        } catch (RealmException expected) {
        }
    }

    public void testRemoveClass() {
        createDefaultRealm();
        Realm.migrateRealmAtPath(getDefaultRealmPath(), new RealmMigration() {
            @Override
            public void migrate(RealmSchema realm, long oldVersion, long newVersion) {
                realm.removeClass("AllTypes");
            }
        });
        realm = getDefaultSharedGroup();
        assertFalse(realm.hasTable("class_AllTypes"));
    }

    public void testRenameEmptyClassThrows() {
        createSimpleRealm();

        // Test from class
        expectIllegalArgumentException(new MigrationBlock() {
            @Override
            public void migrationCode(RealmSchema schema) {
                schema.renameClass(null, "Foo");
            }
        });

        // Test to class
        expectIllegalArgumentException(new MigrationBlock() {
            @Override
            public void migrationCode(RealmSchema schema) {
                schema.renameClass("Owner", null);
            }
        });
    }

    public void testRenameToExistingClassThrows() {
        createSimpleRealm();
        expectIllegalArgumentException(new MigrationBlock() {
            @Override
            public void migrationCode(RealmSchema schema) {
                schema.renameClass("Owner", "Dog");
            }
        });
    }

    public void testRenameClass() {
        createSimpleRealm();
        Realm.migrateRealmAtPath(getDefaultRealmPath(), new RealmMigration() {
            @Override
            public void migrate(RealmSchema realm, long oldVersion, long newVersion) {
                realm.renameClass("Owner", "Foo");
            }
        });
        realm = getDefaultSharedGroup();
        assertFalse(realm.hasTable("class_Owner"));
        assertTrue(realm.hasTable("class_Foo"));
    }

    public void testAddEmptyFieldThrows() {
        createDefaultRealm();
        expectIllegalArgumentException(new MigrationBlock() {
            @Override
            public void migrationCode(RealmSchema schema) {
                schema.addClass("Foo").addString(null);
            }
        });
    }

    public void testAddField() {
        createDefaultRealm();
        Realm.migrateRealmAtPath(getDefaultRealmPath(), new RealmMigration() {
            @Override
            public void migrate(RealmSchema schema, long oldVersion, long newVersion) {
                schema.addClass("Foo")
                        .addString("a")
                        .addShort("b")
                        .addInt("c")
                        .addLong("d")
                        .addBoolean("e")
                        .addFloat("f")
                        .addDouble("g")
                        .addByteArray("h")
                        .addDate("i")
                        .addObject("j", schema.getClass("AllTypes"))
                        .addList("k", schema.getClass("AllTypes"));
            }
        });

        realm = getDefaultSharedGroup();
        assertTrue(realm.hasTable("class_Foo"));
        Table table = realm.getTable("class_Foo");
        assertEquals(11, table.getColumnCount());
        assertColumn(table, "a", 0, ColumnType.STRING);
        assertColumn(table, "b", 1, ColumnType.INTEGER);
        assertColumn(table, "c", 2, ColumnType.INTEGER);
        assertColumn(table, "d", 3, ColumnType.INTEGER);
        assertColumn(table, "e", 4, ColumnType.BOOLEAN);
        assertColumn(table, "f", 5, ColumnType.FLOAT);
        assertColumn(table, "g", 6, ColumnType.DOUBLE);
        assertColumn(table, "h", 7, ColumnType.BINARY);
        assertColumn(table, "i", 8, ColumnType.DATE);
        assertColumn(table, "j", 9, ColumnType.LINK);
        assertColumn(table, "k", 10, ColumnType.LINK_LIST);
    }

    public void testAddFieldWithModifiers() {
        createDefaultRealm();
        Realm.migrateRealmAtPath(getDefaultRealmPath(), new RealmMigration() {
            @Override
            public void migrate(RealmSchema schema, long oldVersion, long newVersion) {
                schema.addClass("Foo")
                        .addString("a", EnumSet.of(RealmModifier.INDEXED))
                        .addLong("b", EnumSet.of(RealmModifier.PRIMARY_KEY))
                        .addBoolean("c", null);
            }
        });

        realm = getDefaultSharedGroup();
        assertTrue(realm.hasTable("class_Foo"));
        Table table = realm.getTable("class_Foo");
        assertEquals(3, table.getColumnCount());
        assertColumn(table, "a", 0, ColumnType.STRING);
        assertColumn(table, "b", 1, ColumnType.INTEGER);
        assertColumn(table, "c", 2, ColumnType.BOOLEAN);
        assertTrue(table.hasIndex(0));
        assertEquals(1, table.getPrimaryKey());
    }

    public void testRemoveEmptyFieldThrows() {
        createDefaultRealm();
        expectIllegalArgumentException(new MigrationBlock() {
            @Override
            public void migrationCode(RealmSchema schema) {
                schema.getClass("AllTypes").removeField(null);
            }
        });
    }

    public void testRemoveNonExistingFieldThrows() {
        createDefaultRealm();
        expectIllegalArgumentException(new MigrationBlock() {
            @Override
            public void migrationCode(RealmSchema schema) {
                schema.getClass("AllTypes").removeField("unknown");
            }
        });
    }

    public void testRemoveField() {
        createDefaultRealm();
        Realm.migrateRealmAtPath(getDefaultRealmPath(), new RealmMigration() {
            @Override
            public void migrate(RealmSchema schema, long oldVersion, long newVersion) {
                schema.getClass("AllTypes").removeField("columnString");
            }
        });
        realm = getDefaultSharedGroup();
        Table allTypesTable = realm.getTable("class_AllTypes");
        assertEquals(8, allTypesTable.getColumnCount());
        assertEquals(-1, allTypesTable.getColumnIndex("columnString"));
    }

    public void testRenameEmptyFieldThrows() {
        createDefaultRealm();

        // From field
        expectIllegalArgumentException(new MigrationBlock() {
            @Override
            public void migrationCode(RealmSchema schema) {
                schema.getClass("AllTypes").renameField(null, "Foo");
            }
        });

        // To field
        expectIllegalArgumentException(new MigrationBlock() {
            @Override
            public void migrationCode(RealmSchema schema) {
                schema.getClass("AllTypes").renameField("columnString", null);
            }
        });
    }

    public void testRenameNonExistingFieldThrows() {
        createDefaultRealm();
        try {
            Realm.migrateRealmAtPath(getDefaultRealmPath(), new RealmMigration() {
                @Override
                public void migrate(RealmSchema schema, long oldVersion, long newVersion) {
                    schema.getClass("AllTypes").renameField("foo", "bar");
                }
            });
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testRenameField() {
        createDefaultRealm();
        Realm.migrateRealmAtPath(getDefaultRealmPath(), new RealmMigration() {
            @Override
            public void migrate(RealmSchema schema, long oldVersion, long newVersion) {
                schema.getClass("AllTypes").renameField("columnString", "columnString2");
            }
        });
        realm = getDefaultSharedGroup();
        Table t = realm.getTable("class_AllTypes");
        assertEquals(9, t.getColumnCount());
        assertEquals(-1, t.getColumnIndex("columnString"));
        assertTrue(t.getColumnIndex("columnString2") != -1);
    }

    public void testAddEmptyIndexThrows() {
        createDefaultRealm();
        expectIllegalArgumentException(new MigrationBlock() {
            @Override
            public void migrationCode(RealmSchema schema) {
                schema.getClass("AllTypes").addIndex(null);
            }
        });
    }

    public void testAddNonExistingIndexThrows() {
        createDefaultRealm();
        expectIllegalArgumentException(new MigrationBlock() {
            @Override
            public void migrationCode(RealmSchema schema) {
                schema.getClass("AllTypes").addIndex("columnFoo");
            }
        });
    }

    public void testAddIllegalIndexThrows() {
        createDefaultRealm();
        expectIllegalArgumentException(new MigrationBlock() {
            @Override
            public void migrationCode(RealmSchema schema) {
                schema.getClass("AllTypes").addIndex("columnDate");
            }
        });
    }

    public void testAddIndex() {
        createDefaultRealm();
        Realm.migrateRealmAtPath(getDefaultRealmPath(), new RealmMigration() {
            @Override
            public void migrate(RealmSchema schema, long oldVersion, long newVersion) {
                schema.getClass("AllTypes").addIndex("columnString");
            }
        });
        realm = getDefaultSharedGroup();
        Table t = realm.getTable("class_AllTypes");
        assertTrue(t.hasIndex(t.getColumnIndex("columnString")));
    }

    public void testRemoveIndexEmptyFieldThrows() {
        createDefaultRealm();
        expectIllegalArgumentException(new MigrationBlock() {
            @Override
            public void migrationCode(RealmSchema schema) {
                schema.getClass("AllTypes").removeIndex(null);
            }
        });
    }

    public void testRemoveIndexNonExistingFieldThrows() {
        createDefaultRealm();
        expectIllegalArgumentException(new MigrationBlock() {
            @Override
            public void migrationCode(RealmSchema schema) {
                schema.getClass("AllTypes").removeIndex("columnFoo");
            }
        });
    }

    public void testRemoveNonExistingIndexThrows() {
        createDefaultRealm();
        expectIllegalArgumentException(new MigrationBlock() {
            @Override
            public void migrationCode(RealmSchema schema) {
                schema.getClass("AllTypes").removeIndex("columnString");
            }
        });
    }

    // TODO
    public void testRemoveIndex() {
        createDefaultRealm();
        Realm.migrateRealmAtPath(getDefaultRealmPath(), new RealmMigration() {
            @Override
            public void migrate(RealmSchema schema, long oldVersion, long newVersion) {
                try {
                    schema.getClass("Dog").removeIndex("name");
                    fail();
                } catch (RuntimeException e) {
                }
            }
        });
        realm = getDefaultSharedGroup();
        Table t = realm.getTable("class_Dog");
        assertTrue(t.hasIndex(t.getColumnIndex("name")));
    }

    public void testAddPrimaryKeyEmptyFieldThrows() {
        createDefaultRealm();
        expectIllegalArgumentException(new MigrationBlock() {
            @Override
            public void migrationCode(RealmSchema schema) {
                schema.getClass("AllTypes").addPrimaryKey(null);
            }
        });
    }

    public void testAddPrimaryKeyNonExistingFieldThrows() {
        createDefaultRealm();
        expectIllegalArgumentException(new MigrationBlock() {
            @Override
            public void migrationCode(RealmSchema schema) {
                schema.getClass("AllTypes").addPrimaryKey("foo");
            }
        });
    }

    public void testAddPrimaryKey() {
        createDefaultRealm();
        Realm.migrateRealmAtPath(getDefaultRealmPath(), new RealmMigration() {
            @Override
            public void migrate(RealmSchema schema, long oldVersion, long newVersion) {
                schema.getClass("AllTypes").addPrimaryKey("columnString");
            }
        });
        realm = getDefaultSharedGroup();
        Table t = realm.getTable("class_AllTypes");
        assertTrue(t.hasPrimaryKey());
        assertEquals(t.getColumnIndex("columnString"), t.getPrimaryKey());
    }

    public void testRemoveNonExistingPrimaryKeyThrows() {
        createDefaultRealm();
        try {
            Realm.migrateRealmAtPath(getDefaultRealmPath(), new RealmMigration() {
                @Override
                public void migrate(RealmSchema schema, long oldVersion, long newVersion) {
                    schema.getClass("AllTypes").removePrimaryKey();
                }
            });
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    public void testRemovePrimaryKey() {
        createSimpleRealmWithPrimaryKey();
        Realm.migrateRealmAtPath(getDefaultRealmPath(), new RealmMigration() {
            @Override
            public void migrate(RealmSchema schema, long oldVersion, long newVersion) {
                schema.getClass("OwnerPrimaryKey").removePrimaryKey();
            }
        });
        realm = getDefaultSharedGroup();
        Table t = realm.getTable("class_OwnerPrimaryKey");
        assertFalse(t.hasPrimaryKey());
    }

    public void testCreateObject() {
        createDefaultRealm();
        Realm.migrateRealmAtPath(getDefaultRealmPath(), new RealmMigration() {
            @Override
            public void migrate(RealmSchema schema, long oldVersion, long newVersion) {
                DynamicRealmObject obj = schema.getClass("AllTypes").createObject();
                assertNotNull(obj);
            }
        });
        realm = getDefaultSharedGroup();
        Table t = realm.getTable("class_AllTypes");
        assertEquals(1, t.size());
    }

    // If a migration creates a different ordering of columns on Realm A, while another ordering is generated by
    // creating a new Realm B. Global column indices will not work. They must be calculated for each Realm.
    public void testLocalColumnIndices() throws IOException {
        String MIGRATED_REALM = "migrated.realm";
        String NEW_REALM = "new.realm";

        // Migrate old Realm to proper schema
        Realm.deleteRealmFile(getContext(), MIGRATED_REALM);
        Realm.setSchema(AllTypes.class);
        Realm migratedRealm = Realm.getInstance(getContext(), MIGRATED_REALM);
        migratedRealm.close();
        Realm.migrateRealmAtPath(new File(getContext().getFilesDir(), MIGRATED_REALM).getAbsolutePath(), new RealmMigration() {
            @Override
            public void migrate(RealmSchema schema, long oldVersion, long newVersion) {
                schema.addClass("FieldOrder")
                        .addInt("field2")
                        .addBoolean("field1");
            }
        });

        // Open migrated Realm and populate column indices based on migration ordering.
        Realm.setSchema(AllTypes.class, FieldOrder.class);
        migratedRealm = Realm.getInstance(getContext(), MIGRATED_REALM);

        // Create new Realm which will cause column indices to be recalculated based on the order in the java file
        // instead of the migration
        Realm.deleteRealmFile(getContext(), NEW_REALM);
        Realm newRealm = Realm.getInstance(getContext(), NEW_REALM);
        newRealm.close();

        // Try to query migrated realm. With local column indices this will work. With global it will fail.
        assertEquals(0, migratedRealm.where(FieldOrder.class).equalTo("field1", true).findAll().size());
    }

    public void testNotSettingIndexThrows() {
        Realm.setSchema(AnnotationTypes.class);
        Realm.migrateRealmAtPath(getDefaultRealmPath(), new RealmMigration() {
            @Override
            public void migrate(RealmSchema schema, long oldVersion, long newVersion) {
                schema.addClass("AnnotationTypes")
                        .addInt("id", EnumSet.of(RealmModifier.PRIMARY_KEY))
                        .addString("indexString") // Forget to set @Index
                        .addString("notIndexString");
            }
        });

        try {
            Realm.getInstance(getContext());
            fail();
        } catch (RealmMigrationNeededException expected) {
        }
    }

    public void testNotSettingPrimaryKeyThrows() {
        Realm.setSchema(AnnotationTypes.class);
        Realm.migrateRealmAtPath(getDefaultRealmPath(), new RealmMigration() {
            @Override
            public void migrate(RealmSchema schema, long oldVersion, long newVersion) {
                schema.addClass("AnnotationTypes")
                        .addInt("id") // Forget to set @PrimaryKey
                        .addString("indexString", EnumSet.of(RealmModifier.INDEXED))
                        .addString("notIndexString");
            }
        });

        try {
            Realm.getInstance(getContext());
            fail();
        } catch (RealmMigrationNeededException expected) {
        }
    }

    public void testSetAnnotations() {
        Realm.setSchema(AnnotationTypes.class);
        Realm.migrateRealmAtPath(getDefaultRealmPath(), new RealmMigration() {
            @Override
            public void migrate(RealmSchema schema, long oldVersion, long newVersion) {
                schema.addClass("AnnotationTypes")
                        .addInt("id", EnumSet.of(RealmModifier.PRIMARY_KEY))
                        .addString("indexString", EnumSet.of(RealmModifier.INDEXED))
                        .addString("notIndexString");
            }
        });

        realm = getDefaultSharedGroup();
        Table table = realm.getTable("class_AnnotationTypes");
        assertEquals(3, table.getColumnCount());
        assertTrue(table.hasPrimaryKey());
        assertTrue(table.hasIndex(table.getColumnIndex("indexString")));
    }

    private void expectIllegalArgumentException(final MigrationBlock block) {
        try {
            Realm.migrateRealmAtPath(getDefaultRealmPath(), new RealmMigration() {
                @Override
                public void migrate(RealmSchema schema, long oldVersion, long newVersion) {
                    block.migrationCode(schema);
                }
            });
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public interface MigrationBlock {
        public void migrationCode(RealmSchema schema);
    }
}
