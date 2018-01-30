/*
 * Copyright 2018 Realm Inc.
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

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import io.realm.Realm;
import io.realm.StandardIntegrationTest;
import io.realm.SyncConfiguration;
import io.realm.SyncManager;
import io.realm.SyncUser;
import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.UserFactory;
import io.realm.rule.RunTestInLooperThread;
import io.realm.sync.permissions.ClassPermissions;
import io.realm.sync.permissions.RealmPermissions;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ObjectLevelPermissionTests extends StandardIntegrationTest {

    private Realm realm;
    private SyncConfiguration syncConfig;

    @Before
    @RunTestInLooperThread
    public void setUp() throws IOException {
        super.setupTest();
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        syncConfig = configurationFactory.createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .partialRealm()
                .build();
        realm = Realm.getInstance(syncConfig);
        looperThread.closeAfterTest(realm);
    }

    @Test
    @RunTestInLooperThread
    public void realmPermissions_localDefaults() {
        // Default permissions (before they can be loaded from the server)
        RealmPermissions permissions = realm.getPermissions();
        fail("FIXME");
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void realmPermissions_serverDefaults() throws InterruptedException {
        SyncManager.getSession(syncConfig).downloadAllServerChanges();
        realm.refresh();
        RealmPermissions permissions = realm.getPermissions();
        fail("FIXME");
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void classPermissions_localDefaults() {
        // Default permissions (before they can be loaded from the server)
        ClassPermissions permissions = realm.getPermissions(AllTypes.class);
        fail("FIXME");
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void classPermissions_serverDefaults() throws InterruptedException {
        SyncManager.getSession(syncConfig).downloadAllServerChanges();
        realm.refresh();
        ClassPermissions permissions = realm.getPermissions(AllTypes.class);
        fail("FIXME");
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void realmPrivileges() {
        fail("FIXME");
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void classPrivileges() {
        fail("FIXME");
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void objectPrivileges() {
        fail("FIXME");
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void getUsers() {
        fail("FIXME");
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void getRoles() {
        fail("FIXME");
        looperThread.testComplete();
    }
}
