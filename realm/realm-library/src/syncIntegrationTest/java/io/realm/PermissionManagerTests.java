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

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;

import io.realm.log.RealmLog;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.UserFactory;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestSyncConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class PermissionManagerTests extends BaseIntegrationTest {

    @Rule
    public RunInLooperThread looperThread = new RunInLooperThread();

    @Rule
    public final TestSyncConfigurationFactory configurationFactory = new TestSyncConfigurationFactory();

    private SyncUser user;

    @Before
    public void setUpTest() {
        user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        looperThread.runAfterTest(new Runnable() {
            @Override
            public void run() {
                user.logout();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void getPermissions_returnLoadedResults() {
        PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);
        pm.getPermissions(new PermissionManager.Callback<RealmResults<Permission>>() {
            @Override
            public void onSuccess(RealmResults<Permission> permissions) {
                assertTrue(permissions.isLoaded());
                assertInitialPermissions(permissions);
                looperThread.testComplete();
            }

            @Override
            public void onError(ObjectServerError error) {
                fail(error.toString());
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void getPermissions_noLongerValidWhenPermissionManagerIsClosed() {
        final PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);
        pm.getPermissions(new PermissionManager.Callback<RealmResults<Permission>>() {
            @Override
            public void onSuccess(RealmResults<Permission> permissions) {
                try {
                    assertTrue(permissions.isValid());
                    pm.close();
                    assertFalse(permissions.isValid());
                } finally {
                    user.logout();
                }
                looperThread.testComplete();
            }

            @Override
            public void onError(ObjectServerError error) {
                fail(error.toString());
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void getPermissions_updatedWithNewRealms() {
        PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);
        pm.getPermissions(new PermissionManager.Callback<RealmResults<Permission>>() {
            @Override
            public void onSuccess(RealmResults<Permission> permissions) {
                assertTrue(permissions.isLoaded());
                assertInitialPermissions(permissions);

                // Create new Realm, which should create a new Permission entry
                SyncConfiguration config2 = new SyncConfiguration.Builder(user, Constants.USER_REALM_2).build();
                final Realm secondRealm = Realm.getInstance(config2);
                looperThread.keepStrongReference(secondRealm);

                // Wait for the permission Result to report the new Realm
                looperThread.keepStrongReference(permissions);
                permissions.addChangeListener(new RealmChangeListener<RealmResults<Permission>>() {
                    @Override
                    public void onChange(RealmResults<Permission> permissions) {
                        assertEquals(2, permissions.size());
                        secondRealm.close();
                        looperThread.testComplete();
                    }
                });
            }

            @Override
            public void onError(ObjectServerError error) {
                fail("Could not open Realm: " + error.toString());
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void getPermissions_closed() throws IOException {
        PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);
        pm.close();

        pm.getPermissions(new PermissionManager.Callback<RealmResults<Permission>>() {
            @Override
            public void onSuccess(RealmResults<Permission> permissions) {
                fail();
            }

            @Override
            public void onError(ObjectServerError error) {
                assertEquals(ErrorCode.UNKNOWN, error.getErrorCode());
                assertEquals(IllegalStateException.class, error.getException().getClass());
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void permissionManagerAsyncTask_handlePermissionRealmError() throws NoSuchFieldException, IllegalAccessException {
        PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);

        // Simulate error in the permission Realm
        Field permissionConfigField = pm.getClass().getDeclaredField("permissionRealmError");
        permissionConfigField.setAccessible(true);
        final ObjectServerError expectedError = new ObjectServerError(ErrorCode.UNKNOWN, "Boom");
        permissionConfigField.set(pm, expectedError);

        PermissionManager.Callback <Void> callback = new PermissionManager.Callback <Void>() {
            @Override
            public void onSuccess(Void result) {
                fail();
            }

            @Override
            public void onError(ObjectServerError error) {
                assertEquals(expectedError, error);
                looperThread.testComplete();
            }
        };

        // Create dummy task that can trigger the error reporting
        runTask(pm, callback);
    }

    @Test
    @RunTestInLooperThread
    public void permissionManagerAsyncTask_handleManagementRealmError() throws NoSuchFieldException, IllegalAccessException {
        PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);

        // Simulate error in the permission Realm
        final ObjectServerError expectedError = new ObjectServerError(ErrorCode.UNKNOWN, "Boom");
        setRealmError(pm, "managementRealmError", expectedError);

        PermissionManager.Callback <Void> callback = new PermissionManager.Callback <Void>() {
            @Override
            public void onSuccess(Void result) {
                fail();
            }

            @Override
            public void onError(ObjectServerError error) {
                assertEquals(expectedError, error);
                looperThread.testComplete();
            }
        };

        // Create dummy task that can trigger the error reporting
        runTask(pm, callback);
    }

    @Test
    @RunTestInLooperThread
    public void permissionManagerAsyncTask_handleTwoErrorsSameErrorCode() throws NoSuchFieldException, IllegalAccessException {
        PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);

        // Simulate error in the permission Realm
        setRealmError(pm, "managementRealmError", new ObjectServerError(ErrorCode.CONNECTION_CLOSED, "Boom1"));

        // Simulate error in the management Realm
        setRealmError(pm, "permissionRealmError", new ObjectServerError(ErrorCode.CONNECTION_CLOSED, "Boom2"));

        PermissionManager.Callback <Void> callback = new PermissionManager.Callback <Void>() {
            @Override
            public void onSuccess(Void result) {
                fail();
            }

            @Override
            public void onError(ObjectServerError error) {
                assertEquals(ErrorCode.CONNECTION_CLOSED, error.getErrorCode());
                assertTrue(error.toString().contains("Boom1"));
                assertTrue(error.toString().contains("Boom2"));
                looperThread.testComplete();
            }
        };

        // Create dummy task that can trigger the error reporting
        runTask(pm, callback);
    }

    @Test
    @RunTestInLooperThread
    public void permissionManagerAsyncTask_handleTwoErrorsDifferentErrorCode() throws NoSuchFieldException, IllegalAccessException {
        PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);

        // Simulate error in the permission Realm
        setRealmError(pm, "managementRealmError", new ObjectServerError(ErrorCode.CONNECTION_CLOSED, "Boom1"));

        // Simulate error in the management Realm
        setRealmError(pm, "permissionRealmError", new ObjectServerError(ErrorCode.SESSION_CLOSED, "Boom2"));

        PermissionManager.Callback <Void> callback = new PermissionManager.Callback <Void>() {
            @Override
            public void onSuccess(Void result) {
                fail();
            }

            @Override
            public void onError(ObjectServerError error) {
                assertEquals(ErrorCode.UNKNOWN, error.getErrorCode());
                assertTrue(error.toString().contains(ErrorCode.CONNECTION_CLOSED.toString()));
                assertTrue(error.toString().contains(ErrorCode.SESSION_CLOSED.toString()));
                looperThread.testComplete();
            }
        };

        // Create dummy task that can trigger the error reporting
        runTask(pm, callback);
    }

    private void setRealmError(PermissionManager pm, String fieldName, ObjectServerError error) throws NoSuchFieldException,
            IllegalAccessException {
        Field managementRealmErrorField = pm.getClass().getDeclaredField(fieldName);
        managementRealmErrorField.setAccessible(true);
        managementRealmErrorField.set(pm, error);
    }

    private void runTask(final PermissionManager pm, final PermissionManager.Callback<Void> callback) {
        new PermissionManager.AsyncTask<Void>(pm, callback) {
            @Override
            public void run() {
                if (!checkAndReportInvalidState()) {
                    fail();
                }
            }
        }.run();
    }

    /**
     * The initial set of permissions of ROS is timing dependant. This method will identify the possible know starting
     * states and fail if neither of these can be verified.
     */
    private void assertInitialPermissions(RealmResults<Permission> permissions) {
        // For a new user, the PermissionManager should contain 1 entry for the __permission Realm, but we are
        // creating the __management Realm at the same time, so this might be here as well.
        if (permissions.size() == 1) {
            // FIXME It is very unpredictable which Permission is returned. This needs to be fixed.
            Permission permission = permissions.first();
            assertTrue(permission.getPath().endsWith("__permission") || permission.getPath().endsWith("__management"));
        } else if (permissions.size() == 2) {
            assertTrue("Failed: " + permissions.get(0).toString(), permissions.get(0).getPath().endsWith("__permission"));
            assertTrue("Failed: " + permissions.get(1).toString(), permissions.get(1).getPath().endsWith("__management"));
        } else {
            fail("Permission Realm contains unknown permissions: " + Arrays.toString(permissions.toArray()));
        }
    }
}
