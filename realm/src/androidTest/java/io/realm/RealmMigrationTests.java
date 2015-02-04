package io.realm;

import android.content.res.AssetManager;
import android.test.AndroidTestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.realm.entities.AllTypes;
import io.realm.exceptions.RealmMigrationNeededException;

public class RealmMigrationTests extends AndroidTestCase {

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
}
