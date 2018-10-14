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

import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.entities.AllJavaTypes;
import io.realm.internal.OsRealmConfig;
import io.realm.log.RealmLog;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.UserFactory;
import io.realm.permissions.AccessLevel;
import io.realm.permissions.Permission;
import io.realm.permissions.PermissionOffer;
import io.realm.permissions.PermissionRequest;
import io.realm.permissions.UserCondition;
import io.realm.rule.RunTestInLooperThread;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
@Ignore // FIXME: Temporary disable unit tests due to lates (3.0.0-alpha.2) ROS having issues. Re-enable once ROS is stable again.
public class PermissionManagerTests extends StandardIntegrationTest {

    private SyncUser user;

    @Before
    public void setUpTest() {
        user = UserFactory.createUniqueUser();
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void getPermissions_returnLoadedResults() {
        PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);
        pm.getPermissions(new PermissionManager.PermissionsCallback() {
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
    @RunTestInLooperThread(emulateMainThread = true)
    public void getPermissions_noLongerValidWhenPermissionManagerIsClosed() {
        final PermissionManager pm = user.getPermissionManager();
        pm.getPermissions(new PermissionManager.PermissionsCallback() {
            @Override
            public void onSuccess(RealmResults<Permission> permissions) {
                assertTrue(permissions.isValid());
                pm.close();
                assertFalse(permissions.isValid());
                looperThread.testComplete();
            }

            @Override
            public void onError(ObjectServerError error) {
                pm.close();
                fail(error.toString());
            }
        });
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void getPermissions_updatedWithNewRealms() {
        final PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);
        pm.getPermissions(new PermissionManager.PermissionsCallback() {
            @Override
            public void onSuccess(RealmResults<Permission> permissions) {
                assertTrue(permissions.isLoaded());
                assertInitialPermissions(permissions);

                // Create new Realm, which should create a new Permission entry
                SyncConfiguration config2 = user.createConfiguration(Constants.USER_REALM_2)
                        .schema(AllJavaTypes.class)
                        .errorHandler(new SyncSession.ErrorHandler() {
                            @Override
                            public void onError(SyncSession session, ObjectServerError error) {
                                fail(error.toString());
                            }
                        })
                        .build();
                final Realm secondRealm = Realm.getInstance(config2);
                looperThread.closeAfterTest(secondRealm);
                // Wait for the permission Result to report the new Realms
                looperThread.keepStrongReference(permissions);
                permissions.addChangeListener(new RealmChangeListener<RealmResults<Permission>>() {
                    @Override
                    public void onChange(RealmResults<Permission> permissions) {
                        RealmLog.error(String.format("2ndCallback: Size: %s, Permissions: %s", permissions.size(), Arrays.toString(permissions.toArray())));
                        Permission p = permissions.where().endsWith("path", "tests2").findFirst();
                        if (p != null) {
                            assertTrue(p.mayRead());
                            assertTrue(p.mayWrite());
                            assertTrue(p.mayManage());
                            looperThread.testComplete();
                        }
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
    @RunTestInLooperThread(emulateMainThread = true)
    public void getPermissions_updatedWithNewRealms_stressTest() {
        final int TEST_SIZE = 10;
        final PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);
        pm.getPermissions(new PermissionManager.PermissionsCallback() {
            @Override
            public void onSuccess(RealmResults<Permission> permissions) {
                assertTrue(permissions.isLoaded());
                assertInitialPermissions(permissions);

                for (int i = 0; i < TEST_SIZE; i++) {
                    SyncConfiguration configNew = user.createConfiguration("realm://" + Constants.HOST + "/~/test" + i)
                            .schema(AllJavaTypes.class)
                            .build();
                    Realm newRealm = Realm.getInstance(configNew);
                    looperThread.closeAfterTest(newRealm);
                }

                // Wait for the permission Result to report the new Realms
                looperThread.keepStrongReference(permissions);
                permissions.addChangeListener(new RealmChangeListener<RealmResults<Permission>>() {
                    @Override
                    public void onChange(RealmResults<Permission> permissions) {
                        RealmLog.error(String.format("Size: %s, Permissions: %s", permissions.size(), Arrays.toString(permissions.toArray())));
                        Permission p = permissions.where().endsWith("path", "test" + (TEST_SIZE - 1)).findFirst();
                        if (p != null) {
                            assertTrue(p.mayRead());
                            assertTrue(p.mayWrite());
                            assertTrue(p.mayManage());
                            looperThread.testComplete();
                        }
                    }
                });
            }

            @Override
            public void onError(ObjectServerError error) {
                fail(error.toString());
            }
        });
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void getPermissions_closed() throws IOException {
        PermissionManager pm = user.getPermissionManager();
        pm.close();

        thrown.expect(IllegalStateException.class);
        pm.getPermissions(new PermissionManager.PermissionsCallback() {
            @Override
            public void onSuccess(RealmResults<Permission> permissions) {
                fail();
            }
            @Override
            public void onError(ObjectServerError error) { fail(); }
        });
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void getPermissions_clientReset() {
        final PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);
        pm.getPermissions(new PermissionManager.PermissionsCallback() {
            @Override
            public void onSuccess(RealmResults<Permission> permissions) {
                // Simulate reset after first request succeeded to make sure that session is
                // alive.
                SyncManager.simulateClientReset(SyncManager.getSession(pm.permissionRealmConfig));
                pm.getPermissions(new PermissionManager.PermissionsCallback() {
                    @Override
                    public void onSuccess(RealmResults<Permission> permissions) {
                        fail();
                    }

                    @Override
                    public void onError(ObjectServerError error) {
                        assertEquals(ErrorCode.CLIENT_RESET, error.getErrorCode());
                        looperThread.testComplete();
                    }
                });
            }

            @Override
            public void onError(ObjectServerError error) {
                fail(error.toString());
            }
        });
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void getPermissions_addTaskAfterClientReset() {
        final PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);
        pm.getPermissions(new PermissionManager.PermissionsCallback() {
            @Override
            public void onSuccess(RealmResults<Permission> permissions) {
                // Simulate reset after first request succeeded to make sure that session is
                // alive.
                SyncManager.simulateClientReset(SyncManager.getSession(pm.permissionRealmConfig));

                // 1. Run task that fail
                pm.getPermissions(new PermissionManager.PermissionsCallback() {
                    @Override
                    public void onSuccess(RealmResults<Permission> permissions) {
                        fail();
                    }

                    @Override
                    public void onError(ObjectServerError error) {
                        assertEquals(ErrorCode.CLIENT_RESET, error.getErrorCode());
                        // 2. Then try to add another
                        pm.getDefaultPermissions(new PermissionManager.PermissionsCallback() {
                            @Override
                            public void onSuccess(RealmResults<Permission> permissions) {
                                fail();
                            }

                            @Override
                            public void onError(ObjectServerError error) {
                                assertEquals(ErrorCode.CLIENT_RESET, error.getErrorCode());
                                looperThread.testComplete();
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(ObjectServerError error) {
                fail(error.toString());
            }
        });
    }

    @Ignore("The PermissionManager can only be opened from the main thread")
    @Test
    public void clientResetOnMultipleThreads() {

        HandlerThread thread1 = new HandlerThread("handler1");
        thread1.start();
        Handler handler1 = new Handler(thread1.getLooper());

        HandlerThread thread2 = new HandlerThread("handler2");
        thread2.start();
        Handler handler2 = new Handler(thread1.getLooper());

        final AtomicReference<PermissionManager> pm1 = new AtomicReference<>(null);
        final AtomicReference<PermissionManager> pm2 = new AtomicReference<>(null);

        final CountDownLatch pmsOpened = new CountDownLatch(1);

        // 1) Thread 1: Open PermissionManager and check permissions
        handler1.post(new Runnable() {
            @Override
            public void run() {
                PermissionManager pm = user.getPermissionManager();
                pm1.set(pm);
                pm.getPermissions(new PermissionManager.PermissionsCallback() {
                    @Override
                    public void onSuccess(RealmResults<Permission> permissions) {
                        assertInitialPermissions(permissions);
                        pmsOpened.countDown();
                    }

                    @Override
                    public void onError(ObjectServerError error) {
                        fail(error.toString());
                    }
                });
            }
        });

        // 2) Thread 2: Open PermissionManager and check permissions
        handler2.post(new Runnable() {
            @Override
            public void run() {
                PermissionManager pm = user.getPermissionManager();
                pm2.set(pm);
                pm.getPermissions(new PermissionManager.PermissionsCallback() {
                    @Override
                    public void onSuccess(RealmResults<Permission> permissions) {
                        assertInitialPermissions(permissions);
                        pmsOpened.countDown();
                    }

                    @Override
                    public void onError(ObjectServerError error) {
                        fail(error.toString());
                    }
                });
            }
        });

        TestHelper.awaitOrFail(pmsOpened);

        // 3) Trigger Client Reset
        SyncManager.simulateClientReset(SyncManager.getSession(pm1.get().permissionRealmConfig));
        SyncManager.simulateClientReset(SyncManager.getSession(pm2.get().permissionRealmConfig));

        // 4) Thread 1: Attempt to get permissions should trigger a Client Reset
        final CountDownLatch clientResetThread1 = new CountDownLatch(1);
        final CountDownLatch clientResetThread2 = new CountDownLatch(1);
        handler1.post(new Runnable() {
            @Override
            public void run() {
                final PermissionManager pm = pm1.get();
                pm.getPermissions(new PermissionManager.PermissionsCallback() {
                    @Override
                    public void onSuccess(RealmResults<Permission> permissions) {
                        fail("Client reset should have been triggered");
                    }

                    @Override
                    public void onError(ObjectServerError error) {
                        assertEquals(ErrorCode.CLIENT_RESET, error.getErrorCode());
                        pm.close();
                        assertFalse(new File(pm.permissionRealmConfig.getPath()).exists());
                        clientResetThread1.countDown();
                    }
                });
            }
        });

        // 5) Thread 2: Attempting to get permissions should also trigger a Client Reset even though
        //    Thread 1 just executed it
        TestHelper.awaitOrFail(clientResetThread1);
        handler2.post(new Runnable() {
            @Override
            public void run() {
                final PermissionManager pm = pm2.get();
                pm.getPermissions(new PermissionManager.PermissionsCallback() {
                    @Override
                    public void onSuccess(RealmResults<Permission> permissions) {
                        fail("Client reset should have been triggered");
                    }

                    @Override
                    public void onError(ObjectServerError error) {
                        assertEquals(ErrorCode.CLIENT_RESET, error.getErrorCode());
                        pm.close();
                        clientResetThread2.countDown();
                    }
                });
            }
        });
        TestHelper.awaitOrFail(clientResetThread2);

        // 6) After closing the PermissionManager, re-opening it again should work fine
        final CountDownLatch newPmOpenedAndReady = new CountDownLatch(1);
        handler1.post(new Runnable() {
            @Override
            public void run() {
                final PermissionManager pm = user.getPermissionManager();
                pm.getPermissions(new PermissionManager.PermissionsCallback() {
                    @Override
                    public void onSuccess(RealmResults<Permission> permissions) {
                        assertInitialPermissions(permissions);
                        pm.close();
                        newPmOpenedAndReady.countDown();
                    }

                    @Override
                    public void onError(ObjectServerError error) {
                        fail(error.toString());
                    }
                });
            }
        });

        TestHelper.awaitOrFail(newPmOpenedAndReady);
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void getDefaultPermissions_returnLoadedResults() {
        PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);
        pm.getDefaultPermissions(new PermissionManager.PermissionsCallback() {
            @Override
            public void onSuccess(RealmResults<Permission> permissions) {
                assertTrue(permissions.isLoaded());
                assertInitialDefaultPermissions(permissions);
                looperThread.testComplete();
            }

            @Override
            public void onError(ObjectServerError error) {
                fail(error.toString());
            }
        });
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void getDefaultPermissions_noLongerValidWhenPermissionManagerIsClosed() {
        final PermissionManager pm = user.getPermissionManager();
        pm.getDefaultPermissions(new PermissionManager.PermissionsCallback() {
            @Override
            public void onSuccess(RealmResults<Permission> permissions) {
                try {
                    assertTrue(permissions.isValid());
                } finally {
                    pm.close();
                }
                assertFalse(permissions.isValid());
                looperThread.testComplete();
            }

            @Override
            public void onError(ObjectServerError error) {
                pm.close();
                fail(error.toString());
            }
        });
    }

    @Test
    @Ignore("FIXME Add once `setPermissions` are implemented")
    @RunTestInLooperThread(emulateMainThread = true)
    public void getDefaultPermissions_updatedWithNewRealms() {

    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void getDefaultPermissions_closed() throws IOException {
        PermissionManager pm = user.getPermissionManager();
        pm.close();

        thrown.expect(IllegalStateException.class);
        pm.getDefaultPermissions(new PermissionManager.PermissionsCallback() {
            @Override
            public void onSuccess(RealmResults<Permission> permissions) {
                fail();
            }
            @Override
            public void onError(ObjectServerError error) { fail(); }
        });
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void permissionManagerAsyncTask_handlePermissionRealmError() throws NoSuchFieldException, IllegalAccessException {
        PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);

        // Simulate error in the permission Realm
        Field permissionConfigField = pm.getClass().getDeclaredField("permissionRealmError");
        permissionConfigField.setAccessible(true);
        final ObjectServerError error = new ObjectServerError(ErrorCode.UNKNOWN, "Boom");
        permissionConfigField.set(pm, error);

        PermissionManager.ApplyPermissionsCallback callback = new PermissionManager.ApplyPermissionsCallback() {
            @Override
            public void onSuccess() {
                fail();
            }

            @Override
            public void onError(ObjectServerError error) {
                assertTrue(error.getErrorMessage().startsWith("Error occurred in Realm"));
                assertTrue(error.getErrorMessage().contains("Permission Realm"));
                assertEquals(ErrorCode.UNKNOWN, error.getErrorCode());
                looperThread.testComplete();
            }
        };

        // Create dummy task that can trigger the error reporting
        runTask(pm, callback);
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void permissionManagerAsyncTask_handleManagementRealmError() throws NoSuchFieldException, IllegalAccessException {
        PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);

        // Simulate error in the permission Realm
        final ObjectServerError error = new ObjectServerError(ErrorCode.UNKNOWN, "Boom");
        setRealmError(pm, "managementRealmError", error);

        PermissionManager.ApplyPermissionsCallback callback = new PermissionManager.ApplyPermissionsCallback() {
            @Override
            public void onSuccess() {
                fail();
            }

            @Override
            public void onError(ObjectServerError error) {
                assertTrue(error.getErrorMessage().startsWith("Error occurred in Realm"));
                assertTrue(error.getErrorMessage().contains("Management Realm"));
                assertEquals(ErrorCode.UNKNOWN, error.getErrorCode());
                looperThread.testComplete();
            }
        };

        // Create dummy task that can trigger the error reporting
        runTask(pm, callback);
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void permissionManagerAsyncTask_handleTwoErrorsSameErrorCode() throws NoSuchFieldException, IllegalAccessException {
        PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);

        // Simulate error in the permission Realm
        setRealmError(pm, "managementRealmError", new ObjectServerError(ErrorCode.CONNECTION_CLOSED, "Boom1"));

        // Simulate error in the management Realm
        setRealmError(pm, "permissionRealmError", new ObjectServerError(ErrorCode.CONNECTION_CLOSED, "Boom2"));

        PermissionManager.ApplyPermissionsCallback callback = new PermissionManager.ApplyPermissionsCallback() {
            @Override
            public void onSuccess() {
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
    @RunTestInLooperThread(emulateMainThread = true)
    public void permissionManagerAsyncTask_handleTwoErrorsDifferentErrorCode() throws NoSuchFieldException, IllegalAccessException {
        PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);

        // Simulate error in the permission Realm
        setRealmError(pm, "managementRealmError", new ObjectServerError(ErrorCode.CONNECTION_CLOSED, "Boom1"));

        // Simulate error in the management Realm
        setRealmError(pm, "permissionRealmError", new ObjectServerError(ErrorCode.SESSION_CLOSED, "Boom2"));

        PermissionManager.ApplyPermissionsCallback callback = new PermissionManager.ApplyPermissionsCallback() {
            @Override
            public void onSuccess() {
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

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void applyPermissions_nonAdminUserFails() {
        SyncUser user2 = UserFactory.createUniqueUser();
        String otherUsersUrl = createRemoteRealm(user2, "test");

        PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);

        // Create request for setting permissions on another users Realm,
        // i.e. user making the request do not have manage rights.
        UserCondition condition = UserCondition.userId(user.getIdentity());
        AccessLevel accessLevel = AccessLevel.WRITE;
        PermissionRequest request = new PermissionRequest(condition, otherUsersUrl, accessLevel);

        pm.applyPermissions(request, new PermissionManager.ApplyPermissionsCallback() {
            @Override
            public void onSuccess() {
                fail();
            }

            @Override
            public void onError(ObjectServerError error) {
                assertEquals(ErrorCode.ACCESS_DENIED, error.getErrorCode());
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void applyPermissions_wrongUrlFails() {
        String wrongUrl = createRemoteRealm(user, "test") + "-notexisting";

        PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);

        // Create request for setting permissions on another users Realm,
        // i.e. user making the request do not have manage rights.
        UserCondition condition = UserCondition.userId(user.getIdentity());
        AccessLevel accessLevel = AccessLevel.WRITE;
        PermissionRequest request = new PermissionRequest(condition, wrongUrl, accessLevel);

        pm.applyPermissions(request, new PermissionManager.ApplyPermissionsCallback() {
            @Override
            public void onSuccess() {
                fail();
            }

            @Override
            public void onError(ObjectServerError error) {
                // FIXME: Should be 614, see https://github.com/realm/ros/issues/429
                assertEquals(ErrorCode.INVALID_PARAMETERS, error.getErrorCode());
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void applyPermissions_withUserId() {
        final SyncUser user2 = UserFactory.createUniqueUser();
        String url = createRemoteRealm(user2, "test");
        PermissionManager pm2 = user2.getPermissionManager();
        looperThread.closeAfterTest(pm2);

        // Create request for giving `user` WRITE permissions to `user2`'s Realm.
        UserCondition condition = UserCondition.userId(user.getIdentity());
        AccessLevel accessLevel = AccessLevel.WRITE;
        PermissionRequest request = new PermissionRequest(condition, url, accessLevel);

        pm2.applyPermissions(request, new PermissionManager.ApplyPermissionsCallback() {
            @Override
            public void onSuccess() {
                PermissionManager pm = user.getPermissionManager();
                looperThread.closeAfterTest(pm);
                pm.getPermissions(new PermissionManager.PermissionsCallback() {
                    @Override
                    public void onSuccess(RealmResults<Permission> permissions) {
                        assertPermissionPresent(permissions, user, "/test", AccessLevel.WRITE);
                    }

                    @Override
                    public void onError(ObjectServerError error) {
                        fail(error.toString());
                    }
                });
            }

            @Override
            public void onError(ObjectServerError error) {
                fail(error.toString());
            }
        });
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void applyPermissions_withUsername() {
        String user1Username = TestHelper.getRandomEmail();
        String user2Username = TestHelper.getRandomEmail();
        final SyncUser user1 = UserFactory.createUser(user1Username);
        final SyncUser user2 = UserFactory.createUser(user2Username);
        PermissionManager pm1 = user1.getPermissionManager();
        looperThread.closeAfterTest(pm1);

        // Create request for giving `user2` WRITE permissions to `user1`'s Realm.
        UserCondition condition = UserCondition.username(user2Username);
        AccessLevel accessLevel = AccessLevel.WRITE;
        String url = createRemoteRealm(user1, "test");
        PermissionRequest request = new PermissionRequest(condition, url, accessLevel);

        pm1.applyPermissions(request, new PermissionManager.ApplyPermissionsCallback() {
            @Override
            public void onSuccess() {
                PermissionManager pm2 = user2.getPermissionManager();
                looperThread.closeAfterTest(pm2);
                pm2.getPermissions(new PermissionManager.PermissionsCallback() {
                    @Override
                    public void onSuccess(RealmResults<Permission> permissions) {
                        assertPermissionPresent(permissions, user2, user1.getIdentity() + "/test", AccessLevel.WRITE);
                    }

                    @Override
                    public void onError(ObjectServerError error) {
                        fail(error.toString());
                    }
                });
            }

            @Override
            public void onError(ObjectServerError error) {
                fail(error.toString());
            }
        });
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void applyPermissions_usersWithNoExistingPermissions() {
        final SyncUser user1 = UserFactory.createUser("user1@realm.io");
        final SyncUser user2 = UserFactory.createUser("user2@realm.io");
        PermissionManager pm1 = user1.getPermissionManager();
        looperThread.closeAfterTest(pm1);

        // Create request for giving all users with no existing permissions WRITE permissions to `user1`'s Realm.
        UserCondition condition = UserCondition.noExistingPermissions();
        AccessLevel accessLevel = AccessLevel.WRITE;
        final String url = createRemoteRealm(user1, "test");
        PermissionRequest request = new PermissionRequest(condition, url, accessLevel);

        pm1.applyPermissions(request, new PermissionManager.ApplyPermissionsCallback() {
            @Override
            public void onSuccess() {
                // Default permissions are not recorded in the __permission Realm for user2
                // Only way to check is by opening the Realm.
                SyncConfiguration config = user2.createConfiguration(url)
                        .schema(AllJavaTypes.class)
                        .waitForInitialRemoteData()
                        .errorHandler(new SyncSession.ErrorHandler() {
                            @Override
                            public void onError(SyncSession session, ObjectServerError error) {
                                fail(error.toString());
                            }
                        })
                        .build();

                RealmAsyncTask task = Realm.getInstanceAsync(config, new Realm.Callback() {
                    @Override
                    public void onSuccess(Realm realm) {
                        realm.close();
                        looperThread.testComplete();
                    }

                    @Override
                    public void onError(Throwable exception) {
                        fail(exception.toString());
                    }
                });
                looperThread.keepStrongReference(task);
            }

            @Override
            public void onError(ObjectServerError error) {
                fail(error.toString());
            }
        });
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void makeOffer() {
        PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);
        String url = createRemoteRealm(user, "test");

        PermissionOffer offer = new PermissionOffer(url, AccessLevel.WRITE);
        pm.makeOffer(offer, new PermissionManager.MakeOfferCallback() {
            @Override
            public void onSuccess(String offerToken) {
                assertNotNull(offerToken);
                looperThread.testComplete();
            }

            @Override
            public void onError(ObjectServerError error) {
                fail(error.toString());
            }
        });
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void makeOffer_noManageAccessThrows() {
        // User 2 creates a Realm
        SyncUser user2 = UserFactory.createUniqueUser();
        String url = createRemoteRealm(user2, "test");

        // User 1 tries to create an offer for it.
        PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);

        PermissionOffer offer = new PermissionOffer(url, AccessLevel.WRITE);
        pm.makeOffer(offer, new PermissionManager.MakeOfferCallback() {
            @Override
            public void onSuccess(String offerToken) {
                fail();
            }

            @Override
            public void onError(ObjectServerError error) {
                assertEquals(ErrorCode.ACCESS_DENIED, error.getErrorCode());
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void acceptOffer() {
        final String offerToken = createOffer(user, "test", AccessLevel.WRITE, null);

        final SyncUser user2 = UserFactory.createUniqueUser();
        final PermissionManager pm = user2.getPermissionManager();
        looperThread.closeAfterTest(pm);

        pm.acceptOffer(offerToken, new PermissionManager.AcceptOfferCallback() {
            @Override
            public void onSuccess(String url, Permission permission) {
                assertEquals("/" + user.getIdentity() + "/test", permission.getPath());
                assertTrue(permission.mayRead());
                assertTrue(permission.mayWrite());
                assertFalse(permission.mayManage());
                assertEquals(user2.getIdentity(), permission.getUserId());
                looperThread.testComplete();
            }

            @Override
            public void onError(ObjectServerError error) {
                fail(error.toString());
            }
        });
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void acceptOffer_invalidToken() {
        PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);
        pm.acceptOffer("wrong-token", new PermissionManager.AcceptOfferCallback() {
            @Override
            public void onSuccess(String url, Permission permission) {
                fail();
            }

            @Override
            public void onError(ObjectServerError error) {
                assertEquals(ErrorCode.INVALID_PARAMETERS, error.getErrorCode());
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    @Ignore("The offer is randomly accepted mostly on docker-02 SHIELD K1")
    public void acceptOffer_expiredThrows() {
        // Trying to guess how long CI is to process this. The offer cannot be created if it
        // already expired.
        long delayMillis = TimeUnit.SECONDS.toMillis(10);
        Date expiresAt = new Date(new Date().getTime() + delayMillis);
        final String offerToken = createOffer(user, "test", AccessLevel.WRITE, expiresAt);
        SystemClock.sleep(delayMillis); // Make sure that the offer expires.
        final SyncUser user2 = UserFactory.createUniqueUser();
        final PermissionManager pm = user2.getPermissionManager();
        looperThread.closeAfterTest(pm);

        pm.acceptOffer(offerToken, new PermissionManager.AcceptOfferCallback() {
            @Override
            public void onSuccess(String url, Permission permission) {
                fail();
            }

            @Override
            public void onError(ObjectServerError error) {
                assertEquals(ErrorCode.EXPIRED_PERMISSION_OFFER, error.getErrorCode());
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void acceptOffer_multipleUsers() {
        final String offerToken = createOffer(user, "test", AccessLevel.WRITE, null);

        final SyncUser user2 = UserFactory.createUniqueUser();
        final SyncUser user3 = UserFactory.createUniqueUser();
        final PermissionManager pm2 = user2.getPermissionManager();
        final PermissionManager pm3 = user3.getPermissionManager();
        looperThread.closeAfterTest(pm2);
        looperThread.closeAfterTest(pm3);

        final AtomicInteger offersAccepted = new AtomicInteger(0);
        PermissionManager.AcceptOfferCallback callback = new PermissionManager.AcceptOfferCallback() {
            @Override
            public void onSuccess(String url, Permission permission) {
                assertEquals("/" + user.getIdentity() + "/test", permission.getPath());
                assertTrue(permission.mayRead());
                assertTrue(permission.mayWrite());
                assertFalse(permission.mayManage());
                if (offersAccepted.incrementAndGet() == 2) {
                    looperThread.testComplete();
                }
            }

            @Override
            public void onError(ObjectServerError error) {
                fail(error.toString());
            }
        };

        pm2.acceptOffer(offerToken, callback);
        pm3.acceptOffer(offerToken, callback);
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void getCreatedOffers() {
        final String offerToken = createOffer(user, "test", AccessLevel.WRITE, null);
        PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);

        pm.getCreatedOffers(new PermissionManager.OffersCallback() {
            @Override
            public void onSuccess(RealmResults<PermissionOffer> offers) {
                RealmResults filteredOffers = offers.where()
                        .equalTo("token", offerToken)
                        .findAllAsync();
                looperThread.keepStrongReference(offers);
                filteredOffers.addChangeListener(new RealmChangeListener<RealmResults>() {
                    @Override
                    public void onChange(RealmResults results) {
                        switch (results.size()) {
                            case 0: return;
                            case 1:
                                looperThread.testComplete();
                                break;
                            default:
                                fail("To many offers: " + Arrays.toString(results.toArray()));
                        }
                    }
                });
            }

            @Override
            public void onError(ObjectServerError error) {
                fail(error.toString());
            }
        });
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void revokeOffer() {
        // createOffer validates that the offer is actually in the __management Realm.
        final String offerToken = createOffer(user, "test", AccessLevel.WRITE, null);
        final PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);

        pm.revokeOffer(offerToken, new PermissionManager.RevokeOfferCallback() {
            @Override
            public void onSuccess() {
                pm.getCreatedOffers(new PermissionManager.OffersCallback() {
                    @Override
                    public void onSuccess(RealmResults<PermissionOffer> offers) {
                        assertEquals(0, offers.size());
                        looperThread.testComplete();
                    }

                    @Override
                    public void onError(ObjectServerError error) {
                        fail(error.toString());
                    }
                });
            }

            @Override
            public void onError(ObjectServerError error) {
                fail(error.toString());
            }
        });
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void revokeOffer_afterOneAcceptEdit() {
        // createOffer validates that the offer is actually in the __management Realm.
        final String offerToken = createOffer(user, "test", AccessLevel.WRITE, null);

        SyncUser user2 = UserFactory.createUniqueUser();
        SyncUser user3 = UserFactory.createUniqueUser();
        final PermissionManager pm1 = user.getPermissionManager();
        PermissionManager pm2 = user2.getPermissionManager();
        final PermissionManager pm3 = user3.getPermissionManager();
        looperThread.closeAfterTest(pm1);
        looperThread.closeAfterTest(pm2);
        looperThread.closeAfterTest(pm3);

        pm2.acceptOffer(offerToken, new PermissionManager.AcceptOfferCallback() {
            @Override
            public void onSuccess(String realmUrl, Permission permission) {
                pm1.revokeOffer(offerToken, new PermissionManager.RevokeOfferCallback() {
                    @Override
                    public void onSuccess() {
                        pm3.acceptOffer(offerToken, new PermissionManager.AcceptOfferCallback() {
                            @Override
                            public void onSuccess(String realmUrl, Permission permission) {
                                fail("Offer should have been revoked");
                            }

                            @Override
                            public void onError(ObjectServerError error) {
                                assertEquals(ErrorCode.INVALID_PARAMETERS, error.getErrorCode());
                                looperThread.testComplete();
                            }
                        });
                    }

                    @Override
                    public void onError(ObjectServerError error) {
                        fail(error.toString());
                    }
                });
            }

            @Override
            public void onError(ObjectServerError error) {
                fail(error.toString());
            }
        });
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    @Ignore("Figure out why clocks on server/emulator on CI seem to differ")
    public void revokeOffer_alreadyExpired() {
        fail("Implement this");
    }

    /**
     * Creates a offer for a newly created Realm.
     *
     * @param user User that should create the offer
     * @param realmName Realm to create
     * @param level accessLevel to offer
     * @param expires when the offer expires
     */
    private String createOffer(final SyncUser user, final String realmName, final AccessLevel level, final Date expires) {
        final CountDownLatch offerReady = new CountDownLatch(1);
        final AtomicReference<String> offer = new AtomicReference<>(null);
        final HandlerThread ht = new HandlerThread("OfferThread");
        ht.start();
        Handler handler = new Handler(ht.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                String url = createRemoteRealm(user, realmName);
                final PermissionManager pm = user.getPermissionManager();
                pm.makeOffer(new PermissionOffer(url, level, expires), new PermissionManager.MakeOfferCallback() {
                    @Override
                    public void onSuccess(String offerToken) {
                        offer.set(offerToken);
                        pm.close();
                        offerReady.countDown();
                    }

                    @Override
                    public void onError(ObjectServerError error) {
                        pm.close();
                        fail(error.toString());
                    }
                });
            }
        });
        TestHelper.awaitOrFail(offerReady);
        ht.quit();
        return offer.get();
    }

    /**
     * Wait for a given permission to be present.
     *
     * @param permissions permission results.
     * @param user user that is being granted the permission.
     * @param urlSuffix the url suffix to listen for.
     * @param accessLevel the expected access level for 'user'.
     */
    private void assertPermissionPresent(RealmResults<Permission> permissions, final SyncUser user, String urlSuffix, final AccessLevel accessLevel) {
        RealmResults<Permission> filteredPermissions = permissions.where().endsWith("path", urlSuffix).findAllAsync();
        looperThread.keepStrongReference(permissions);
        filteredPermissions.addChangeListener(new RealmChangeListener<RealmResults<Permission>>() {
            @Override
            public void onChange(RealmResults<Permission> permissions) {
                switch(permissions.size()) {
                    case 0: return;
                    case 1:
                        Permission p = permissions.first();
                        assertEquals(accessLevel.mayRead(), p.mayRead());
                        assertEquals(accessLevel.mayWrite(), p.mayWrite());
                        assertEquals(accessLevel.mayManage(), p.mayManage());
                        assertEquals(user.getIdentity(), p.getUserId());
                        looperThread.testComplete();
                        break;
                    default:
                        fail("To many permissions matched: " + Arrays.toString(permissions.toArray()));
                }
            }
        });
    }

    private void setRealmError(PermissionManager pm, String fieldName, ObjectServerError error) throws NoSuchFieldException,
            IllegalAccessException {
        Field managementRealmErrorField = pm.getClass().getDeclaredField(fieldName);
        managementRealmErrorField.setAccessible(true);
        managementRealmErrorField.set(pm, error);
    }

    private void runTask(final PermissionManager pm, final PermissionManager.ApplyPermissionsCallback callback) {
        new PermissionManager.PermissionManagerTask<Void>(pm, callback) {
            @Override
            public void run() {
                if (!checkAndReportInvalidState()) {
                    fail();
                }
            }
        }.run();
    }

    /**
     * Creates an empty remote Realm on ROS owned by the provided user
     */
    private String createRemoteRealm(SyncUser user, String realmName) {
        String url = Constants.AUTH_SERVER_URL + "~/" + realmName;
        SyncConfiguration config = user.createConfiguration(url)
                .name(realmName)
                .schema(AllJavaTypes.class)
                .sessionStopPolicy(OsRealmConfig.SyncSessionStopPolicy.IMMEDIATELY)
                .build();

        Realm realm = Realm.getInstance(config);
        SyncSession session = SyncManager.getSession(config);
        final CountDownLatch uploadLatch = new CountDownLatch(1);
        session.addUploadProgressListener(ProgressMode.CURRENT_CHANGES, new ProgressListener() {
            @Override
            public void onChange(Progress progress) {
                if (progress.isTransferComplete()) {
                    uploadLatch.countDown();
                }
            }
        });
        TestHelper.awaitOrFail(uploadLatch);
        realm.close();
        return config.getServerUrl().toString();
    }

    /**
     * The initial set of permissions of ROS is timing dependant. This method will identify the possible known starting
     * states and fail if neither of these can be verified.
     */
    private void assertInitialPermissions(RealmResults<Permission> permissions) {
         assertEquals("Unexpected count() for __permission Realm: " + Arrays.toString(permissions.toArray()), 1, permissions.where().endsWith("path", "__permission").count());
         assertEquals("Unexpected count() for __management Realm: " + Arrays.toString(permissions.toArray()), 1, permissions.where().endsWith("path", "__management").count());
    }

    private void assertInitialDefaultPermissions(RealmResults<Permission> permissions) {
         assertEquals("Unexpected count() for __wildcardpermissions Realm: " + Arrays.toString(permissions.toArray()), 1, permissions.where().endsWith("path", "__wildcardpermissions").count());
    }

    private void assertGreaterThan(String error, int base, long count) {
        if (count <= base) {
            throw new AssertionError(error);
        }
    }

}
