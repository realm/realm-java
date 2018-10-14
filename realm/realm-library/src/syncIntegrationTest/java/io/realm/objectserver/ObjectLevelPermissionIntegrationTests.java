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

import java.util.Arrays;
import java.util.List;

import io.realm.IsolatedIntegrationTests;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.SyncConfiguration;
import io.realm.SyncManager;
import io.realm.SyncUser;
import io.realm.annotations.RealmModule;
import io.realm.entities.AllJavaTypes;
import io.realm.internal.sync.permissions.ObjectPermissionsModule;
import io.realm.log.RealmLog;
import io.realm.objectserver.model.PermissionObject;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.StringOnlyModule;
import io.realm.objectserver.utils.UserFactory;
import io.realm.rule.RunTestInLooperThread;
import io.realm.sync.permissions.ClassPrivileges;
import io.realm.sync.permissions.ObjectPrivileges;
import io.realm.sync.permissions.Permission;
import io.realm.sync.permissions.RealmPrivileges;
import io.realm.sync.permissions.Role;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for Object Level Permissions.
 * Each test is run in isolation as we use the the global default Realm for each test.
 * It is currently not possible to manually create a world readable Realm as
 * {@link io.realm.PermissionManager} is unstable on CI.
 */
@RunWith(AndroidJUnit4.class)
public class ObjectLevelPermissionIntegrationTests extends IsolatedIntegrationTests {

    @RealmModule(classes = {AllJavaTypes.class})
    public static class ObjectLevelTestModule {
    }

    @RealmModule(classes = {PermissionObject.class})
    public static class OLPermissionModule {
    }

    // Check default privileges after being online for the first time
    @Test
    @RunTestInLooperThread()
    public void getPrivileges_serverDefaults() throws InterruptedException {
        List schemaModule = Arrays.asList(new ObjectLevelTestModule());
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        SyncConfiguration syncConfig = configurationFactory.createSyncConfigurationBuilder(user, Constants.DEFAULT_REALM)
                .modules(schemaModule)
                .build();

        Realm realm = Realm.getInstance(syncConfig);

        // Make sure that all objects are part of the Partial Sync transitive closure
        realm.where(AllJavaTypes.class).findAllAsync("keep-AllJavaTypes");

        // Create offline object
        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class, 0);
        realm.commitTransaction();
        assertEquals(1, realm.where(AllJavaTypes.class).count());

        // Make sure that server permissions have been applied to local object
        SyncManager.getSession(syncConfig).uploadAllLocalChanges();
        SyncManager.getSession(syncConfig).downloadAllServerChanges();
        realm.refresh();

        // Check Realm privileges
        RealmPrivileges realmPrivileges = realm.getPrivileges();
        assertFullAccess(realmPrivileges);

        // Check Class privileges
        ClassPrivileges classPrivileges = realm.getPrivileges(AllJavaTypes.class);
        assertFullAccess(classPrivileges);

        // Check Object privileges
        assertEquals(1, realm.where(AllJavaTypes.class).count());
        ObjectPrivileges objectPrivileges = realm.getPrivileges(obj);
        assertFullAccess(objectPrivileges);

        realm.close();
        looperThread.testComplete();
    }

    // Restrict read/write permission, only the owner of the object can see/modify it
    @Test
    @RunTestInLooperThread()
    public void restrictAccessToOwner() throws InterruptedException {
        List schemaModules = Arrays.asList(new StringOnlyModule(), new OLPermissionModule(), new ObjectPermissionsModule());

        // connect with user1
        SyncUser user1 = UserFactory.createUniqueUser(Constants.AUTH_URL);
        SyncConfiguration user1SyncConfig = configurationFactory
                .createSyncConfigurationBuilder(user1, Constants.DEFAULT_REALM)
                .modules(schemaModules)
                .build();
        Realm user1Realm = Realm.getInstance(user1SyncConfig);
        user1Realm.beginTransaction();

        // added a new Role to restrict access to our objects
        Role role = user1Realm.createObject(Role.class, "role_" + user1.getIdentity());
        role.addMember(user1.getIdentity());

        // add permission so this will be only visible and modifiable from user1
        Permission userPermission = new Permission(role);
        userPermission.setCanRead(true);
        userPermission.setCanQuery(true);
        userPermission.setCanCreate(true);
        userPermission.setCanUpdate(true);
        userPermission.setCanUpdate(true);
        userPermission.setCanDelete(true);
        userPermission.setCanSetPermissions(true);
        userPermission.setCanModifySchema(true);

        PermissionObject permissionObject1 = user1Realm.createObject(PermissionObject.class, "Foo");
        permissionObject1.getPermissions().add(userPermission);
        user1Realm.commitTransaction();

        SyncManager.getSession(user1SyncConfig).uploadAllLocalChanges();
        user1Realm.close();

        // Connect with admin user and verify that user1 object is visible (non-partial Realm)
        SyncUser adminUser = UserFactory.createNicknameUser(Constants.AUTH_URL, "admin2", true);
        SyncConfiguration adminConfig = configurationFactory.createSyncConfigurationBuilder(adminUser, Constants.DEFAULT_REALM)
                .fullSynchronization()
                .modules(schemaModules)
                .waitForInitialRemoteData()
                .build();
        Realm adminRealm = Realm.getInstance(adminConfig);
        RealmResults<PermissionObject> allPermissionObjects = adminRealm.where(PermissionObject.class).findAll();
        assertEquals(1, allPermissionObjects.size());
        PermissionObject permissionObject = allPermissionObjects.first();
        assertEquals("Foo", permissionObject.getName());
        assertEquals(1, permissionObject.getPermissions().size());
        Permission permission = permissionObject.getPermissions().get(0);
        assertFullAccess(permission);
        adminRealm.close();

        // Connect with user 2 and verify that user1 object is not visible
        SyncUser user2 = UserFactory.createUniqueUser(Constants.AUTH_URL);
        SyncConfiguration syncConfig2 = configurationFactory.createSyncConfigurationBuilder(user2, Constants.DEFAULT_REALM)
                .modules(schemaModules)
                .build();
        Realm user2Realm = Realm.getInstance(syncConfig2);
        looperThread.closeAfterTest(user2Realm);
        RealmResults<PermissionObject> allAsync = user2Realm.where(PermissionObject.class).findAllAsync();
        looperThread.keepStrongReference(allAsync);
        // new object should not be visible for user2 partial sync
        allAsync.addChangeListener((permissionObjects2, changeSet) -> {
            RealmLog.info("State: " + changeSet.getState().toString() + ", complete: " + changeSet.isCompleteResult());
            if (changeSet.isCompleteResult()) {
                assertEquals(0, permissionObjects2.size());
                looperThread.testComplete();
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

    private void assertFullAccess(ClassPrivileges privileges) {
        assertTrue(privileges.canCreate());
        assertTrue(privileges.canRead());
        assertTrue(privileges.canUpdate());
        assertTrue(privileges.canQuery());
        assertTrue(privileges.canSetPermissions());
    }

    private void assertFullAccess(RealmPrivileges privileges) {
        assertTrue(privileges.canRead());
        assertTrue(privileges.canUpdate());
        assertTrue(privileges.canSetPermissions());
        assertTrue(privileges.canModifySchema());
    }

    private void assertFullAccess(ObjectPrivileges privileges) {
        assertTrue(privileges.canRead());
        assertTrue(privileges.canUpdate());
        assertTrue(privileges.canDelete());
        assertTrue(privileges.canSetPermissions());
    }

}
