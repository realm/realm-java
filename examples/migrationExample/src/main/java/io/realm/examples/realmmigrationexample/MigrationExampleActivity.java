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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.realm.Realm;
import io.realm.examples.realmmigrationexample.model.Migration;
import io.realm.examples.realmmigrationexample.model.Person;
import io.realm.exceptions.RealmMigrationNeededException;

/*
** This example demonstrates how you can migrate your data through different updates
** of your models.
*/
public class MigrationExampleActivity extends Activity {

    public static final String TAG = MigrationExampleActivity.class.getName();

    private LinearLayout rootLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_basic_example);

        rootLayout = ((LinearLayout) findViewById(R.id.container));
        rootLayout.removeAllViews();

        // 3 versions of the databases for testing. Normally you would only have one.
        String path3 = copyBundledRealmFile(this.getResources().openRawResource(R.raw.default0), "default0");
        String path1 = copyBundledRealmFile(this.getResources().openRawResource(R.raw.default1), "default1");
        String path2 = copyBundledRealmFile(this.getResources().openRawResource(R.raw.default2), "default2");

        // If you try to open a file that doesn't match your model an exception is thrown:
        try {
            // should throw as migration is required
            Realm.getInstance(this, "default1");
        } catch (RealmMigrationNeededException ex) {
            Log.i(TAG, "Excellent! This is expected.");
        }

        // So you migrate your data
        Realm.migrateRealmAtPath(path1, new Migration());
        Realm realm1 = Realm.getInstance(this, "default1");
        showStatus(realm1);
        realm1.close();

        // Another migration test
        Realm.migrateRealmAtPath(path2, new Migration());
        Realm realm2 = Realm.getInstance(this, "default2");
        showStatus(realm2);
        realm2.close();

        // and a third:
        Realm.migrateRealmAtPath(path3, new Migration());
        Realm realm3 = Realm.getInstance(this, "default0");
        showStatus(realm3);
        realm3.close();
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
        for (Person person : realm.allObjects(Person.class)) {
            stringBuilder.append(person.toString()).append("\n");
        }
        return stringBuilder.toString();
    }

    private void showStatus(Realm realm) {
        String txt = realmString(realm);
        Log.i(TAG, txt);
        TextView tv = new TextView(this);
        tv.setText(txt);
        rootLayout.addView(tv);
    }
}
