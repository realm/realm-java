/*
 * Copyright 2018 Realm Inc.
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

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.objectserver.utils.Constants;
import io.realm.rule.RunInLooperThread;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Testing sync specific methods on {@link Realm}.
 */
@RunWith(AndroidJUnit4.class)
public class SyncedRealmTests {

    @Rule
    public final TestSyncConfigurationFactory configFactory = new TestSyncConfigurationFactory();

    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Realm realm;

    @After
    public void tearDown() {
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
//        for (RealmUser user : RealmApp.allUsers().values()) {
//            RealmApp.logout(user);
//        }
    }

    private Realm getNormalRealm() {
        RealmConfiguration config = configFactory.createConfiguration();
        realm = Realm.getInstance(config);
        return realm;
    }

    private Realm getFullySyncRealm() {
        SyncConfiguration config = configFactory.createSyncConfigurationBuilder(SyncTestUtils.createTestUser(), "http://foo.com/fullsync")
                .build();
        realm = Realm.getInstance(config);
        return realm;
    }

    // Test for https://github.com/realm/realm-java/issues/6619
    @Test
    @Ignore("Going to be removed anyway")
    public void testUpgradingOptionalSubscriptionFields() throws IOException {
        SyncUser user = SyncTestUtils.createTestUser();

        // Put an older Realm at the location where Realm would otherwise create a new empty one.
        // This way, Realm will upgrade this file instead.
        // We don't need to synchronize data with the server, so any errors due to missing
        // server side files are ignored.
        // The file was created using Realm Java 5.10.0
        SyncConfiguration config = configFactory.createSyncConfigurationBuilder(user, "realm://127.0.0.1:9080/optionalsubscriptionfields").build();
        File realmDir = config.getRealmDirectory();
        File oldRealmFile = new File(realmDir, "optionalsubscriptionfields");
        assertFalse(oldRealmFile.exists());
        configFactory.copyFileFromAssets(InstrumentationRegistry.getTargetContext().getApplicationContext(), "optionalsubscriptionfields.realm", oldRealmFile);
        assertTrue(oldRealmFile.exists());

        try {
            // Opening the Realm should not throw a schema mismatch
            realm = Realm.getInstance(config);

            // Verify that createdAt/updatedAt are still optional even though the Java model class
            // says they should be required.
            assertTrue(realm.getSchema().get("__ResultSets").isNullable("created_at"));
            assertTrue(realm.getSchema().get("__ResultSets").isNullable("updated_at"));
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void compactRealm_populatedRealm() {
        SyncConfiguration config = configFactory.createSyncConfigurationBuilder(SyncTestUtils.createTestUser(), Constants.DEFAULT_REALM).build();
        realm = Realm.getInstance(config);
        realm.executeTransaction(r -> {
            for (int i = 0; i < 10; i++) {
                r.insert(new AllJavaTypes(i));
            }
        });
        realm.close();
        assertTrue(Realm.compactRealm(config));
        realm = Realm.getInstance(config);
        assertEquals(10, realm.where(AllJavaTypes.class).count());
    }

    @Test
    public void compactOnLaunch_shouldCompact() throws IOException {
        SyncUser user = SyncTestUtils.createTestUser();

        // Fill Realm with data and record size
        SyncConfiguration config1 = configFactory.createSyncConfigurationBuilder(user, Constants.DEFAULT_REALM).build();
        realm = Realm.getInstance(config1);
        byte[] oneMBData = new byte[1024 * 1024];
        realm.beginTransaction();
        for (int i = 0; i < 10; i++) {
            realm.createObject(AllTypes.class).setColumnBinary(oneMBData);
        }
        realm.commitTransaction();
        realm.close();
        long originalSize = new File(realm.getPath()).length();

        // Open Realm with CompactOnLaunch
        SyncConfiguration config2 = configFactory.createSyncConfigurationBuilder(user, Constants.DEFAULT_REALM)
                .compactOnLaunch(new CompactOnLaunchCallback() {
                    @Override
                    public boolean shouldCompact(long totalBytes, long usedBytes) {
                        return true;
                    }
                })
                .build();
        realm = Realm.getInstance(config2);
        realm.close();
        long compactedSize = new File(realm.getPath()).length();

        assertTrue(originalSize > compactedSize);
    }

}
