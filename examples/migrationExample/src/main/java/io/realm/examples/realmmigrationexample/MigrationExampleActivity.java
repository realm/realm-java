/*
 * Copyright 2014 Realm Inc.
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

package io.realm.examples.realmmigrationexample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.examples.realmmigrationexample.model.Migration;
import io.realm.examples.realmmigrationexample.model.Person;

/*
** This example demonstrates how you can migrate your data through different updates
** of your models.
*/
public class MigrationExampleActivity extends Activity {

    public static final String TAG = MigrationExampleActivity.class.getName();

    private LinearLayout rootLayout = null;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_migration_example);

        rootLayout = ((LinearLayout) findViewById(R.id.container));
        rootLayout.removeAllViews();

        // 3 versions of the databases for testing. Normally you would only have one.
        copyBundledRealmFile(this.getResources().openRawResource(R.raw.default0), "default0");
        copyBundledRealmFile(this.getResources().openRawResource(R.raw.default1), "default1");
        copyBundledRealmFile(this.getResources().openRawResource(R.raw.default2), "default2");

        // When you create a RealmConfiguration you can specify the version of the schema.
        // If the schema does not have that version a RealmMigrationNeededException will be thrown.
        RealmConfiguration config0 = new RealmConfiguration.Builder(this)
                .name("default0")
                .schemaVersion(3)
                .build();

        // You can then manually call Realm.migrateRealm().
        try {
            Realm.migrateRealm(config0, new Migration());
        } catch (FileNotFoundException ignored) {
            // If the Realm file doesn't exist, just ignore.
        }
        realm = Realm.getInstance(config0);
        showStatus("Default0");
        showStatus(realm);
        realm.close();

        // Or you can add the migration code to the configuration. This will run the migration code without throwing
        // a RealmMigrationNeededException.
        RealmConfiguration config1 = new RealmConfiguration.Builder(this)
                .name("default1")
                .schemaVersion(3)
                .migration(new Migration())
                .build();

        realm = Realm.getInstance(config1); // Automatically run migration if needed
        showStatus("Default1");
        showStatus(realm);
        realm.close();

        // or you can set .deleteRealmIfMigrationNeeded() if you don't want to bother with migrations.
        // WARNING: This will delete all data in the Realm though.
        RealmConfiguration config2 = new RealmConfiguration.Builder(this)
                .name("default2")
                .schemaVersion(3)
                .deleteRealmIfMigrationNeeded()
                .build();

        realm = Realm.getInstance(config2);
        showStatus("default2");
        showStatus(realm);
        realm.close();
    }

    private String copyBundledRealmFile(InputStream inputStream, String outFileName) {
        try {
            File file = new File(this.getFilesDir(), outFileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
            outputStream.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String realmString(Realm realm) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Person person : realm.where(Person.class).findAll()) {
            stringBuilder.append(person.toString()).append("\n");
        }

        return (stringBuilder.length() == 0) ? "<data was deleted>" : stringBuilder.toString();
    }

    private void showStatus(Realm realm) {
        showStatus(realmString(realm));
    }

    private void showStatus(String txt) {
        Log.i(TAG, txt);
        TextView tv = new TextView(this);
        tv.setText(txt);
        rootLayout.addView(tv);
    }
}
