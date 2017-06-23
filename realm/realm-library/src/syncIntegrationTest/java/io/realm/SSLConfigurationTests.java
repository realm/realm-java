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
import io.realm.rule.TestSyncConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class SSLConfigurationTests extends BaseIntegrationTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10);

    @Test
    public void trustedRootCA() throws InterruptedException {
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
}
