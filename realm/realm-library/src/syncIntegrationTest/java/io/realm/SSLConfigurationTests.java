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

import android.os.SystemClock;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.realm.entities.StringOnly;
import io.realm.exceptions.RealmFileException;
import io.realm.log.LogLevel;
import io.realm.log.RealmLog;
import io.realm.objectserver.utils.Constants;
import io.realm.rule.RunTestInLooperThread;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class SSLConfigurationTests extends StandardIntegrationTest {

    // TODO: All tests in this class are currently marked @RunTestInLooperThread,
    // this is strictly not necessary, but currently needed to avoid other issues with setting
    // up tests.

    @Rule
    public Timeout globalTimeout = Timeout.seconds(120);

    @Test
    @RunTestInLooperThread
    public void trustedRootCA() throws InterruptedException {
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);

        // 1. Copy a valid Realm to the server
        //noinspection unchecked
        final SyncConfiguration syncConfig = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .fullSynchronization()
                .schema(StringOnly.class)
                .build();
        Realm realm = Realm.getInstance(syncConfig);

        realm.beginTransaction();
        realm.createObject(StringOnly.class).setChars("Foo");
        realm.commitTransaction();

        // make sure the changes gets to the server
        SyncManager.getSession(syncConfig).uploadAllLocalChanges();
        realm.close();
        user.logOut();

        // 2. Local state should now be completely reset. Open the Realm again with a new configuration which should
        // download the uploaded changes.
        user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password), Constants.AUTH_URL);
        //noinspection unchecked
        SyncConfiguration syncConfigSSL = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM_SECURE)
                .fullSynchronization()
                .name("useSsl")
                .schema(StringOnly.class)
                .waitForInitialRemoteData()
                .trustedRootCA("trusted_ca.pem")
                .build();
        realm = Realm.getInstance(syncConfigSSL);

        RealmResults<StringOnly> all = realm.where(StringOnly.class).findAll();
        try {
            assertEquals(1, all.size());
            assertEquals("Foo", all.get(0).getChars());
        } finally {
            realm.close();
        }
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void withoutSSLVerification() throws InterruptedException {
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);

        // 1. Copy a valid Realm to the server
        //noinspection unchecked
        final SyncConfiguration syncConfig = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .fullSynchronization()
                .schema(StringOnly.class)
                .build();
        Realm realm = Realm.getInstance(syncConfig);

        realm.beginTransaction();
        realm.createObject(StringOnly.class).setChars("Foo");
        realm.commitTransaction();

        // make sure the changes gets to the server
        SyncManager.getSession(syncConfig).uploadAllLocalChanges();
        realm.close();
        user.logOut();

        // 2. Local state should now be completely reset. Open the Realm again with a new configuration which should
        // download the uploaded changes.
        user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password), Constants.AUTH_URL);
        //noinspection unchecked
        SyncConfiguration syncConfigSSL = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM_SECURE)
                .fullSynchronization()
                .name("useSsl")
                .schema(StringOnly.class)
                .waitForInitialRemoteData()
                .disableSSLVerification()
                .build();
        realm = Realm.getInstance(syncConfigSSL);

        RealmResults<StringOnly> all = realm.where(StringOnly.class).findAll();
        try {
            assertEquals(1, all.size());
            assertEquals("Foo", all.get(0).getChars());
        } finally {
            realm.close();
        }
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void trustedRootCA_syncShouldFailWithoutTrustedCA() throws InterruptedException {
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);

        // 1. Copy a valid Realm to the server
        //noinspection unchecked
        final SyncConfiguration syncConfig = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .schema(StringOnly.class)
                .build();
        Realm realm = Realm.getInstance(syncConfig);

        realm.beginTransaction();
        realm.createObject(StringOnly.class).setChars("Foo");
        realm.commitTransaction();

        // make sure the changes gets to the server
        SyncManager.getSession(syncConfig).uploadAllLocalChanges();
        realm.close();
        user.logOut();

        // 2. Local state should now be completely reset. Open the Realm again with a new configuration which should
        // download the uploaded changes.
        user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password), Constants.AUTH_URL);
        //noinspection unchecked
        SyncConfiguration syncConfigSSL = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM_SECURE)
                .name("useSsl")
                .schema(StringOnly.class)
                .trustedRootCA("untrusted_ca.pem")
                .build();
        // waitForInitialRemoteData will throw an Internal error (125): Operation Canceled
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(2));
        realm = Realm.getInstance(syncConfigSSL);
        try {
            assertTrue(realm.isEmpty());
        } finally {
            realm.close();
        }
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void combining_trustedRootCA_and_withoutSSLVerification_willThrow() {
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);

        TestHelper.TestLogger testLogger = new TestHelper.TestLogger();
        int originalLevel = RealmLog.getLevel();
        RealmLog.add(testLogger);
        RealmLog.setLevel(LogLevel.WARN);

        //noinspection unchecked
        configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM_SECURE)
                .name("useSsl")
                .schema(StringOnly.class)
                .trustedRootCA("trusted_ca.pem")
                .disableSSLVerification()
                .build();

        assertEquals("SSL Verification is disabled, the provided server certificate will not be used.",
                testLogger.message);
        RealmLog.remove(testLogger);
        RealmLog.setLevel(originalLevel);
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void trustedRootCA_notExisting_certificate_willThrow() {
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);
        //noinspection unchecked
        SyncConfiguration syncConfig = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM_SECURE)
                .schema(StringOnly.class)
                .trustedRootCA("none_existing_file.pem")
                .build();

        try {
            Realm.getInstance(syncConfig);
            fail();
        } catch (RealmFileException ignored) {
        }
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void combiningTrustedRootCA_and_disableSSLVerification() throws InterruptedException {
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);

        // 1. Copy a valid Realm to the server using ssl_verify_path option
        //noinspection unchecked
        final SyncConfiguration syncConfigWithCertificate = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM_SECURE)
                .fullSynchronization()
                .schema(StringOnly.class)
                .trustedRootCA("trusted_ca.pem")
                .build();
        Realm realm = Realm.getInstance(syncConfigWithCertificate);

        realm.beginTransaction();
        realm.createObject(StringOnly.class).setChars("Foo");
        realm.commitTransaction();

        // make sure the changes gets to the server
        SyncManager.getSession(syncConfigWithCertificate).uploadAllLocalChanges();
        realm.close();
        user.logOut();

        // 2. Local state should now be completely reset. Open the Realm again with a new configuration which should
        // download the uploaded changes.
        user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password), Constants.AUTH_URL);
        //noinspection unchecked
        SyncConfiguration syncConfigDisableSSL = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM_SECURE)
                .fullSynchronization()
                .name("useSsl")
                .schema(StringOnly.class)
                .waitForInitialRemoteData()
                .disableSSLVerification()
                .build();
        realm = Realm.getInstance(syncConfigDisableSSL);

        RealmResults<StringOnly> all = realm.where(StringOnly.class).findAll();
        try {
            assertEquals(1, all.size());
            assertEquals("Foo", all.get(0).getChars());
        } finally {
            realm.close();
        }
        looperThread.testComplete();
    }

    // IMPORTANT: Following test assume the root certificate is installed on the test device
    //            certificate is located in <realm-java>/tools/sync_test_server/keys/android_test_certificate.crt
    //            adb push <realm-java>/tools/sync_test_server/keys/android_test_certificate.crt /sdcard/
    //            then import the certificate from the device (Settings/Security/Install from storage)
    @Test
    @RunTestInLooperThread
    public void sslVerifyCallback_isUsed() throws InterruptedException {
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);

        // 1. Copy a valid Realm to the server using ssl_verify_path option
        //noinspection unchecked
        final SyncConfiguration syncConfig = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .fullSynchronization()
                .schema(StringOnly.class)
                .build();
        Realm realm = Realm.getInstance(syncConfig);

        realm.beginTransaction();
        realm.createObject(StringOnly.class).setChars("Foo");
        realm.commitTransaction();

        // make sure the changes gets to the server
        SyncManager.getSession(syncConfig).uploadAllLocalChanges();
        realm.close();
        user.logOut();

        // 2. Local state should now be completely reset. Open the Realm again with a new configuration which should
        // download the uploaded changes.
        user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password), Constants.AUTH_URL);
        //noinspection unchecked
        SyncConfiguration syncConfigSecure = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM_SECURE)
                .name("useSsl")
                .fullSynchronization()
                .schema(StringOnly.class)
                .waitForInitialRemoteData()
                .build();
        realm = Realm.getInstance(syncConfigSecure);

        RealmResults<StringOnly> all = realm.where(StringOnly.class).findAll();
        try {
            assertEquals(1, all.size());
            assertEquals("Foo", all.get(0).getChars());
        } finally {
            realm.close();
        }
        looperThread.testComplete();
    }
}
