package io.realm;

import android.test.AndroidTestCase;

import java.io.File;
import java.io.IOException;

import io.realm.entities.AllTypes;
import io.realm.entities.AnnotationTypes;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnType;
import io.realm.internal.Table;

public class RealmMigrationTests extends AndroidTestCase {

    public Realm realm;

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

    public void testNotSettingIndexThrows() {
        Realm.setSchema(AnnotationTypes.class);
        Realm.migrateRealmAtPath(new File(getContext().getFilesDir(), "default.realm").getAbsolutePath(), new RealmMigration() {
            @Override
            public long execute(Realm realm, long version) {
                Table table = realm.getTable(AnnotationTypes.class);
                table.addColumn(ColumnType.INTEGER, "id");
                table.setPrimaryKey("id");
                table.addColumn(ColumnType.STRING, "indexString");
                table.addColumn(ColumnType.STRING, "notIndexString");
                // Forget to set @Index
                return 1;
            }
        });

        try {
            realm = Realm.getInstance(getContext());
            fail();
        } catch (RealmMigrationNeededException expected) {
        }
    }

    public void testNotSettingPrimaryKeyThrows() {
        Realm.setSchema(AnnotationTypes.class);
        Realm.migrateRealmAtPath(new File(getContext().getFilesDir(), "default.realm").getAbsolutePath(), new RealmMigration() {
            @Override
            public long execute(Realm realm, long version) {
                Table table = realm.getTable(AnnotationTypes.class);
                table.addColumn(ColumnType.INTEGER, "id");
                // Forget to set @PrimaryKey
                long columnIndex = table.addColumn(ColumnType.STRING, "indexString");
                table.setIndex(columnIndex);
                table.addColumn(ColumnType.STRING, "notIndexString");
                return 1;
            }
        });

        try {
            realm = Realm.getInstance(getContext());
            fail();
        } catch (RealmMigrationNeededException expected) {
        }
    }

    public void testSetAnnotations() {
        Realm.setSchema(AnnotationTypes.class);
        Realm.migrateRealmAtPath(new File(getContext().getFilesDir(), "default.realm").getAbsolutePath(), new RealmMigration() {
            @Override
            public long execute(Realm realm, long version) {
                Table table = realm.getTable(AnnotationTypes.class);
                table.addColumn(ColumnType.INTEGER, "id");
                table.setPrimaryKey("id");
                long columnIndex = table.addColumn(ColumnType.STRING, "indexString");
                table.setIndex(columnIndex);
                table.addColumn(ColumnType.STRING, "notIndexString");
                return 1;
            }
        });

        realm = Realm.getInstance(getContext());
        Table table = realm.getTable(AnnotationTypes.class);
        assertEquals(3, table.getColumnCount());
        assertTrue(table.hasPrimaryKey());
        assertTrue(table.hasIndex(table.getColumnIndex("indexString")));
    }
}
