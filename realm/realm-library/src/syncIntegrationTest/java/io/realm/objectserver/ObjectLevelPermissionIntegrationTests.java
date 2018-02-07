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

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.StandardIntegrationTest;
import io.realm.SyncConfiguration;
import io.realm.SyncManager;
import io.realm.SyncUser;
import io.realm.objectserver.model.PermissionObject;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.UserFactory;
import io.realm.rule.RunTestInLooperThread;
import io.realm.sync.permissions.Permission;
import io.realm.sync.permissions.Role;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ObjectLevelPermissionIntegrationTests extends StandardIntegrationTest {

//    private Realm realm;
//    private SyncConfiguration syncConfig;
//    private SyncUser user1;
//    private SyncUser user2;


//    @Before
//    @RunTestInLooperThread
//    public void setUp() throws IOException {
//        super.setupTest();
//        user1 = UserFactory.createNicknameUser(Constants.AUTH_URL, "user1", true);
//        syncConfig = configurationFactory.createSyncConfigurationBuilder(user1, Constants.GLOBAL_REALM)
//                .partialRealm()
//                .build();
////        realm = Realm.getInstance(syncConfig);
//        looperThread.closeAfterTest(realm);
//    }
//
//    @Test
//    @RunTestInLooperThread
//    public void realmPermissions_serverDefaults() throws InterruptedException {
//        SyncManager.getSession(syncConfig).downloadAllServerChanges();
//        realm.refresh();
//        RealmPermissions permissions = realm.getPermissions();
//        fail("FIXME");
//        looperThread.testComplete();
//    }
//
//    @Test
//    @RunTestInLooperThread
//    public void classPermissions_serverDefaults() throws InterruptedException {
//        SyncManager.getSession(syncConfig).downloadAllServerChanges();
//        realm.refresh();
//        ClassPermissions permissions = realm.getPermissions(AllTypes.class);
//        fail("FIXME");
//        looperThread.testComplete();
//    }
//
//    @Test
//    @RunTestInLooperThread
//    public void realmPrivileges() {
//        fail("FIXME");
//        looperThread.testComplete();
//    }
//
//    @Test
//    @RunTestInLooperThread
//    public void classPrivileges() {
//        fail("FIXME");
//        looperThread.testComplete();
//    }
//
//    @Test
//    @RunTestInLooperThread
//    public void objectPrivileges() {
//        fail("FIXME");
//        looperThread.testComplete();
//    }
//
//    @Test
//    @RunTestInLooperThread
//    public void getUsers() {
//        fail("FIXME");
//        looperThread.testComplete();
//    }
//
//    @Test
//    @RunTestInLooperThread
//    public void getRoles() {
//        fail("FIXME");
//        looperThread.testComplete();
//    }

    // Restrict read/write permission, only the owner of the object can see/modify it
    // (use case of the ToDo app)
    // TODO in the ToDo app restrict access to all Items elements directly
    //      unless they are part of the retrieved Project (i.e remove query permission but give read
    //      to the Items table
    @Test
    @RunTestInLooperThread
    public void restrictAccessToOwner() throws InterruptedException {
        final SyncUser user2 = UserFactory.createNicknameUser(Constants.AUTH_URL, "user2", true);

        final String user2Role = "role_" + user2.getIdentity();

        SyncConfiguration user2SyncConfig = configurationFactory
                .createSyncConfigurationBuilder(user2, Constants.GLOBAL_REALM)
                .initialData(realm -> {
                    // create a Role for this user
                    Role role = realm.createObject(Role.class, user2Role);
                    role.addMember(user2.getIdentity());
                    realm.insert(role);

                    // TODO add classes specific permissions if necessary (i.e the OS default doesn't work for this use case)
                })
                .partialRealm()
                .build();

        Realm realmUser2 = Realm.getInstance(user2SyncConfig);

//        ClassPermissions classPermissions = new ClassPermissions(PermissionObject.class);
//        realmUser2.beginTransaction();
//
//        Permission permission = new Permission();
//        permission.setCanRead(true);
//        permission.setCanQuery(true);
//        permission.setCanCreate(true);
//        permission.setCanUpdate(true);
//        Role everyoneRole = realmUser2.where(Role.class).equalTo("name", "everyone").findFirst();
//        assertNotNull(everyoneRole);//FIXME do we have the guarantee that the role exists
//        permission.setRole(everyoneRole);
//
//        classPermissions.getPermissions().add(permission);
//
//        realmUser2.commitTransaction();
//        SyncManager.getSession(user2SyncConfig).uploadAllLocalChanges();
//        realmUser2.close();

        // create object by user1
        realmUser2.beginTransaction();
        PermissionObject userObject = realmUser2.createObject(PermissionObject.class, "user_2_instance1");

        Role role = realmUser2.where(Role.class).equalTo("name", user2Role).findFirst();
        assertNotNull(role);

        // add permission so this will be only writable from user1
        Permission userPermission = new Permission(role);
        userPermission.setCanRead(true);
        userPermission.setCanQuery(true);
        userPermission.setCanCreate(true);
        userPermission.setCanUpdate(true);
        userPermission.setCanUpdate(true);
        userPermission.setCanDelete(true);
        //TODO call instead allPrivileges builder instead of setting everything one by one
        // add a builder that start by either given all permissions or restricting all permissions
        // then start tuning

        userObject.getPermissions().add(userPermission);
        realmUser2.commitTransaction();

        SyncManager.getSession(user2SyncConfig).uploadAllLocalChanges();
        realmUser2.close();

        // user1 can't see or edit the object
        SyncUser user1 = UserFactory.createNicknameUser(Constants.AUTH_URL, "user1", true);
        SyncConfiguration user1syncConfiguration = configurationFactory
                .createSyncConfigurationBuilder(user1, Constants.GLOBAL_REALM)
                .partialRealm()
                .build();
        Realm user1Realm = Realm.getInstance(user1syncConfiguration);
        looperThread.addTestRealm(user1Realm);
        RealmResults<PermissionObject> all_permission_object = user1Realm
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
