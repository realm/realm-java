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

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.entities.StringOnly;

@RunWith(AndroidJUnit4.class)
public class MultipleThreads {

    @Before
    public void setup() {
        Realm.init(InstrumentationRegistry.getTargetContext());
    }

    @After
    public void teardown() {
    }

    // Stress testing multiple readers/writers on threads
    // Related to https://github.com/realm/realm-core/issues/2383 and https://github.com/realm/realm-java/issues/2567
    @Test
    public void multipleReadersAndWriters() {
        final int nReaders = 10;
        final int nWriters = 10;
        final int nDeleters = 2;

        final String filename = "multiple-threads.realm";

        final RealmConfiguration realmConfig = new RealmConfiguration.Builder()
                .name(filename)
                .deleteRealmIfMigrationNeeded()
                .schema(StringOnly.class)
                .build();
        Realm r = Realm.getInstance(realmConfig);

        for(int i = 0; i < nReaders; i++) {
            Log.d("REALM", "creating reader " + i);
            Runnable reader = new Runnable() {
                @Override
                public void run() {
                    Realm r = Realm.getInstance(realmConfig);
                    while (true) {
                        RealmResults<StringOnly> stringOnlies = r.where(StringOnly.class).findAll();
                        try {
                            Thread.sleep((int)(Math.random()*200.0));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            Thread thread = new Thread(reader);
            thread.start();
        }

        for(int i = 0; i < nWriters; i++) {
            Log.d("REALM", "creating writer " + i);
            Runnable writer = new Runnable() {
                @Override
                public void run() {
                    Realm r = Realm.getInstance(realmConfig);
                    while (true) {
                        r.beginTransaction();
                        StringOnly stringOnly = r.createObject(StringOnly.class);
                        stringOnly.setChars("Smølf");
                        r.commitTransaction();
                        try {
                            Thread.sleep((int)(Math.random()*200.0));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            Thread thread = new Thread(writer);
            thread.start();
        }

        for(int i = 0; i < nDeleters; i++) {
            Log.d("REALM", "creating deleter " + i);
            Runnable deleter = new Runnable() {
                @Override
                public void run() {
                    Realm r = Realm.getInstance(realmConfig);
                    while (true) {
                        r.beginTransaction();
                        r.where(StringOnly.class).findAll().deleteAllFromRealm();
                        r.commitTransaction();
                        try {
                            Thread.sleep((int) (Math.random() * 200.0));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            Thread thread = new Thread(deleter);
            thread.start();
        }

        while (true) {
            try {
                Thread.sleep(5000);
                Log.d("REALM", "sleeping");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
