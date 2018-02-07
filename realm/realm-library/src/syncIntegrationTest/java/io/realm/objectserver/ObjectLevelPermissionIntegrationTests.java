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
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.StandardIntegrationTest;
import io.realm.SyncConfiguration;
import io.realm.SyncManager;
import io.realm.SyncUser;
import io.realm.annotations.RealmModule;
import io.realm.entities.AllJavaTypes;
import io.realm.objectserver.model.PermissionObject;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.UserFactory;
import io.realm.rule.RunTestInLooperThread;
import io.realm.sync.permissions.ClassPermissions;
import io.realm.sync.permissions.Permission;
import io.realm.sync.permissions.RealmPermissions;
import io.realm.sync.permissions.RealmPrivileges;
import io.realm.sync.permissions.Role;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ObjectLevelPermissionIntegrationTests extends StandardIntegrationTest {

    @RealmModule(classes = { AllJavaTypes.class })
    public static class ObjectLevelTestModule {
    }

    // Check default privileges after being online for the first time
    @Test
    @RunTestInLooperThread
    public void getPrivileges_serverDefaults() throws InterruptedException {
        SyncUser user = UserFactory.createNicknameUser(Constants.AUTH_URL, "user1", true);
        SyncConfiguration syncConfig = configurationFactory.createSyncConfigurationBuilder(user, Constants.GLOBAL_REALM)
                .modules(new ObjectLevelTestModule())
                .partialRealm()
                .waitForInitialRemoteData()
                .build();
        Realm realm = Realm.getInstance(syncConfig);
        looperThread.closeAfterTest(realm);

        // FIXME: Work-around for class permissions not being setup correctly yet
        // Remove this once ROS is upgraded to Sync 3.0.0-beta.1
        realm.beginTransaction();
        ClassPermissions permissions = realm.where(ClassPermissions.class).equalTo("name", "__Class").findFirst();
        Permission permission = permissions.getPermissions().first();
        permission.setCanCreate(true);
        permission.setCanDelete(true);
        realm.createObject(ClassPermissions.class, "AllJavaTypes").getPermissions().add(permission);
        realm.commitTransaction();
        SyncManager.getSession(syncConfig).uploadAllLocalChanges();
        SyncManager.getSession(syncConfig).downloadAllServerChanges();
        // FIXME: Workaround end

        // Check Realm privileges
        RealmPrivileges realmPrivileges = realm.getPrivileges();
        assertFullAccess(realmPrivileges);

        // Check Class privileges
        RealmPrivileges classPrivileges = realm.getPrivileges(AllJavaTypes.class);
        assertFullAccess(classPrivileges);

        // Check Object privileges
        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class);
        realm.commitTransaction();
        SyncManager.getSession(syncConfig).uploadAllLocalChanges();
        SyncManager.getSession(syncConfig).downloadAllServerChanges();
        realm.refresh();
        RealmPrivileges objectPrivileges = realm.getPrivileges(obj);
        assertEquals(1, realm.where(AllJavaTypes.class).count()); // Make sure object isn't deleted
        assertFullAccess(objectPrivileges);

        looperThread.testComplete();
    }


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

    private void assertFullAccess(Permission permission) {
        assertTrue(permission.canCreate());
        assertTrue(permission.canRead());
        assertTrue(permission.canUpdate());
        assertTrue(permission.canDelete());
        assertTrue(permission.canQuery());
        assertTrue(permission.canSetPermissions());
        assertTrue(permission.canModifySchema());
    }

    private void assertFullAccess(RealmPrivileges privileges) {
        assertTrue(privileges.canCreate());
        assertTrue(privileges.canRead());
        assertTrue(privileges.canUpdate());
        assertTrue(privileges.canDelete());
        assertTrue(privileges.canQuery());
        assertTrue(privileges.canSetPermissions());
        assertTrue(privileges.canModifySchema());
    }
}
