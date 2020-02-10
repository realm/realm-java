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
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import io.realm.entities.AllJavaTypes;
import io.realm.internal.OsRealmConfig;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.UserFactory;
import io.realm.permissions.AccessLevel;
import io.realm.permissions.Permission;
import io.realm.permissions.PermissionOffer;
import io.realm.permissions.PermissionRequest;
import io.realm.permissions.UserCondition;
import io.realm.rule.RunTestInLooperThread;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class PathLevelPermissionsTests extends StandardIntegrationTest {

    private SyncUser user;

    @Before
    public void setUpTest() {
        user = UserFactory.createUniqueUser();
    }

    @Test
    @RunTestInLooperThread()
    public void retrieveGrantedPermissions_returnLoadedResults() {
        user.retrieveGrantedPermissionsAsync(new SyncUser.Callback<List<Permission>>() {
            @Override
            public void onSuccess(List<Permission> permissions) {
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
    public void retrieveGrantedPermissions_updatedWithNewRealms() {
        user.retrieveGrantedPermissionsAsync(new SyncUser.Callback<List<Permission>>() {
            @Override
            public void onSuccess(List<Permission> permissions) {
                assertInitialPermissions(permissions);

                // Create new Realm, which should create a new Permission entry
                SyncConfiguration config2 = user.createConfiguration(Constants.USER_REALM_2)
                        .schema(AllJavaTypes.class)
                        .fullSynchronization()
                        .errorHandler((session, error) -> fail(error.toString()))
                        .build();
                final Realm secondRealm = Realm.getInstance(config2);
                looperThread.closeAfterTest(secondRealm);
                try {
                    SyncManager.getSession(config2).uploadAllLocalChanges();
                } catch (InterruptedException e) {
                    fail(e.toString());
                }

                // Wait for the permission Result to report the new Realms
                List<Permission> permissions2 = user.retrieveGrantedPermissions();
                assertEquals(1, permissions.size());
                assertEquals(2, permissions2.size());
                Permission permission = permissions2.get(1);
                assertTrue(permission.getPath().endsWith("tests2"));
                assertTrue(permission.mayRead());
                assertTrue(permission.mayWrite());
                assertTrue(permission.mayManage());
                looperThread.testComplete();
            }

            @Override
            public void onError(ObjectServerError error) {
                fail(error.toString());
            }
        });
    }

    @Test
    @RunTestInLooperThread()
    public void getPermissions_updatedWithNewRealms_stressTest() {
        final int TEST_SIZE = 10;
        List<Permission> permissions = user.retrieveGrantedPermissions();
        assertInitialPermissions(permissions);

        for (int i = 0; i < TEST_SIZE; i++) {
            SyncConfiguration configNew = user.createConfiguration("realm://" + Constants.HOST + "/~/test" + i)
                    .fullSynchronization()
                    .schema(AllJavaTypes.class)
                    .build();
            Realm newRealm = Realm.getInstance(configNew);
            looperThread.closeAfterTest(newRealm);
        }

        List<Permission> perms = permissions;
        while(perms.size() < TEST_SIZE + 1) { // +1 is __wildcardpermissions
            perms = user.retrieveGrantedPermissions();
        }

        Permission p = perms.get(TEST_SIZE);
        assertTrue(p.getPath().endsWith("test" + (TEST_SIZE - 1)));
        assertTrue(p.mayRead());
        assertTrue(p.mayWrite());
        assertTrue(p.mayManage());
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread(emulateMainThread = true)
    public void applyPermissions_nonAdminUserFails() {
        SyncUser user2 = UserFactory.createUniqueUser();
        String otherUsersUrl = createRemoteRealm(user2, "test");

        // Create request for setting permissions on another users Realm,
        // i.e. user making the request do not have manage rights.
        UserCondition condition = UserCondition.userId(user.getIdentity());
        AccessLevel accessLevel = AccessLevel.WRITE;
        PermissionRequest request = new PermissionRequest(condition, otherUsersUrl, accessLevel);

        user.applyPermissionsAsync(request, new SyncUser.Callback<Void>() {
            @Override
            public void onSuccess(Void success) {
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

        // Create request for setting permissions on another users Realm,
        // i.e. user making the request do not have manage rights.
        UserCondition condition = UserCondition.userId(user.getIdentity());
        AccessLevel accessLevel = AccessLevel.WRITE;
        PermissionRequest request = new PermissionRequest(condition, wrongUrl, accessLevel);
        user.applyPermissionsAsync(request, new SyncUser.Callback<Void>() {
            @Override
            public void onSuccess(Void ignore) {
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
    public void applyPermissions_withUserId() {
        final SyncUser user2 = UserFactory.createUniqueUser();
        String url = createRemoteRealm(user2, "test");

        // Create request for giving `user` WRITE permissions to `user2`'s Realm.
        UserCondition condition = UserCondition.userId(user.getIdentity());
        AccessLevel accessLevel = AccessLevel.WRITE;
        PermissionRequest request = new PermissionRequest(condition, url, accessLevel);

        user2.applyPermissionsAsync(request, new SyncUser.Callback<Void>() {
            @Override
            public void onSuccess(Void ignore) {
                List<Permission> permissions = user.retrieveGrantedPermissions();
                assertPermissionPresent(permissions, user, "/test", AccessLevel.WRITE);
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

        // Create request for giving `user2` WRITE permissions to `user1`'s Realm.
        UserCondition condition = UserCondition.username(user2Username);
        AccessLevel accessLevel = AccessLevel.WRITE;
        String url = createRemoteRealm(user1, "test");
        PermissionRequest request = new PermissionRequest(condition, url, accessLevel);

        user1.applyPermissions(request);
        List<Permission> user2Permissions = user2.retrieveGrantedPermissions();
        assertPermissionPresent(user2Permissions, user2, user1.getIdentity() + "/test", AccessLevel.WRITE);
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void applyPermissions_usersWithNoExistingPermissions() {
        final SyncUser user1 = UserFactory.createUser("user1@realm.io");
        final SyncUser user2 = UserFactory.createUser("user2@realm.io");

        // Create request for giving all users with no existing permissions WRITE permissions to `user1`'s Realm.
        UserCondition condition = UserCondition.noExistingPermissions();
        AccessLevel accessLevel = AccessLevel.WRITE;
        final String url = createRemoteRealm(user1, "test");
        PermissionRequest request = new PermissionRequest(condition, url, accessLevel);

        user1.applyPermissions(request);
        List<Permission> user2Permissions = user2.retrieveGrantedPermissions();
        assertPermissionPresent(user2Permissions, null, "/" + user1.getIdentity() + "/test", AccessLevel.WRITE);

        // Remove wildcard permission to prevent them from interfering with other tests
        user1.applyPermissions(new PermissionRequest(UserCondition.noExistingPermissions(), url, AccessLevel.NONE));

    }

    @Test
    @RunTestInLooperThread
    public void makeOffer() {
        String url = createRemoteRealm(user, "test");

        PermissionOffer offer = new PermissionOffer(url, AccessLevel.WRITE);
        user.makePermissionsOfferAsync(offer, new SyncUser.Callback<String>() {
            @Override
            public void onSuccess(String token) {
                assertNotNull(token);
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
        PermissionOffer offer = new PermissionOffer(url, AccessLevel.WRITE);
        user.makePermissionsOfferAsync(offer, new SyncUser.Callback<String>() {
            @Override
            public void onSuccess(String s) {
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
        user2.acceptPermissionsOfferAsync(offerToken, new SyncUser.Callback<String>() {
            @Override
            public void onSuccess(String realmPath) {
                assertEquals("/" + user.getIdentity() + "/test", realmPath);
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
        user.acceptPermissionsOfferAsync("wrong-token", new SyncUser.Callback<String>() {
            @Override
            public void onSuccess(String s) {
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
    public void acceptOffer_multipleUsers() {
        final String offerToken = createOffer(user, "test", AccessLevel.WRITE, null);
        final SyncUser user2 = UserFactory.createUniqueUser();
        final SyncUser user3 = UserFactory.createUniqueUser();

        final AtomicInteger offersAccepted = new AtomicInteger(0);
        SyncUser.Callback<String> callback = new SyncUser.Callback<String>() {
            @Override
            public void onSuccess(String url) {
                assertEquals("/" + user.getIdentity() + "/test", url);
                if (offersAccepted.incrementAndGet() == 2) {
                    looperThread.testComplete();
                }
            }

            @Override
            public void onError(ObjectServerError error) {
                fail(error.toString());
            }
        };

        user2.acceptPermissionsOfferAsync(offerToken, callback);
        user2.acceptPermissionsOfferAsync(offerToken, callback);
    }

    @Test
    @RunTestInLooperThread
    public void getCreatedOffers() {
        final String offerToken = createOffer(user, "test", AccessLevel.WRITE, null);

        user.retrieveCreatedPermissionsOffersAsync(new SyncUser.Callback<List<PermissionOffer>>() {
            @Override
            public void onSuccess(List<PermissionOffer> permissionOffers) {
                assertEquals(1, permissionOffers.size());
                assertEquals(offerToken, permissionOffers.get(0).getToken());
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
    public void revokeOffer() {
        // createOffer validates that the offer is actually in the __management Realm.
        final String offerToken = createOffer(user, "test", AccessLevel.WRITE, null);

        user.invalidatePermissionsOfferAsync(offerToken, new SyncUser.Callback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                List<PermissionOffer> offers = user.retrieveCreatedPermissionsOffers();
                assertEquals(0, offers.size());
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
    public void revokeOffer_afterOneAcceptEdit() {
        final String offerToken = createOffer(user, "test", AccessLevel.WRITE, null);
        SyncUser user2 = UserFactory.createUniqueUser();
        SyncUser user3 = UserFactory.createUniqueUser();

        String path = user2.acceptPermissionsOffer(offerToken);
        assertTrue(path.endsWith("test"));
        user.invalidatePermissionsOffer(offerToken);
        try {
            user3.acceptPermissionsOffer(offerToken);
            fail();
        } catch (ObjectServerError error) {
            assertEquals(ErrorCode.EXPIRED_PERMISSION_OFFER, error.getErrorCode());
            looperThread.testComplete();
        }
    }

    /**
     * Creates an offer for a newly created Realm.
     *
     * @param user User that should create the offer
     * @param realmName Realm to create
     * @param level accessLevel to offer
     * @param expires when the offer expires
     */
    private String createOffer(final SyncUser user, final String realmName, final AccessLevel level, final Date expires) {
        String url = createRemoteRealm(user, realmName);
        return user.makePermissionsOffer(new PermissionOffer(url, level, expires));
    }

    /**
     * Wait for a given permission to be present.
     *
     * @param permissions permission results.
     * @param user user that is being granted the permission.
     * @param urlSuffix the url suffix to listen for.
     * @param accessLevel the expected access level for 'user'.
     */
    private void assertPermissionPresent(List<Permission> permissions, @Nullable final SyncUser user, String urlSuffix, final AccessLevel accessLevel) {
        for (Permission p : permissions) {
            if (p.getPath().endsWith(urlSuffix)) {
                assertEquals(accessLevel.mayRead(), p.mayRead());
                assertEquals(accessLevel.mayWrite(), p.mayWrite());
                assertEquals(accessLevel.mayManage(), p.mayManage());
                if (user != null) {
                    // Specific permissions
                    assertEquals(user.getIdentity(), p.getUserId());
                } else {
                    // Default permissions
                    assertNull(p.getUserId());
                }
                looperThread.testComplete();
                return;
            }
        }
        throw new AssertionError("No matching permissions");
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
     * The initial set of permissions from ROS.
     */
    private void assertInitialPermissions(List<Permission> permissions) {
        assertEquals(1, permissions.size());
        assertEquals("/__wildcardpermissions", permissions.get(0).getPath());
    }
}
