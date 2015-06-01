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

package io.realm.examples.encryptionexample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.security.SecureRandom;

import io.realm.Realm;

public class EncryptionExampleActivity extends Activity {

    public static final String TAG = EncryptionExampleActivity.class.getName();

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start with a clean slate every time
        Realm.deleteRealmFile(this);

        // Generate a key
        // IMPORTANT! This is a silly way to generate a key. It is also never stored.
        // For proper key handling please consult:
        // * https://developer.android.com/training/articles/keystore.html
        // * http://nelenkov.blogspot.dk/2012/05/storing-application-secrets-in-androids.html
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);

        // Open the Realm with encryption enabled
        realm = Realm.getInstance(this, key);


        // Everything continues to work as normal except for that the file is encrypted on disk
        realm.beginTransaction();
        Person person = realm.createObject(Person.class);
        person.setName("Happy Person");
        person.setAge(14);
        realm.commitTransaction();

        person = realm.where(Person.class).findFirst();
        Log.i(TAG, String.format("Person name: %s", person.getName()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close(); // Remember to close Realm when done.
    }
}
