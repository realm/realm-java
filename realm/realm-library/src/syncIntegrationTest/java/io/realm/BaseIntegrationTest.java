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

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import io.realm.log.LogLevel;
import io.realm.log.RealmLog;
import io.realm.objectserver.utils.HttpUtils;

public class BaseIntegrationTest {

    private static int originalLogLevel;

    @BeforeClass
    public static void setUp () throws Exception {
        SyncManager.Debug.skipOnlineChecking = true;
        try {
            deleteRosFiles();
            BaseRealm.applicationContext = null; // Make it possible to re-initialize file system
            Realm.init(InstrumentationRegistry.getContext());
            originalLogLevel = RealmLog.getLevel();
            RealmLog.setLevel(LogLevel.DEBUG);
            HttpUtils.startSyncServer();
        } catch (Exception e) {
            // Throwing an exception from this method will crash JUnit. Instead just log it.
            // If this setup method fails, all unit tests in the class extending it will most likely fail as well.
            RealmLog.error("Could not start Sync Server", e);
        }
    }

    @AfterClass
    public static void tearDown () throws Exception {
        try {
            HttpUtils.stopSyncServer();
            RealmLog.setLevel(originalLogLevel);
            deleteRosFiles();
        } catch (Exception e) {
            RealmLog.error("Failed to stop Sync Server", e);
        }
    }

    // Cleanup filesystem to make sure nothing lives for the next test.
    // Failing to do so might lead to DIVERGENT_HISTORY errors being thrown if Realms from
    // previous tests are being accessed.
    private static void deleteRosFiles() throws IOException {
        File rosFiles = new File(InstrumentationRegistry.getContext().getFilesDir(),"realm-object-server");
        if (rosFiles.isDirectory()) {
            deleteFile(rosFiles);
        }
    }

    private static void deleteFile(File file) throws IOException {
        if (file.isDirectory()) {
            for (File c : file.listFiles())
                deleteFile(c);
        }
        if (!file.delete()) {
            throw new FileNotFoundException("Failed to delete file: " + file);
        }
    }
}
