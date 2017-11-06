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
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.realm.entities.StringOnly;
import io.realm.exceptions.RealmFileException;
import io.realm.log.LogLevel;
import io.realm.log.RealmLog;
import io.realm.objectserver.utils.Constants;
import io.realm.rule.TestSyncConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


@RunWith(AndroidJUnit4.class)
public class SSLConfigurationTests extends BaseIntegrationTest {
//todo check we're using websovkte for secure realm
//    @Rule
//    public Timeout globalTimeout = Timeout.seconds(10);

    @Test
    public void trustedRootCA() throws InterruptedException {
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.login(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);

        // 1. Copy a valid Realm to the server
        final SyncConfiguration syncConfig = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .schema(StringOnly.class)
                .build();
        Realm realm = Realm.getInstance(syncConfig);

        realm.beginTransaction();
        realm.createObject(StringOnly.class).setChars("Foo");
        realm.commitTransaction();

        // make sure the changes gets to the server
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(2));  // FIXME: Replace with Sync Progress Notifications once available.
        realm.close();
        user.logout();
        Realm.deleteRealm(syncConfig);

        // 2. Local state should now be completely reset. Open the Realm again with a new configuration which should
        // download the uploaded changes.
        user = SyncUser.login(SyncCredentials.usernamePassword(username, password), Constants.AUTH_URL);
        SyncConfiguration config = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM_SECURE)
                .schema(StringOnly.class)
                .waitForInitialRemoteData()
                .trustedRootCA("trusted_ca.pem")
                .build();
        realm = Realm.getInstance(config);

        RealmResults<StringOnly> all = realm.where(StringOnly.class).findAll();
        try {
            assertEquals(1, all.size());
            assertEquals("Foo", all.get(0).getChars());
        } finally {
            realm.close();
        }
    }

    @Test
    public void withoutSSLVerification() throws InterruptedException {
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.login(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);

        // 1. Copy a valid Realm to the server
        final SyncConfiguration configOld = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .schema(StringOnly.class)
                .build();
        Realm realm = Realm.getInstance(configOld);

        realm.beginTransaction();
        realm.createObject(StringOnly.class).setChars("Foo");
        realm.commitTransaction();

        // make sure the changes gets to the server
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(2));  // FIXME: Replace with Sync Progress Notifications once available.
        realm.close();
        user.logout();
        Realm.deleteRealm(configOld);

        // 2. Local state should now be completely reset. Open the Realm again with a new configuration which should
        // download the uploaded changes.
        user = SyncUser.login(SyncCredentials.usernamePassword(username, password), Constants.AUTH_URL);
        SyncConfiguration config = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM_SECURE)
                .schema(StringOnly.class)
                .waitForInitialRemoteData()
                .disableSSLVerification()
                .build();
        realm = Realm.getInstance(config);

        RealmResults<StringOnly> all = realm.where(StringOnly.class).findAll();
        try {
            assertEquals(1, all.size());
            assertEquals("Foo", all.get(0).getChars());
        } finally {
            realm.close();
        }
    }

    @Test
    public void trustedRootCA_syncShouldFailWithoutTrustedCA() throws InterruptedException {
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.login(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);

        // 1. Copy a valid Realm to the server
        final SyncConfiguration configOld = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .schema(StringOnly.class)
                .build();
        Realm realm = Realm.getInstance(configOld);

        realm.beginTransaction();
        realm.createObject(StringOnly.class).setChars("Foo");
        realm.commitTransaction();

        // make sure the changes gets to the server
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(2));  // FIXME: Replace with Sync Progress Notifications once available.
        realm.close();
        user.logout();
        Realm.deleteRealm(configOld);

        // 2. Local state should now be completely reset. Open the Realm again with a new configuration which should
        // download the uploaded changes.
        user = SyncUser.login(SyncCredentials.usernamePassword(username, password), Constants.AUTH_URL);
        SyncConfiguration config = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM_SECURE)
                .schema(StringOnly.class)
                .build();
        realm = Realm.getInstance(config);
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(2));// don't call waitForInitialRemoteData as it will block forever
        try {
            assertTrue(realm.isEmpty());
        } finally {
            realm.close();
        }
    }

    @Test
    public void combining_trustedRootCA_and_withoutSSLVerification_willThrow() {
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.login(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);

        TestHelper.TestLogger testLogger = new TestHelper.TestLogger();
        RealmLog.add(testLogger);
        RealmLog.setLevel(LogLevel.WARN);

        configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM_SECURE)
                .schema(StringOnly.class)
                .trustedRootCA("trusted_ca.pem")
                .disableSSLVerification()
                .build();

        assertEquals("SSL Verification is disabled, the provided server certificate will not be used.",
                testLogger.message);
    }

    @Test
    public void trustedRootCA_notExisting_certificate_willThrow() {
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.login(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);
        SyncConfiguration config = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM_SECURE)
                .schema(StringOnly.class)
                .trustedRootCA("not_existing_file.pem")
                .build();

        try {
            Realm.getInstance(config);
            fail();
        } catch (RealmFileException ignored) {
        }
    }

    // combining ssl_trust_certificate_path & verify_servers_ssl_certificate=false (should work since no validation for second)
    @Test
    public void combiningTwoSSLConfiguration() throws InterruptedException {
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.login(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);

        // 1. Copy a valid Realm to the server using ssl_verify_path option
        final SyncConfiguration configOld = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM_SECURE)
                .schema(StringOnly.class)
                .trustedRootCA("trusted_ca.pem")
                .build();
        Realm realm = Realm.getInstance(configOld);

        realm.beginTransaction();
        realm.createObject(StringOnly.class).setChars("Foo");
        realm.commitTransaction();

        // make sure the changes gets to the server
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(2));  // FIXME: Replace with Sync Progress Notifications once available.
        realm.close();
        user.logout();
        Realm.deleteRealm(configOld);

        // 2. Local state should now be completely reset. Open the Realm again with a new configuration which should
        // download the uploaded changes.
        user = SyncUser.login(SyncCredentials.usernamePassword(username, password), Constants.AUTH_URL);
        SyncConfiguration config = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM_SECURE)
                .schema(StringOnly.class)
                .waitForInitialRemoteData()
                .disableSSLVerification()
                .build();
        realm = Realm.getInstance(config);

        RealmResults<StringOnly> all = realm.where(StringOnly.class).findAll();
        try {
            assertEquals(1, all.size());
            assertEquals("Foo", all.get(0).getChars());
        } finally {
            realm.close();
        }
    }

    // combining verify_servers_ssl_certificate=false & SSLVerifyCallback (should fail since first config is not inherited)
    @Test
    public void combiningTwoSSLConfiguration2() throws InterruptedException {
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.login(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);

        // 1. Copy a valid Realm to the server using ssl_verify_path option
        final SyncConfiguration configOld = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM_SECURE)
                .schema(StringOnly.class)
                .disableSSLVerification()
                .build();
        Realm realm = Realm.getInstance(configOld);

        realm.beginTransaction();
        realm.createObject(StringOnly.class).setChars("Foo");
        realm.commitTransaction();

        // make sure the changes gets to the server
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(2));  // FIXME: Replace with Sync Progress Notifications once available.
        realm.close();
        user.logout();
        Realm.deleteRealm(configOld);

        // 2. Local state should now be completely reset. Open the Realm again with a new configuration which should
        // not download the uploaded changes. since it uses the verifyCallback which returns false (cert is not installed on Android)
        // TODO add test that make sure the callback is called, use mockito to verify number of calls
        // plus override the behaviour ti return false/true as desired
        user = SyncUser.login(SyncCredentials.usernamePassword(username, password), Constants.AUTH_URL);
        SyncConfiguration config = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM_SECURE)
                .schema(StringOnly.class)
                .build();
        realm = Realm.getInstance(config);
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(2));
        assertTrue(realm.isEmpty());
        realm.close();
    }

    // combining 2 different certificate path second is invalid should not use the first valid one (from 1 sync)
    @Test
    public void combiningTwoSSLConfiguration3() throws InterruptedException {
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.login(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);
//        String json = user.getSyncUser().toJson();
        // 1. Copy a valid Realm to the server using ssl_verify_path option
        final SyncConfiguration configOld = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM_SECURE)
                .schema(StringOnly.class)
                .trustedRootCA("untrusted_ca.pem")
                .build();
        Realm realm = Realm.getInstance(configOld);

        realm.beginTransaction();
        realm.createObject(StringOnly.class).setChars("Foo");
        realm.commitTransaction();

