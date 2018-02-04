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

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.StandardIntegrationTest;
import io.realm.SyncConfiguration;
import io.realm.SyncManager;
import io.realm.SyncUser;
import io.realm.entities.AllTypes;
import io.realm.objectserver.model.PermissionObject;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.UserFactory;
import io.realm.rule.RunTestInLooperThread;
import io.realm.sync.permissions.ClassPermissions;
import io.realm.sync.permissions.RealmPermission;
import io.realm.sync.permissions.RealmPermissions;
import io.realm.sync.permissions.Role;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class ObjectLevelPermissionIntegrationTests extends StandardIntegrationTest {

    private Realm realm;
    private SyncConfiguration syncConfig;
    private SyncUser adminUser;
    private SyncUser user1;
    private SyncUser user2;


    @Before
    @RunTestInLooperThread
    public void setUp() throws IOException {
        super.setupTest();
        user1 = UserFactory.createUniqueUser(Constants.AUTH_URL);
        syncConfig = configurationFactory.createSyncConfigurationBuilder(user1, Constants.USER_REALM_PUBLIC)
                .partialRealm()
                .build();
        realm = Realm.getInstance(syncConfig);
        looperThread.closeAfterTest(realm);
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

    // Restrict read/write permission, only the owner of the object can see/modify it
    // (use case of the ToDo app)
    @Test
    @RunTestInLooperThread
    public void restrictAccessToOwner() throws InterruptedException {
        //TODO use admin user
        adminUser = UserFactory.createAdminUser(Constants.AUTH_URL);
        user2 = UserFactory.createUniqueUser(Constants.AUTH_URL);

        SyncConfiguration adminSyncConfig = configurationFactory
                .createSyncConfigurationBuilder(adminUser, Constants.SYNC_SERVER_URL)
                .partialRealm()
                .build();

        Realm realmAdmin = Realm.getInstance(adminSyncConfig);

        // should be done by admin
        ClassPermissions classPermissions = new ClassPermissions(PermissionObject.class);
        realmAdmin.beginTransaction();

        RealmPermission permission = new RealmPermission();
        permission.setCanRead(true);
        permission.setCanQuery(true);
        permission.setCanCreate(true);
        permission.setCanUpdate(true);
        Role everyoneRole = realmAdmin.where(Role.class).equalTo("name", "everyone").findFirst();
        assertNotNull(everyoneRole);//FIXME do we have the guarantee that the role exists
        permission.setRole(everyoneRole);

        classPermissions.getPermissions().add(permission);
        realmAdmin.commitTransaction();
        SyncManager.getSession(adminSyncConfig).uploadAllLocalChanges();
        realmAdmin.close();

        // create object by user1
        realm.beginTransaction();
        // TODO what happen if we don't add a permission to the instance? can we still read the object
        PermissionObject userObject = realm.createObject(PermissionObject.class, "user_1_instance1");
        // add permission so this will be only writable from user1
        RealmPermission userPermission = new RealmPermission();
        userPermission.setCanRead(true);
        userPermission.setCanQuery(true);
        userPermission.setCanCreate(true);
        userPermission.setCanUpdate(true);

        Role userObjectRole = new Role(user1.getIdentity() + "_role");
        userObjectRole.addMember(user1.getIdentity());

        userPermission.setRole(userObjectRole);
        userObject.getPermissions().add(userPermission);
        realm.commitTransaction();
        SyncManager.getSession(syncConfig).uploadAllLocalChanges();
        realm.close();

        // user2 can't see or edit the object
        user2 = UserFactory.createUniqueUser(Constants.AUTH_URL);
        SyncConfiguration user2syncConfiguration = configurationFactory
                .createSyncConfigurationBuilder(user2, Constants.SYNC_SERVER_URL)
                .build();
        Realm user2Realm = Realm.getInstance(user2syncConfiguration);
        looperThread.addTestRealm(user2Realm);
        RealmResults<PermissionObject> all_permission_object = user2Realm
                .where(PermissionObject.class)
                .findAllAsync("all_permission_object");
        looperThread.keepStrongReference(all_permission_object);
        all_permission_object.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<PermissionObject>>() {
            @Override
            public void onChange(RealmResults<PermissionObject> permissionObjects, OrderedCollectionChangeSet changeSet) {
                switch (changeSet.getState()) {
                    case INITIAL: {
                        assertEquals(0, permissionObjects.size());
                        looperThread.testComplete();
                        break;
                    }
                    case UPDATE: {
                        fail("We don't expect updates");
                        break;
                    }
                    case ERROR: {
                        fail("Error while registering the partial sync query");
                        break;
                    }
                }
            }
        });
    }
}
