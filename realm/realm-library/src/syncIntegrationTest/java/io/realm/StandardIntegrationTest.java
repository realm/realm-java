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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;


/**
 * The standard base class for integration tests.
 * This class will keep a ROS instance running for all tests to minimize the overhead between each test.
 *
 * NOTE: All tests extending this class should use `@RunTestInLooperThread` tests.
 */
public abstract class StandardIntegrationTest extends BaseIntegrationTest {

    @BeforeClass
    public static void setupTestClass() throws Exception {
        startSyncServer();
    }

    @AfterClass
    public static void tearDownTestClass() throws Exception {
        stopSyncServer();
    }

    @Before
    public void setupTest() throws IOException {
        SyncTestUtils.prepareEnvironmentForTest();
    }

    @After
    public void teardownTest() {
        if (!looperThread.isRuleUsed() || looperThread.isTestComplete()) {
            // Non-looper tests can reset here
            SyncTestUtils.restoreEnvironmentAfterTest();
        } else {
            // Otherwise we need to wait for the test to complete
            looperThread.runAfterTest(new Runnable() {
                @Override
                public void run() {
                    SyncTestUtils.restoreEnvironmentAfterTest();
                }
            });
        }
    }

}
