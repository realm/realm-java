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
import io.realm.entities.AllTypes;
import io.realm.entities.Dog;
import io.realm.exceptions.RealmException;
import io.realm.rule.RunInLooperThread;
import io.realm.sync.permissions.RealmPrivileges;

import static io.realm.util.SyncTestUtils.createTestUser;
import static org.junit.Assert.assertFalse;
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

    @RealmModule(classes = { AllJavaTypes.class })
    public static class TestModule {
    }

    @Before
    public void setUp() {
        user = createTestUser();
        configuration = new SyncConfiguration.Builder(user, REALM_URI)
                .partialRealm()
                .modules(new TestModule())
                .build();
        realm = Realm.getInstance(configuration);
    }

    @After
    public void tearDown() {
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }

    @Test
    public void getPrivileges_realm_localDefaults() {
        RealmPrivileges privileges = realm.getPrivileges();
        assertFullAccess(privileges);
    }

    @Test
    public void getPrivileges_class_localDefaults() {
        RealmPrivileges privileges = realm.getPrivileges(AllJavaTypes.class);
        assertDefaultAccess(privileges);
    }

    @Test
    public void getPrivileges_object_localDefaults() {
        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class, 0);
        realm.commitTransaction();
        assertDefaultAccess(realm.getPrivileges(obj));
    }

    private void assertDefaultAccess(RealmPrivileges privileges) {
        assertFalse(privileges.canCreate());
        assertTrue(privileges.canRead());
        assertTrue(privileges.canUpdate());
        assertFalse(privileges.canDelete());
        assertTrue(privileges.canQuery());
        assertTrue(privileges.canSetPermissions());
        assertTrue(privileges.canModifySchema());
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

    private void assertNoAccess(RealmPrivileges privileges) {
        assertFalse(privileges.canCreate());
        assertFalse(privileges.canRead());
        assertFalse(privileges.canUpdate());
        assertFalse(privileges.canDelete());
        assertFalse(privileges.canQuery());
        assertFalse(privileges.canSetPermissions());
        assertFalse(privileges.canModifySchema());
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
    }

    @Test
    public void getPrivileges_wrongThreadThrows() throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
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
            }
        });
        thread.start();
        thread.join(TestHelper.STANDARD_WAIT_SECS * 1000);
    }

    @Test(expected = RealmException.class)
    public void getPrivileges_class_notPartofSchemaThrows() {
        realm.getPrivileges(Dog.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPrivileges_class_nullThrows() {
        //noinspection ConstantConditions
        realm.getPrivileges((Class<? extends RealmModel>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPrivileges_object_nullThrows() {
        //noinspection ConstantConditions
        realm.getPrivileges((RealmModel) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPrivileges_object_unmanagedThrows() {
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
}
