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

import io.realm.ObjectServerError;
import io.realm.PermissionManager;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.StandardIntegrationTest;
import io.realm.SyncConfiguration;
import io.realm.SyncManager;
import io.realm.SyncUser;
import io.realm.annotations.RealmModule;
import io.realm.entities.AllJavaTypes;
import io.realm.internal.sync.permissions.ObjectPermissionsModule;
import io.realm.objectserver.model.PermissionObject;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.StringOnlyModule;
import io.realm.objectserver.utils.UserFactory;
import io.realm.permissions.AccessLevel;
import io.realm.permissions.PermissionRequest;
import io.realm.permissions.UserCondition;
import io.realm.rule.RunTestInLooperThread;
import io.realm.sync.permissions.ClassPermissions;
import io.realm.sync.permissions.ClassPrivileges;
import io.realm.sync.permissions.Permission;
import io.realm.sync.permissions.RealmPrivileges;
import io.realm.sync.permissions.Role;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ObjectLevelPermissionIntegrationTests extends StandardIntegrationTest {

    @RealmModule(classes = { AllJavaTypes.class })
    public static class ObjectLevelTestModule {
    }

    @RealmModule(classes = {PermissionObject.class})
    public static class OLPermissionModule {
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
        permission.setCanModifySchema(true);
        realm.createObject(ClassPermissions.class, "AllJavaTypes").getPermissions().add(permission);
        realm.commitTransaction();
        SyncManager.getSession(syncConfig).uploadAllLocalChanges();
        SyncManager.getSession(syncConfig).downloadAllServerChanges();
        // FIXME: Workaround end

        // Check Realm privileges
        RealmPrivileges realmPrivileges = realm.getPrivileges();
        assertFullAccess(realmPrivileges);

        // Check Class privileges
        ClassPrivileges classPrivileges = realm.getPrivileges(AllJavaTypes.class);
        assertFullAccess(classPrivileges);

        // Check Object privileges
        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class, 0);
        realm.commitTransaction();
        SyncManager.getSession(syncConfig).uploadAllLocalChanges();
        SyncManager.getSession(syncConfig).downloadAllServerChanges();
        realm.refresh();
        assertEquals(0, realm.where(AllJavaTypes.class).count()); // Make sure object isn't deleted

        // FIXME: Re-enable when 3.0.0-beta.1 is released in ROS
//        assertEquals(1, realm.where(AllJavaTypes.class).count()); // Make sure object isn't deleted
//        RealmPrivileges objectPrivileges = realm.getPrivileges(obj);
//        assertFullAccess(objectPrivileges);

        looperThread.testComplete();
    }

//    @Test
//    @RunTestInLooperThread
//    public void getRoles() {
//        fail("FIXME");
//        looperThread.testComplete();
//    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void restrictAccessToOwner() {
        // Create a reference/global Realm needed for partial sync
        SyncUser adminUser = UserFactory.createAdminUser(Constants.AUTH_URL);
        SyncConfiguration adminSyncConfig = configurationFactory.createSyncConfigurationBuilder(adminUser, Constants.GLOBAL_REALM)
                .modules(new StringOnlyModule())// we can't create an empty endpoint (no schema) with the Java API
                .build();
        final Realm adminRealm = Realm.getInstance(adminSyncConfig);
        // Grant all users ROS permission to access the reference Realm
        PermissionManager pm = adminUser.getPermissionManager();
        PermissionRequest request = new PermissionRequest(UserCondition.noExistingPermissions(), Constants.GLOBAL_REALM, AccessLevel.WRITE);
        pm.applyPermissions(request, new PermissionManager.ApplyPermissionsCallback() {
            @Override
            public void onSuccess() {
                adminRealm.close();
                SyncUser user1 = UserFactory.createUniqueUser(Constants.AUTH_URL);
                SyncUser user2 = UserFactory.createUniqueUser(Constants.AUTH_URL);

                // connect with user1
                SyncConfiguration user1SyncConfig = configurationFactory
                        .createSyncConfigurationBuilder(user1, Constants.GLOBAL_REALM)
                        .modules(new OLPermissionModule(), new ObjectPermissionsModule())
                        .partialRealm()
                        .build();

                Realm realmUser1 = Realm.getInstance(user1SyncConfig);
                realmUser1.beginTransaction();
                // added a new Role to restrict access to our objects
                Role role = realmUser1.createObject(Role.class, "role_" + user1.getIdentity());
                role.addMember(user1.getIdentity());
                realmUser1.insert(role);

                // add permission so this will be only writable from user1
                Permission userPermission = new Permission(role);
                userPermission.setCanRead(true);
                userPermission.setCanQuery(true);
                userPermission.setCanCreate(true);
                userPermission.setCanUpdate(true);
                userPermission.setCanUpdate(true);
                userPermission.setCanDelete(true);
                userPermission.setCanSetPermissions(true);
                userPermission.setCanModifySchema(true);

                PermissionObject permissionObject1 = realmUser1.createObject(PermissionObject.class, "Foo");
                permissionObject1.getPermissions().add(userPermission);
                realmUser1.commitTransaction();

                // open admin Realm with permission module to query for PermissionObject
                Realm referenceRealm = Realm.getInstance(configurationFactory.createSyncConfigurationBuilder(adminUser, Constants.GLOBAL_REALM)
                        .modules(new OLPermissionModule(), new ObjectPermissionsModule())
                        .build());
                // new object is available in the reference Realm
                RealmResults<PermissionObject> allPermissionObjects = referenceRealm.where(PermissionObject.class).findAllAsync();
                allPermissionObjects.addChangeListener(permissionObjects -> {
                    assertEquals(1, permissionObjects.size());
                    assertEquals("Foo", permissionObjects.get(0).getName());
                    assertEquals(1, permissionObjects.get(0).getPermissions().size());
                    Permission permission = permissionObjects.get(0).getPermissions().get(0);
                    assertFullAccess(permission);
                    referenceRealm.close();

                    // Open a partial sync with a different user to make sure the instance is not visible
                    SyncConfiguration syncConfig2 = configurationFactory
                            .createSyncConfigurationBuilder(user2, Constants.GLOBAL_REALM)
                            .modules(new OLPermissionModule(), new ObjectPermissionsModule())
                            .partialRealm()
                            .build();
                    Realm realmUser2 = Realm.getInstance(syncConfig2);
                    RealmResults<PermissionObject> allAsync = realmUser2.where(PermissionObject.class).findAllAsync();
                    looperThread.keepStrongReference(allAsync);
                    allAsync.addChangeListener((permissionObjects2, changeSet) -> {
                        switch (changeSet.getState()) {
                            case INITIAL:
                                assertEquals(0, permissionObjects2.size());
                                break;
                            case UPDATE:
                                assertEquals(0, permissionObjects2.size());
                                realmUser2.close();
                                realmUser1.close();
                                looperThread.testComplete();
                                break;
                            case ERROR:
                                fail("Unexpected error callback");
                                break;
                        }
                    });
                });
            }

            @Override
            public void onError(ObjectServerError error) {
                adminRealm.close();
                fail(error.getErrorMessage());
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
        assertTrue(privileges.canModifySchema());
    }

    private void assertFullAccess(RealmPrivileges privileges) {
        assertTrue(privileges.canRead());
        assertTrue(privileges.canUpdate());
        assertTrue(privileges.canSetPermissions());
        assertTrue(privileges.canModifySchema());
    }
}
