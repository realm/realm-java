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

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.internal.Util;
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
public class PermissionManagerTests extends IsolatedIntegrationTests {

    private SyncUser user;

    @Before
    public void setUpTest() {
        user = UserFactory.createUniqueUser();
    }

    @Test
    @RunTestInLooperThread
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
    @RunTestInLooperThread
    public void getPermissions_noLongerValidWhenPermissionManagerIsClosed() {
        final PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);
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
                fail(error.toString());
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void getPermissions_updatedWithNewRealms() {
        PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);
        pm.getPermissions(new PermissionManager.PermissionsCallback() {
            @Override
            public void onSuccess(RealmResults<Permission> permissions) {
                assertTrue(permissions.isLoaded());
                assertInitialPermissions(permissions);

                // Create new Realm, which should create a new Permission entry
                SyncConfiguration config2 = new SyncConfiguration.Builder(user, Constants.USER_REALM_2)
                        .errorHandler(new SyncSession.ErrorHandler() {
                            @Override
                            public void onError(SyncSession session, ObjectServerError error) {
                                fail(error.toString());
                            }
                        })
                        .build();
                final Realm secondRealm = Realm.getInstance(config2);
                looperThread.closeAfterTest(secondRealm);

                // Wait for the permission Result to report the new Realm
                looperThread.keepStrongReference(permissions);
                permissions.addChangeListener(new RealmChangeListener<RealmResults<Permission>>() {
                    @Override
                    public void onChange(RealmResults<Permission> permissions) {
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
    @RunTestInLooperThread
    public void getPermissions_closed() throws IOException {
        PermissionManager pm = user.getPermissionManager();
        pm.close();

        pm.getPermissions(new PermissionManager.PermissionsCallback() {
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
    public void getDefaultPermissions_returnLoadedResults() {
        PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);
        pm.getDefaultPermissions(new PermissionManager.PermissionsCallback() {
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
    public void getDefaultPermissions_noLongerValidWhenPermissionManagerIsClosed() {
        final PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);
        pm.getDefaultPermissions(new PermissionManager.PermissionsCallback() {
            @Override
            public void onSuccess(RealmResults<Permission> permissions) {
                assertTrue(permissions.isValid());
                pm.close();
                assertFalse(permissions.isValid());
                looperThread.testComplete();
            }

            @Override
            public void onError(ObjectServerError error) {
                fail(error.toString());
            }
        });
    }

    @Test
    @Ignore("FIXME Add once `setPermissions` are implemented")
    @RunTestInLooperThread
    public void getDefaultPermissions_updatedWithNewRealms() {

    }

    @Test
    @RunTestInLooperThread
    public void getDefaultPermissions_closed() throws IOException {
        PermissionManager pm = user.getPermissionManager();
        looperThread.closeAfterTest(pm);
        pm.close();

        pm.getDefaultPermissions(new PermissionManager.PermissionsCallback() {
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
    @RunTestInLooperThread
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
    @RunTestInLooperThread
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
    @RunTestInLooperThread
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
    @RunTestInLooperThread
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
    @RunTestInLooperThread
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
                assertEquals(ErrorCode.ACCESS_DENIED, error.getErrorCode());
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread
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
    @RunTestInLooperThread
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
    @RunTestInLooperThread
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
                SyncConfiguration config = new SyncConfiguration.Builder(user2, url)
                        .errorHandler(new SyncSession.ErrorHandler() {
                            @Override
                            public void onError(SyncSession session, ObjectServerError error) {
                                fail(error.toString());
                            }
                        })
                        .build();
                Realm realm = Realm.getInstance(config);
                try {
                    SyncManager.getSession(config).downloadAllServerChanges();
                } catch (InterruptedException e) {
                    fail(Util.getStackTrace(e));
                }
                realm.close();
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
    @RunTestInLooperThread
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
    @RunTestInLooperThread
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
    @RunTestInLooperThread
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
    @RunTestInLooperThread
    @Ignore("FIXME: Figure out how the time differs between emulator and server")
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
    @RunTestInLooperThread
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
    @RunTestInLooperThread
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
                            case 2:
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
        SyncConfiguration config = new SyncConfiguration.Builder(user, url).build();

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
        // For a new user, the PermissionManager should contain 1 entry for the __permission Realm, but we are
        // creating the __management Realm at the same time, so this might be here as well.
        permissions = permissions.sort("path");
        if (permissions.size() == 1) {
            // FIXME It is very unpredictable which Permission is returned. This needs to be fixed.
            Permission permission = permissions.first();
            assertTrue(permission.getPath().endsWith("__permission") || permission.getPath().endsWith("__management"));
        } else if (permissions.size() == 2) {
            assertTrue("Failed: " + permissions.get(0).toString(), permissions.get(0).getPath().endsWith("__management"));
            assertTrue("Failed: " + permissions.get(1).toString(), permissions.get(1).getPath().endsWith("__permission"));
        } else {
            fail("Permission Realm contains unknown permissions: " + Arrays.toString(permissions.toArray()));
        }
    }

}
