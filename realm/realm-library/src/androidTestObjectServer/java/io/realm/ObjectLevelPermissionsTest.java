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
package io.realm;

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.annotations.RealmModule;
import io.realm.entities.AllJavaTypes;
import io.realm.entities.Dog;
import io.realm.exceptions.RealmException;
import io.realm.rule.RunInLooperThread;
import io.realm.sync.permissions.ClassPermissions;
import io.realm.sync.permissions.ClassPrivileges;
import io.realm.sync.permissions.ObjectPrivileges;
import io.realm.sync.permissions.Permission;
import io.realm.sync.permissions.PermissionUser;
import io.realm.sync.permissions.RealmPermissions;
import io.realm.sync.permissions.RealmPrivileges;
import io.realm.sync.permissions.Role;

import static io.realm.SyncTestUtils.createTestUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ObjectLevelPermissionsTest {

    private static String REALM_URI = "realm://objectserver.realm.io/~/default";

    private SyncConfiguration configuration;
    private SyncUser user;

    @Rule
    public final TestSyncConfigurationFactory configFactory = new TestSyncConfigurationFactory();

    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();
    private Realm realm;
    private DynamicRealm dynamicRealm;

    @RealmModule(classes = { AllJavaTypes.class })
    public static class TestModule {
    }

    @Before
    public void setUp() {
        user = createTestUser();
        configuration = user.createConfiguration(REALM_URI)
                .modules(new TestModule())
                .build();
        realm = Realm.getInstance(configuration);
        dynamicRealm = DynamicRealm.getInstance(configuration);
    }

    @After
    public void tearDown() {
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
        if (dynamicRealm != null && !dynamicRealm.isClosed()) {
            dynamicRealm.close();
        }
    }

    @Test
    public void getPrivileges_realm_localDefaults() {
        RealmPrivileges privileges = realm.getPrivileges();
        assertFullAccess(privileges);

        privileges = dynamicRealm.getPrivileges();
        assertFullAccess(privileges);
    }

    @Test
    public void getPrivileges_realm_revokeLocally() {
        realm.executeTransaction(r -> {
           Role role = realm.getRoles().where().equalTo("name", "everyone").findFirst();
           role.removeMember(user.getIdentity());
        });

        RealmPrivileges privileges = realm.getPrivileges();
        assertNoAccess(privileges);

        privileges = dynamicRealm.getPrivileges();
        assertNoAccess(privileges);
    }

    @Test
    public void getPrivileges_class_localDefaults() {
        ClassPrivileges privileges = realm.getPrivileges(AllJavaTypes.class);
        assertFullAccess(privileges);

        privileges = dynamicRealm.getPrivileges(AllJavaTypes.CLASS_NAME);
        assertFullAccess(privileges);
    }

    @Test
    public void getPrivileges_class_revokeLocally() {
        realm.executeTransaction(r -> {
            Role role = realm.getRoles().where().equalTo("name", "everyone").findFirst();
            role.removeMember(user.getIdentity());
        });

        ClassPrivileges privileges = realm.getPrivileges(AllJavaTypes.class);
        assertNoAccess(privileges);

        privileges = dynamicRealm.getPrivileges(AllJavaTypes.CLASS_NAME);
        assertNoAccess(privileges);
    }

    @Test
    public void getPrivileges_object_localDefaults() {
        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class, 0);
        realm.commitTransaction();
        assertFullAccess(realm.getPrivileges(obj));

        dynamicRealm.beginTransaction();
        DynamicRealmObject dynamicObject = dynamicRealm.createObject(AllJavaTypes.CLASS_NAME, 1);
        dynamicRealm.commitTransaction();
        assertFullAccess(dynamicRealm.getPrivileges(dynamicObject));
    }

    @Test
    public void getPrivileges_object_revokeLocally() {
        realm.executeTransaction(r -> {
            Role role = realm.getRoles().where().equalTo("name", "everyone").findFirst();
            role.removeMember(user.getIdentity());
        });

        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class, 0);
        realm.commitTransaction();
        assertNoAccess(realm.getPrivileges(obj));

        dynamicRealm.beginTransaction();
        DynamicRealmObject dynamicObject = dynamicRealm.createObject(AllJavaTypes.CLASS_NAME, 1);
        dynamicRealm.commitTransaction();
        assertNoAccess(dynamicRealm.getPrivileges(dynamicObject));
    }

    @Test
    public void getPrivileges_closedRealmThrows() {
        realm.close();
        try {
            realm.getPrivileges();
            fail();
        } catch(IllegalStateException ignored) {
        }

        try {
            realm.getPrivileges(AllJavaTypes.class);
            fail();
        } catch(IllegalStateException ignored) {
        }

        try {
            //noinspection ConstantConditions
            realm.getPrivileges((RealmModel) null);
            fail();
        } catch(IllegalStateException ignored) {
        }

        dynamicRealm.close();
        try {
            dynamicRealm.getPrivileges();
            fail();
        } catch(IllegalStateException ignored) {
        }

        try {
            dynamicRealm.getPrivileges(AllJavaTypes.CLASS_NAME);
            fail();
        } catch(IllegalStateException ignored) {
        }

        try {
            //noinspection ConstantConditions
            dynamicRealm.getPrivileges((RealmModel) null);
            fail();
        } catch(IllegalStateException ignored) {
        }
    }

    @Test
    public void getPrivileges_wrongThreadThrows() throws InterruptedException {
        Thread thread = new Thread(() -> {
            try {
                realm.getPrivileges();
                fail();
            } catch(IllegalStateException ignored) {
            }

            try {
                realm.getPrivileges(AllJavaTypes.class);
                fail();
            } catch(IllegalStateException ignored) {
            }

            try {
                //noinspection ConstantConditions
                realm.getPrivileges((RealmModel) null);
                fail();
            } catch(IllegalStateException ignored) {
            }

            try {
                dynamicRealm.getPrivileges();
                fail();
            } catch(IllegalStateException ignored) {
            }

            try {
                dynamicRealm.getPrivileges(AllJavaTypes.CLASS_NAME);
                fail();
            } catch(IllegalStateException ignored) {
            }

            try {
                //noinspection ConstantConditions
                dynamicRealm.getPrivileges((RealmModel) null);
                fail();
            } catch(IllegalStateException ignored) {
            }
        });
        thread.start();
        thread.join(TestHelper.STANDARD_WAIT_SECS * 1000);
    }

    @Test
    public void getPrivileges_class_notPartofSchemaThrows() {
        try {
            realm.getPrivileges(Dog.class);
            fail();
        } catch (RealmException ignore) {
        }

        try {
            dynamicRealm.getPrivileges("Dog");
            fail();
        } catch (RealmException ignore) {
        }
    }

    @Test
    public void getPrivileges_class_nullThrows() {
        try {
            //noinspection ConstantConditions
            realm.getPrivileges((Class<? extends RealmModel>) null);
            fail();
        } catch (IllegalArgumentException ignore) {
        }

        try {
            //noinspection ConstantConditions
            dynamicRealm.getPrivileges((String) null);
            fail();
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void getPrivileges_object_nullThrows() {
        try {
            //noinspection ConstantConditions
            realm.getPrivileges((RealmModel) null);
            fail();
        } catch (IllegalArgumentException ignore) {
        }

        try {
            //noinspection ConstantConditions
            dynamicRealm.getPrivileges((DynamicRealmObject) null);
            fail();
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPrivileges_object_unmanagedThrows() {
        // DynamicRealm do not support unmanaged DynamicRealmObjects
        realm.getPrivileges(new AllJavaTypes(0));
    }

    @Test
    public void getPrivileges_object_wrongRealmThrows() {
        Realm otherRealm = Realm.getInstance(configFactory.createConfiguration("other"));
        otherRealm.beginTransaction();
        AllJavaTypes obj = otherRealm.createObject(AllJavaTypes.class, 0);
        try {
            realm.getPrivileges(obj);
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            otherRealm.close();
        }
    }


    @Test
    public void getPermissions() {
        // Typed RealmPermissions
        RealmPermissions realmPermissions = realm.getPermissions();
        RealmList<Permission> list = realmPermissions.getPermissions();
        assertEquals(1, list.size());
        assertEquals("everyone", list.first().getRole().getName());
        assertFullAccess(list.first());

//        // FIXME: Dynamic RealmPermissions - Until support is enabled
//        realmPermissions = dynamicRealm.getPermissions();
//        list = realmPermissions.getPermissions();
//        assertEquals(1, list.size());
//        assertEquals("everyone", list.first().getRole().getName());
//        assertFullAccess(list.first());
    }

    @Test
    public void getPermissions_wrongThreadThrows() throws InterruptedException {
        Thread t = new Thread(() -> {
            try {
                realm.getPermissions();
                fail();
            } catch (IllegalStateException ignore) {
            }

// FIXME: Disabled until support is enabled
//            try {
//                dynamicRealm.getPermissions();
//                fail();
//            } catch (IllegalStateException ignore) {
//            }
        });
        t.start();
        t.join(TestHelper.STANDARD_WAIT_SECS * 1000);
    }

    @Test
    public void getPermissions_closedRealmThrows() {
        realm.close();
        try {
            realm.getPermissions();
            fail();
        } catch (IllegalStateException ignore) {
        }

// FIXME Disabled until support is enabled
//        dynamicRealm.close();
//        try {
//            dynamicRealm.getPermissions();
//            fail();
//        } catch (IllegalStateException ignore) {
//        }
    }

    @Test
    public void getClassPermissions() {
        // Typed RealmPermissions
        ClassPermissions classPermissions = realm.getPermissions(AllJavaTypes.class);
        assertEquals("AllJavaTypes", classPermissions.getName());
        RealmList<Permission> list = classPermissions.getPermissions();
        assertEquals(1, list.size());
        assertEquals("everyone", list.first().getRole().getName());
        assertFullAccess(list.first());

        // FIXME: Dynamic RealmPermissions - Disabled until support is enabled
//        classPermissions = dynamicRealm.getPermissions(AllJavaTypes.CLASS_NAME);
//        assertEquals("AllJavaTypes", classPermissions.getName());
//        list = classPermissions.getPermissions();
//        assertEquals(1, list.size());
//        assertEquals("everyone", list.first().getRole().getName());
//        assertDefaultAccess(list.first());
    }

    @Test
    public void getClassPermissions_wrongThreadThrows() throws InterruptedException {
        Thread t = new Thread(() -> {
            try {
                realm.getPermissions(AllJavaTypes.class);
                fail();
            } catch (IllegalStateException ignore) {
            }

// FIXME: Disabled until support is enabled
//            try {
//                dynamicRealm.getPermissions(AllJavaTypes.CLASS_NAME);
//                fail();
//            } catch (IllegalStateException ignore) {
//            }
        });
        t.start();
        t.join(TestHelper.STANDARD_WAIT_SECS * 1000);
    }

    @Test
    public void getClassPermissions_closedRealmThrows() {
        realm.close();
        try {
            realm.getPermissions(AllJavaTypes.class);
            fail();
        } catch (IllegalStateException ignore) {
        }

// FIXME: Disabled until support is enabled
//        dynamicRealm.close();
//        try {
//            dynamicRealm.getPermissions(AllJavaTypes.CLASS_NAME);
//            fail();
//        } catch (IllegalStateException ignore) {
//        }
    }

    @Test
    public void userPrivateRole() {
        RealmResults<PermissionUser> permissionUsers = realm.where(PermissionUser.class).findAll();
        assertEquals(1, permissionUsers.size());

        PermissionUser permissionUser = permissionUsers.get(0);
        assertNotNull(permissionUser);
        Role role = permissionUser.getPrivateRole();
        assertNotNull(role);

        assertEquals("__User:" + user.getIdentity(), role.getName());
        assertTrue(role.hasMember(user.getIdentity()));
    }

    @Test
    public void userPrivateRoleNotAvailableBeforeSyncClientCreated() {
        realm.beginTransaction();
        PermissionUser permissionUser = realm.createObject(PermissionUser.class, "id123");
        realm.commitTransaction();

        Role builtInRole = permissionUser.getPrivateRole();
        assertNull(builtInRole);
        permissionUser = realm.where(PermissionUser.class).equalTo("id", "id123").findFirst();
        assertNull(permissionUser.getPrivateRole());
        assertTrue(permissionUser.getRoles().isEmpty());
    }

    @Test
    public void getRoles() {
        RealmResults<Role> roles = realm.getRoles();
        assertEquals(2, roles.size());

        roles = roles.where().sort("name").findAll();
        Role role = roles.get(0);
        assertEquals("__User:" + user.getIdentity(), role.getName());
        assertTrue(role.hasMember(user.getIdentity()));

        role = roles.get(1);
        assertEquals("everyone", role.getName());
        assertTrue(role.hasMember(user.getIdentity()));

    }

    @Test
    public void getRoles_wrongThreadThrows() throws InterruptedException {
        Thread t = new Thread(() -> {
            try {
                realm.getRoles();
                fail();
            } catch (IllegalStateException ignore) {
            }
        });
        t.start();
        t.join(TestHelper.STANDARD_WAIT_SECS * 1000);

    }

    @Test
    public void getRoles_closedRealmThrows() {
        realm.close();
        try {
            realm.getRoles();
            fail();
        } catch (IllegalStateException ignore) {
        }

// FIXME: Until support is enabled
//        dynamicRealm.close();
//        try {
//            dynamicRealm.getRoles();
//            fail();
//        } catch (IllegalStateException ignore) {
//        }
    }
    @Test
    public void noPrivileges() {
        Role role = new Role("foo");
        Permission admin = new Permission.Builder(role).allPrivileges().build();
        assertFullAccess(admin);
    }

    @Test
    public void allPrivileges() {
        Role role = new Role("foo");
        Permission nobody = new Permission.Builder(role).noPrivileges().build();
        assertNoAccess(nobody);
    }

    private void assertFullAccess(RealmPrivileges privileges) {
        assertTrue(privileges.canRead());
        assertTrue(privileges.canUpdate());
        assertTrue(privileges.canSetPermissions());
        assertTrue(privileges.canModifySchema());
    }

    private void assertFullAccess(ClassPrivileges privileges) {
        assertTrue(privileges.canCreate());
        assertTrue(privileges.canRead());
        assertTrue(privileges.canUpdate());
        assertTrue(privileges.canQuery());
        assertTrue(privileges.canSetPermissions());
    }

    private void assertFullAccess(ObjectPrivileges privileges) {
        assertTrue(privileges.canRead());
        assertTrue(privileges.canUpdate());
        assertTrue(privileges.canDelete());
        assertTrue(privileges.canSetPermissions());
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

    private void assertNoAccess(Permission permission) {
        assertFalse(permission.canCreate());
        assertFalse(permission.canRead());
        assertFalse(permission.canUpdate());
        assertFalse(permission.canDelete());
        assertFalse(permission.canQuery());
        assertFalse(permission.canSetPermissions());
        assertFalse(permission.canModifySchema());
    }

    private void assertNoAccess(RealmPrivileges privileges) {
        assertFalse(privileges.canRead());
        assertFalse(privileges.canUpdate());
        assertFalse(privileges.canSetPermissions());
        assertFalse(privileges.canModifySchema());
    }

    private void assertNoAccess(ClassPrivileges privileges) {
        assertFalse(privileges.canCreate());
        assertFalse(privileges.canRead());
        assertFalse(privileges.canUpdate());
        assertFalse(privileges.canQuery());
        assertFalse(privileges.canSetPermissions());
    }

    private void assertNoAccess(ObjectPrivileges privileges) {
        assertFalse(privileges.canRead());
        assertFalse(privileges.canUpdate());
        assertFalse(privileges.canDelete());
        assertFalse(privileges.canSetPermissions());
    }

}
