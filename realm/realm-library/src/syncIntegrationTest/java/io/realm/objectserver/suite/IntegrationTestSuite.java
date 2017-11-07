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

package io.realm.objectserver.suite;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import io.realm.SSLConfigurationTests;
import io.realm.SyncedRealmTests;
import io.realm.objectserver.AuthTests;
import io.realm.objectserver.EncryptedSynchronizedRealmTests;
import io.realm.objectserver.ProcessCommitTests;
import io.realm.objectserver.ProgressListenerTests;
import io.realm.SyncSessionTests;

// Test suite includes all integration tests. Makes it easy to run all integration tests in the Android Studio.
@RunWith(Suite.class)
@Suite.SuiteClasses({
        SSLConfigurationTests.class,
        SyncedRealmTests.class,
        AuthTests.class,
        EncryptedSynchronizedRealmTests.class,
        ProcessCommitTests.class,
        ProgressListenerTests.class,
        SyncSessionTests.class})
public class IntegrationTestSuite {
}