//        // make sure the changes gets to the server
//        SystemClock.sleep(TimeUnit.SECONDS.toMillis(2));  // FIXME: Replace with Sync Progress Notifications once available.
//        realm.close();
//        user.logout();
//        Realm.deleteRealm(configOld);

        // 2. Local state should now be completely reset. Open the Realm again with a new configuration which should
        // download the uploaded changes.
        String username2 = UUID.randomUUID().toString();
        final SyncUser user2 = SyncUser.login(SyncCredentials.usernamePassword(username2, password, true), Constants.AUTH_URL);
        SyncConfiguration config = configurationFactory.createSyncConfigurationBuilder(user2, Constants.USER_REALM_SECURE)
                .schema(StringOnly.class)
                .trustedRootCA("trusted_ca.pem")
                .build();
        Realm realm2 = Realm.getInstance(config);

        realm2.beginTransaction();
        realm2.createObject(StringOnly.class).setChars("Bar");
        realm2.commitTransaction();

        // wait for realm2 to upload commits
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(2));

        realm.close();
        realm2.close();

        final CountDownLatch wait = new CountDownLatch(1);
        new Thread() {
            @Override
            public void run() {
                // now try with none SSL to make sure commits only from Realm2 were uploaded
                SyncConfiguration config3 = configurationFactory.createSyncConfigurationBuilder(user2, Constants.USER_REALM_SECURE)
                        .schema(StringOnly.class)
                        .disableSSLVerification()
                        .build();
                Realm realm3 = Realm.getInstance(config3);

                RealmResults<StringOnly> all = realm3.where(StringOnly.class).findAll();
                try {
                    assertEquals(1, all.size());
                    assertEquals("Bar", all.get(0).getChars());
                } finally {
                    realm3.close();
                    wait.countDown();
                }
            }
        }.start();

        wait.await();
    }

    // make sure SSLValidate Callback is called
    //FIXME test passed make sure the certificate is installed
    @Test
    public void sslVerifyCallback_isUsed() throws InterruptedException {
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.login(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);

        // 1. Copy a valid Realm to the server using ssl_verify_path option
        final SyncConfiguration configUnsecure = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .schema(StringOnly.class)
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        System.out.println(">>>>>>>>>>>>>>>> UN-S E C  E R R O R HANDLER");
                    }
                })
                .build();
        Realm realm = Realm.getInstance(configUnsecure);

        realm.beginTransaction();
        realm.createObject(StringOnly.class).setChars("Foo");
        realm.commitTransaction();

        // make sure the changes gets to the server
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(2));  // FIXME: Replace with Sync Progress Notifications once available.
        realm.close();
        user.logout();
        Realm.deleteRealm(configUnsecure);

        // 2. Local state should now be completely reset. Open the Realm again with a new configuration which should
        // download the uploaded changes.
        user = SyncUser.login(SyncCredentials.usernamePassword(username, password), Constants.AUTH_URL);
        SyncConfiguration configSecure = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM_SECURE)
                .schema(StringOnly.class)
                .waitForInitialRemoteData()
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        System.out.println(">>>>>>>>>>>>>>>> S E C  E R R O R HANDLER");
                    }
                })
                .build();
        realm = Realm.getInstance(configSecure);

        RealmResults<StringOnly> all = realm.where(StringOnly.class).findAll();
        try {
            assertEquals(1, all.size());
            assertEquals("Foo", all.get(0).getChars());
        } finally {
            realm.close();
        }
    }
}
