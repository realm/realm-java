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

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.UiThreadTestRule;
import android.util.Log;

import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;

import io.realm.internal.OsRealmConfig;
import io.realm.internal.Util;
import io.realm.internal.sync.permissions.ObjectPermissionsModule;
import io.realm.log.LogLevel;
import io.realm.log.RealmLog;
import io.realm.objectserver.utils.HttpUtils;
import io.realm.objectserver.utils.UserFactory;
import io.realm.rule.RunInLooperThread;


/**
 * Base class used by Integration Tests.
 * This class should not be used directly. Instead {@link StandardIntegrationTest} or {@link IsolatedIntegrationTests }
 * should be used instead.
 */
public abstract class BaseIntegrationTest {

    private static int originalLogLevel;

    @Rule
    public RunInLooperThread looperThread = new RunInLooperThread();

    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    protected ConfigurationWrapper configurationFactory = new ConfigurationWrapper(looperThread);

    /**
     * Starts a new ROS instance that can be used for testing.
     */
    protected static void startSyncServer() {
        SyncManager.Debug.skipOnlineChecking = true;
        try {
            HttpUtils.startSyncServer();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Stops the ROS instance used for the test. The {@link #startSyncServer()} will stop the sync server if needed, so
     * normally there is no need to call this.
     */
    protected static void stopSyncServer() {
        try {
            HttpUtils.stopSyncServer();
        } catch (Exception e) {
            Log.e(HttpUtils.TAG, "Failed to stop Sync Server: " + Util.getStackTrace(e));
        }
    }
    
    // Returns a valid SyncConfiguration usable by tests
    // FIXME: WARNING: Do not use `SyncTestRealmConfigurationFactory`, but use this. Refactor later.
    protected static class ConfigurationWrapper {

        private final RunInLooperThread looperThread;

        ConfigurationWrapper(RunInLooperThread looperThread) {
            this.looperThread = looperThread;
            try {
                // The RunInLooperThread rule might not be fully created yet. Do it here.
                // The `create()` call is idempotent, so should be safe.
                looperThread.create();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        public SyncConfiguration.Builder createSyncConfigurationBuilder(SyncUser user, String url) {
            return user.createConfiguration(url)
                    .sessionStopPolicy(OsRealmConfig.SyncSessionStopPolicy.IMMEDIATELY)
                    .modules(Realm.getDefaultModule(), new ObjectPermissionsModule())
                    .directory(looperThread.getRoot());
        }

        public File getRoot() {
            return looperThread.getRoot();
        }
    }
}