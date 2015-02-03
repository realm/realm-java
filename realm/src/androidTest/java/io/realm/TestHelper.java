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

package io.realm;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.realm.entities.AllTypes;

public class TestHelper {
    static void populateForMultiSort(Realm testRealm) {
        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        AllTypes object1 = testRealm.createObject(AllTypes.class);
        object1.setColumnLong(5);
        object1.setColumnString("Adam");

        AllTypes object2 = testRealm.createObject(AllTypes.class);
        object2.setColumnLong(4);
        object2.setColumnString("Brian");

        AllTypes object3 = testRealm.createObject(AllTypes.class);
        object3.setColumnLong(4);
        object3.setColumnString("Adam");
        testRealm.commitTransaction();
    }

    // Copies a Realm file from assets to app files dir
    public static void copyRealmFromAssets(Context context, String realmPath, String newName) throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream is = assetManager.open(realmPath);
        File file = new File(context.getFilesDir(), newName);
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] buf = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buf)) > -1) {
            outputStream.write(buf, 0, bytesRead);
        }
        outputStream.close();
        is.close();
    }

    // Deletes the old database and copies a new one into its place
    public static void prepareDatabaseFromAssets(Context context, String realmPath, String newName) throws IOException {
        Realm.deleteRealmFile(context, newName);
        TestHelper.copyRealmFromAssets(context, realmPath, newName);
    }
}
