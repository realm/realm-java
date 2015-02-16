package io.realm;

import android.content.res.AssetManager;
import android.test.AndroidTestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.realm.entities.AllTypes;
import io.realm.entities.MigrationFieldInMiddle;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnType;
import io.realm.internal.Table;

public class RealmMigrationTests extends AndroidTestCase {

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Realm.setSchema();
    }

    // Copies a Realm file from assets to app files dir
    private void copyRealmFromAssets(String fileName) throws IOException {
        AssetManager assetManager = getContext().getAssets();
        InputStream is = assetManager.open(fileName);
        File file = new File(getContext().getFilesDir(), fileName);
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] buf = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buf)) > -1) {
            outputStream.write(buf, 0, bytesRead);
        }
        outputStream.close();
        is.close();
    }

    public void testRealmClosedAfterMigrationException() throws IOException {
        String REALM_NAME = "default0.realm";
        Realm.deleteRealmFile(getContext(), REALM_NAME);
        copyRealmFromAssets(REALM_NAME);
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

    public void testRemovingMiddleField() throws IOException {
        String REALM = "field_removed_migration.realm";
        Realm.deleteRealmFile(getContext(), REALM);
        copyRealmFromAssets(REALM);
        Realm.setSchema(MigrationFieldInMiddle.class);
        Realm.migrateRealmAtPath(new File(getContext().getFilesDir(), REALM).getAbsolutePath(), new RealmMigration() {
            @Override
            public long execute(Realm realm, long version) {
                Table table = realm.getTable(MigrationFieldInMiddle.class);
                table.removeColumn(table.getColumnIndex("secondField"));
                return 1;
            }
        });

        Realm realm = null;
        try {
            realm = Realm.getInstance(getContext(), REALM);
            RealmResults<MigrationFieldInMiddle> results = realm.allObjects(MigrationFieldInMiddle.class).where().equalTo("thirdField", "Foo").findAll();
            assertEquals(0, results.size());
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    public void testAddingMiddleField() throws IOException {
        String REALM = "field_added_migration.realm";
        Realm.deleteRealmFile(getContext(), REALM);
        copyRealmFromAssets(REALM);
        Realm.setSchema(MigrationAddedFieldInMiddle.class);
        Realm.migrateRealmAtPath(new File(getContext().getFilesDir(), REALM).getAbsolutePath(), new RealmMigration() {
            @Override
            public long execute(Realm realm, long version) {
                Table table = realm.getTable(MigrationAddedFieldInMiddle.class);
                table.addColumn(ColumnType.INTEGER, "newField");
                return 1;
            }
        });

        Realm realm = null;
        try {
            realm = Realm.getInstance(getContext(), REALM);
            RealmResults<MigrationAddedFieldInMiddle> results = realm.where(MigrationAddedFieldInMiddle.class).equalTo("thirdField", "Foo").findAll();
            assertEquals(0, results.size());
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }
}
