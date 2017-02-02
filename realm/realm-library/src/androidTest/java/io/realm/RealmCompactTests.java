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
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import io.realm.entities.StringOnly;

import static android.support.test.InstrumentationRegistry.getContext;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmCompactTests {
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getContext();
        Realm.init(getContext());
    }

    @After
    public void tearDown() {
    }

    @Test
    public void compactExternalRealm() {
        File directory = context.getExternalFilesDir(null);
        directory.mkdirs();
        assertTrue(directory.exists());

        RealmConfiguration config
            = new RealmConfiguration.Builder().directory(directory).name("somerealm.realm").build();
        Realm realm = Realm.getInstance(config);

        File realmFile = new File(config.getPath());
        assertTrue(realmFile.exists() && realmFile.canWrite());

        StringOnly test = new StringOnly();
        test.setChars("na na na na, na na na na, hey hey, goodbye");

        realm.beginTransaction();
        try {
            realm.copyToRealm(test);
            realm.commitTransaction();
            assertTrue(realm.where(StringOnly.class).count() > 0);

            realm.close();

            assertTrue(Realm.compactRealm(config));
        }
        catch (Exception e) {
            realm.cancelTransaction();
            realm.close();

            fail();
        }
        finally {
            Realm.deleteRealm(config);
        }
    }
}
