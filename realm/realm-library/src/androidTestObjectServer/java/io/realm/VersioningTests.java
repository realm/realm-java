/*
 * Copyright 2016 Realm Inc.
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.realm.entities.Cat;
import io.realm.entities.Owner;
import io.realm.rule.TestSyncConfigurationFactory;
import io.realm.util.SyncTestUtils;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class VersioningTests {
    private Realm realm;

    @Rule
    public final TestSyncConfigurationFactory syncFactory = new TestSyncConfigurationFactory();

    @After
    public void tearDown() throws Exception {
        if (realm != null) {
            realm.close();
        }
    }

    @Test
    public void disallow_olderVersion() throws IOException {
        final long schemaVersion = 97;

        // open version 97
        SyncUser user = SyncTestUtils.createTestUser();
        SyncConfiguration config = syncFactory.createConfigurationBuilder(
                SyncTestUtils.createTestUser(),
                "realm://objectserver.realm.io/~/default")
                .name("versionTest.realm")
                .schemaVersion(schemaVersion)
                .build();
        Realm realm = Realm.getInstance(config);
        realm.beginTransaction();

        // begin version 2
        Owner owner = realm.createObject(Owner.class);
        owner.setName("blake");
        // end version 2

        Cat cat = realm.createObject(Cat.class);
        cat.setName("susuwatari");
        cat.setOwner(owner);

        realm.commitTransaction();
        realm.close();

        // Replace it with version 2
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        syncFactory.copyRealmFromAssets(context, "versionTest.realm", config);

        // Re-open.  Should still be version 97
        realm = Realm.getInstance(config);
        assertEquals(schemaVersion, realm.getVersion());
    }
}
