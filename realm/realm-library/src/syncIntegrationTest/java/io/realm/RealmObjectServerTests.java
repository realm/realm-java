/*
 * Copyright 2017 Realm Inc.
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

import android.support.test.runner.AndroidJUnit4;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.realm.entities.StringOnly;
import io.realm.exceptions.IncompatibleSyncedFileException;
import io.realm.exceptions.RealmFileException;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.StringOnlyModule;
import io.realm.rule.RunTestInLooperThread;
import io.realm.util.SyncTestUtils;
import okhttp3.internal.Util;

import static io.realm.util.SyncTestUtils.createTestUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Catch all class for tests that not naturally fit anywhere else.
 */
@RunWith(AndroidJUnit4.class)
public class RealmObjectServerTests extends StandardIntegrationTest {

    @Test
    @RunTestInLooperThread
    public void offlineClientReset() throws IOException {
        // Open a 2.x file to work out the location of the file
        // get the abs path
        // copy & rename the asset file to abs location
        // using the previous SyncConf open the Realm again, now it should throw

        final SyncUser user = SyncTestUtils.createTestUser(Constants.AUTH_URL);
        SyncConfiguration config = new SyncConfiguration.Builder(user, Constants.USER_REALM)
                .modules(new StringOnlyModule())
                .build();
        String path = config.getPath();
        File realmFile = new File (path);
        assertFalse(realmFile.exists());
        // copy the 1.x Realm
        copyFile("sync-1.x.realm", new File(path));
        assertTrue(realmFile.exists());

        // open the file using the new ROS 2.x server
        try {
            Realm.getInstance(config);
            fail("should throw IncompatibleSyncedFileException");
        } catch (IncompatibleSyncedFileException expected) {
            String recoveryPath = expected.getRecoveryPath();
            assertTrue(new File(recoveryPath).exists());
            // can open the backup Realm
            RealmConfiguration backupRealmConfiguration = expected.getBackupRealmConfiguration(null, new StringOnlyModule());
            Realm backupRealm = Realm.getInstance(backupRealmConfiguration);
            assertFalse(backupRealm.isEmpty());
            RealmResults<StringOnly> all = backupRealm.where(StringOnly.class).findAll();
            assertEquals(1, all.size());
            assertEquals("Hello from ROS 1.X", all.get(0).getChars());

            // make sure it's read only
            try {
                backupRealm.beginTransaction();
                fail("Backup Realm should be read-only, we should throw");
            } catch (IllegalStateException e) {
            }
            backupRealm.close();

            // we can open in dynamic mode
            DynamicRealm dynamicRealm = DynamicRealm.getInstance(backupRealmConfiguration);
            dynamicRealm.getSchema().checkHasTable(StringOnly.CLASS_NAME, "Dynamic Realm should contains " + StringOnly.CLASS_NAME);
            RealmResults<DynamicRealmObject> allDynamic = dynamicRealm.where(StringOnly.CLASS_NAME).findAll();
            assertEquals(1, allDynamic.size());
            assertEquals("Hello from ROS 1.X", allDynamic.first().getString(StringOnly.FIELD_CHARS));
            dynamicRealm.close();
        }

        Realm realm = Realm.getInstance(config);
        assertTrue(realm.isEmpty());

        looperThread.testComplete();
    }

    private static void copyFile(String assetFileName, File file) throws IOException {
        try (InputStream inputStream = BaseRealm.applicationContext.getAssets().open(assetFileName);
             FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] buf = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > -1) {
                outputStream.write(buf, 0, bytesRead);
            }
        }
    }
}
