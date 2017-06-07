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
import android.util.Log;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import io.realm.internal.Util;
import io.realm.log.LogLevel;
import io.realm.log.RealmLog;
import io.realm.objectserver.utils.HttpUtils;

public class BaseIntegrationTest {

    private static int originalLogLevel;

    @BeforeClass
    public static void setUp () throws Exception {
        SyncManager.Debug.skipOnlineChecking = true;
        try {
            HttpUtils.startSyncServer();
        } catch (Exception e) {
            // Throwing an exception from this method will crash JUnit. Instead just log it.
            // If this setup method fails, all unit tests in the class extending it will most likely fail as well.
            Log.e(HttpUtils.TAG, "Could not start Sync Server: " + Util.getStackTrace(e));
        }
    }

    @AfterClass
    public static void tearDown () throws Exception {
        try {
            HttpUtils.stopSyncServer();
        } catch (Exception e) {
            Log.e(HttpUtils.TAG, "Failed to stop Sync Server" + Util.getStackTrace(e));
        }
    }

    @Before
    public void setupTest() throws IOException {
        // TODO We should implement a more consistent reset method for all of Sync that reset
        // everything completely including deleting all files.
        deleteRosFiles();
        if (BaseRealm.applicationContext != null) {
            // Realm was already initialized. Reset all internal state
            // in order to be able fully re-initialize.

            // This will set the 'm_metadata_manager' in 'sync_manager.cpp' to be 'null'
            // causing the SyncUser to remain in memory.
            // They're actually not persisted into disk.
            // move this call to 'tearDown' to clean in-memory & on-disk users
            // once https://github.com/realm/realm-object-store/issues/207 is resolved
            SyncManager.reset();
            BaseRealm.applicationContext = null; // Required for Realm.init() to work
        }
        Realm.init(InstrumentationRegistry.getContext());
        originalLogLevel = RealmLog.getLevel();
        RealmLog.setLevel(LogLevel.DEBUG);
    }

    @After
    public void tearDownTest() throws IOException {
        RealmLog.setLevel(originalLogLevel);
    }


    // Cleanup filesystem to make sure nothing lives for the next test.
    // Failing to do so might lead to DIVERGENT_HISTORY errors being thrown if Realms from
    // previous tests are being accessed.
    private static void deleteRosFiles() throws IOException {
        File rosFiles = new File(InstrumentationRegistry.getContext().getFilesDir(),"realm-object-server");
        deleteFile(rosFiles);
    }

    private static void deleteFile(File file) throws IOException {
        if (file.isDirectory()) {
            for (File c : file.listFiles()) {
                deleteFile(c);
            }
        }
        if (!file.delete()) {
            throw new IllegalStateException("Failed to delete file or directory: " + file.getAbsolutePath());
        }
    }
}
