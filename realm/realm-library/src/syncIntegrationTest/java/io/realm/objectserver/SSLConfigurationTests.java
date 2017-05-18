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

package io.realm.objectserver;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncUser;
import io.realm.objectserver.model.ProcessInfo;
import io.realm.objectserver.utils.Constants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SSLConfigurationTests extends BaseIntegrationTest {

    @Test
    public void trustedRootCA() throws InterruptedException {
        final String userId = UUID.randomUUID().toString();

        SyncCredentials credentials = SyncCredentials.usernamePassword(userId, "password", true);
        SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        SyncConfiguration config = new SyncConfiguration.Builder(user, Constants.USER_REALM)
                .build();
        Realm realm = Realm.getInstance(config);

        realm.beginTransaction();
        ProcessInfo processInfo = realm.createObject(ProcessInfo.class);
        processInfo.setName("Main_Process1");
        realm.commitTransaction();

        // make sure the changes gets to the server
        Thread.sleep(1500);
        realm.close();
        Realm.deleteRealm(config);

        // now connect again it should bring the previous commit
        credentials = SyncCredentials.usernamePassword(userId, "password", false);
        user = SyncUser.login(credentials, Constants.AUTH_URL);
        config = new SyncConfiguration.Builder(user, Constants.USER_REALM_SECURE)
                .waitForInitialRemoteData()
                .trustedRootCA("trusted_ca.pem")
                .build();

        realm = Realm.getInstance(config);
        RealmResults<ProcessInfo> all = realm.where(ProcessInfo.class).findAll();
        assertEquals(1, all.size());
        assertEquals("Main_Process1", all.get(0).getName());
    }

    @Test
    public void trustedRootCA_syncShouldFailWithoutTrustedCA() throws InterruptedException {
        final String userId = UUID.randomUUID().toString();

        SyncCredentials credentials = SyncCredentials.usernamePassword(userId, "password", true);
        SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        SyncConfiguration config = new SyncConfiguration.Builder(user, Constants.USER_REALM)
                .build();
        Realm realm = Realm.getInstance(config);

        realm.beginTransaction();
        ProcessInfo processInfo = realm.createObject(ProcessInfo.class);
        processInfo.setName("Main_Process1");
        realm.commitTransaction();

        // make sure the changes gets to the server
        Thread.sleep(1500);
        realm.close();
        Realm.deleteRealm(config);

        // now connect again it should not bring the previous commit
        // since the SSL handshake will fail
        credentials = SyncCredentials.usernamePassword(userId, "password", false);
        user = SyncUser.login(credentials, Constants.AUTH_URL);
        config = new SyncConfiguration.Builder(user, Constants.USER_REALM_SECURE)
                .build();
        realm = Realm.getInstance(config);
        assertTrue(realm.isEmpty());
    }

    @Test
    public void withoutSSLVerification() throws InterruptedException {
        final String userId = UUID.randomUUID().toString();

        SyncCredentials credentials = SyncCredentials.usernamePassword(userId, "password", true);
        SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        SyncConfiguration config = new SyncConfiguration.Builder(user, Constants.USER_REALM)
                .build();
        Realm realm = Realm.getInstance(config);

        realm.beginTransaction();
        ProcessInfo processInfo = realm.createObject(ProcessInfo.class);
        processInfo.setName("Main_Process1");
        realm.commitTransaction();

        // make sure the changes gets to the server
        Thread.sleep(1500);
        realm.close();
        Realm.deleteRealm(config);

        // now connect again it should bring the previous commit
        credentials = SyncCredentials.usernamePassword(userId, "password", false);
        user = SyncUser.login(credentials, Constants.AUTH_URL);
        config = new SyncConfiguration.Builder(user, Constants.USER_REALM_SECURE)
                .waitForInitialRemoteData()
                .withoutSSLVerification()
                .build();
        realm = Realm.getInstance(config);
        RealmResults<ProcessInfo> all = realm.where(ProcessInfo.class).findAll();
        assertEquals(1, all.size());
        assertEquals("Main_Process1", all.get(0).getName());
    }
}
