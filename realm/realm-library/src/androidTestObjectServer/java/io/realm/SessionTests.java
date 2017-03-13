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

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;
import io.realm.rule.TestSyncConfigurationFactory;
import io.realm.util.SyncTestUtils;

import static io.realm.util.SyncTestUtils.createTestUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class SessionTests {

    private static String REALM_URI = "realm://objectserver.realm.io/~/default";

    private SyncConfiguration configuration;
    private SyncUser user;

    @Rule
    public final TestSyncConfigurationFactory configFactory = new TestSyncConfigurationFactory();

    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    @Before
    public void setUp() {
        user = createTestUser();
        configuration = new SyncConfiguration.Builder(user, REALM_URI).build();
    }

    @Test
    public void get_syncValues() {
        SyncSession session = new SyncSession(configuration);
        assertEquals("realm://objectserver.realm.io/JohnDoe/default", session.getServerUrl().toString());
        assertEquals(user, session.getUser());
        assertEquals(configuration, session.getConfiguration());
    }

    // Check that a Client Reset is correctly reported.
    @Test
    @RunTestInLooperThread
    public void errorHandler_clientResetReported() {
        SyncUser user = createTestUser();
        String url = "realm://objectserver.realm.io/default";
        final SyncConfiguration config = configFactory.createSyncConfigurationBuilder(user , url)
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        fail("Wrong error " + error.toString());
                    }

                    @Override
                    public void onClientReset(SyncSession session, ClientResetError error) {
                        String filePathFromError = error.getOriginalFileLocation().getAbsolutePath();
                        String filePathFromConfig = session.getConfiguration().getPath();
                        assertEquals(filePathFromError, filePathFromConfig);
                        assertFalse(error.getBackupFileLocation().exists());
                        assertTrue(error.getOriginalFileLocation().exists());
                        looperThread.testComplete();
                    }
                })
                .build();

        Realm realm = Realm.getInstance(config);
        looperThread.testRealms.add(realm);

        // Trigger error
        SyncManager.simulateClientReset(SyncManager.getSession(config));
    }

    // Check that we can manually execute the Client Reset.
    @Test
    public void errorHandler_manualExecuteClientReset() {
        SyncUser user = createTestUser();
        String url = "realm://objectserver.realm.io/default";
        final SyncConfiguration config = configFactory.createSyncConfigurationBuilder(user , url)
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        fail("Wrong error " + error.toString());
                    }

                    @Override
                    public void onClientReset(SyncSession session, ClientResetError error) {
                        try {
                            error.executeClientReset();
                            fail("All Realms should be closed before executing Client Reset can be allowed");
                        } catch(IllegalStateException ignored) {
                        }

                        // Execute Client Reset
                        looperThread.testRealms.get(0).close();
                        error.executeClientReset();

                        // Validate that files have been moved
                        assertFalse(error.getOriginalFileLocation().exists());
                        assertTrue(error.getBackupFileLocation().exists());
                        looperThread.testComplete();
                    }
                })
                .build();

        Realm realm = Realm.getInstance(config);
        looperThread.testRealms.add(realm);

        // Trigger error
        SyncManager.simulateClientReset(SyncManager.getSession(config));
    }

}
