package io.realm.objectserver;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.AuthenticationListener;
import io.realm.BaseIntegrationTest;
import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncManager;
import io.realm.SyncSession;
import io.realm.SyncUser;
import io.realm.SyncUserInfo;
import io.realm.entities.StringOnly;
import io.realm.internal.async.RealmAsyncTaskImpl;
import io.realm.internal.objectserver.Token;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.StringOnlyModule;
import io.realm.objectserver.utils.UserFactory;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestSyncConfigurationFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(AndroidJUnit4.class)
public class AuthTests extends BaseIntegrationTest {
    @Rule
    public TestSyncConfigurationFactory configFactory = new TestSyncConfigurationFactory();

    @Test
    public void login_userNotExist() {
        SyncCredentials credentials = SyncCredentials.usernamePassword("IWantToHackYou", "GeneralPassword", false);
        try {
            SyncUser.login(credentials, Constants.AUTH_URL);
            fail();
        } catch (ObjectServerError expected) {
            assertEquals(ErrorCode.INVALID_CREDENTIALS, expected.getErrorCode());
        }
    }

    @Test
    @RunTestInLooperThread
    public void loginAsync_userNotExist() {
        SyncCredentials credentials = SyncCredentials.usernamePassword("IWantToHackYou", "GeneralPassword", false);
        SyncUser.loginAsync(credentials, Constants.AUTH_URL, new SyncUser.Callback() {
            @Override
            public void onSuccess(SyncUser user) {
                fail();
            }

            @Override
            public void onError(ObjectServerError error) {
                assertEquals(ErrorCode.INVALID_CREDENTIALS, error.getErrorCode());
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void login_newUser() {
        SyncCredentials credentials = SyncCredentials.usernamePassword("myUser", "password", true);
        SyncUser.loginAsync(credentials, Constants.AUTH_URL, new SyncUser.Callback() {
            @Override
            public void onSuccess(SyncUser user) {
                assertFalse(user.isAdmin());
                try {
                    assertEquals(new URL(Constants.AUTH_URL), user.getAuthenticationUrl());
                } catch (MalformedURLException e) {
                    fail(e.toString());
                }
                looperThread.testComplete();
            }

            @Override
            public void onError(ObjectServerError error) {
                fail(error.toString());
            }
        });
    }

    // FIXME: https://github.com/realm/realm-java/issues/4711
    // fail may be related to this issue https://github.com/realm/realm-java/issues/5068
    @Test
    @RunTestInLooperThread
    @Ignore("This fails expectSimpleCommit for some reasons, needs to be FIXED ASAP.")
    public void login_withAccessToken() {
        SyncUser adminUser = UserFactory.createAdminUser(Constants.AUTH_URL);
        SyncCredentials credentials = SyncCredentials.accessToken(adminUser.getAccessToken().value(), "custom-admin-user", adminUser.isAdmin());
        SyncUser.loginAsync(credentials, Constants.AUTH_URL, new SyncUser.Callback() {
            @Override
            public void onSuccess(SyncUser user) {
                assertTrue(user.isAdmin());
                final SyncConfiguration config = new SyncConfiguration.Builder(user, Constants.SYNC_SERVER_URL)
                        .errorHandler(new SyncSession.ErrorHandler() {
                            @Override
                            public void onError(SyncSession session, ObjectServerError error) {
                                fail("Session failed: " + error);
                            }
                        })
                        .build();

                final Realm realm = Realm.getInstance(config);
                looperThread.addTestRealm(realm);

                // FIXME: Right now we have no Java API for detecting when a session is established
                // So we optimistically assume it has been connected after 1 second.
                looperThread.postRunnableDelayed(new Runnable() {
                    @Override
                    public void run() {
                        assertTrue(SyncManager.getSession(config).getUser().isValid());
                        looperThread.testComplete();
                    }
                }, 1000);
            }

            @Override
            public void onError(ObjectServerError error) {
                fail("Login failed: " + error);
            }
        });
    }

    @Test
    public void loginAsync_errorHandlerThrows() throws InterruptedException {
        final AtomicBoolean errorThrown = new AtomicBoolean(false);

        // Create custom Looper thread to be able to check for errors thrown when processing Looper events.
        Thread t = new Thread(new Runnable() {
            private volatile Handler handler;
            @Override
            public void run() {
                Looper.prepare();
                try {
                    handler = new Handler();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            SyncCredentials credentials = SyncCredentials.usernamePassword("IWantToHackYou", "GeneralPassword", false);
                            SyncUser.loginAsync(credentials, Constants.AUTH_URL, new SyncUser.Callback() {
                                @Override
                                public void onSuccess(SyncUser user) {
                                    fail();
                                }

                                @Override
                                public void onError(ObjectServerError error) {
                                    assertEquals(ErrorCode.INVALID_CREDENTIALS, error.getErrorCode());
                                    throw new IllegalArgumentException("BOOM");
                                }
                            });
                        }
                    });
                    Looper.loop(); //
                } catch (IllegalArgumentException e) {
                    errorThrown.set(true);
                }
            }
        });
        t.start();
        t.join(TimeUnit.SECONDS.toMillis(10));
        assertTrue(errorThrown.get());
    }

    @Test
    public void changePassword() {
        String username = UUID.randomUUID().toString();
        String originalPassword = "password";
        SyncCredentials credentials = SyncCredentials.usernamePassword(username, originalPassword, true);
        SyncUser userOld = SyncUser.login(credentials, Constants.AUTH_URL);
        assertTrue(userOld.isValid());

        // Change password and try to log in with new password
        String newPassword = "new-password";
        userOld.changePassword(newPassword);
        userOld.logout();
        credentials = SyncCredentials.usernamePassword(username, newPassword, false);
        SyncUser userNew = SyncUser.login(credentials, Constants.AUTH_URL);

        assertTrue(userNew.isValid());
        assertEquals(userOld.getIdentity(), userNew.getIdentity());
    }

    @Test
    public void changePassword_using_admin() {
        String username = UUID.randomUUID().toString();
        String originalPassword = "password";
        SyncCredentials credentials = SyncCredentials.usernamePassword(username, originalPassword, true);
        SyncUser userOld = SyncUser.login(credentials, Constants.AUTH_URL);
        assertTrue(userOld.isValid());

        // Login an admin user
        SyncUser adminUser = UserFactory.createAdminUser(Constants.AUTH_URL);
        assertTrue(adminUser.isValid());
        assertTrue(adminUser.isAdmin());

        // Change password using admin user
        String newPassword = "new-password";
        adminUser.changePassword(userOld.getIdentity(), newPassword);

        // Try to log in with new password
        userOld.logout();
        credentials = SyncCredentials.usernamePassword(username, newPassword, false);
        SyncUser userNew = SyncUser.login(credentials, Constants.AUTH_URL);

        assertTrue(userNew.isValid());
        assertEquals(userOld.getIdentity(), userNew.getIdentity());
    }

    @Test
    @RunTestInLooperThread
    public void changePassword_using_admin_async() {
        final String username = UUID.randomUUID().toString();
        final String originalPassword = "password";
        final SyncCredentials credentials = SyncCredentials.usernamePassword(username, originalPassword, true);
        final SyncUser userOld = SyncUser.login(credentials, Constants.AUTH_URL);
        assertTrue(userOld.isValid());

        // Login an admin user
        final SyncUser adminUser = UserFactory.createAdminUser(Constants.AUTH_URL);
        assertTrue(adminUser.isValid());
        assertTrue(adminUser.isAdmin());

        // Change password using admin user
        final String newPassword = "new-password";
        adminUser.changePasswordAsync(userOld.getIdentity(), newPassword, new SyncUser.Callback() {
            @Override
            public void onSuccess(SyncUser administratorUser) {
                assertEquals(adminUser, administratorUser);

                // Try to log in with new password
                userOld.logout();
                SyncCredentials credentials = SyncCredentials.usernamePassword(username, newPassword, false);
                SyncUser userNew = SyncUser.login(credentials, Constants.AUTH_URL);

                assertTrue(userNew.isValid());
                assertEquals(userOld.getIdentity(), userNew.getIdentity());

                looperThread.testComplete();
            }

            @Override
            public void onError(ObjectServerError error) {
                fail(error.getErrorMessage());
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void changePassword_throwWhenUserIsLoggedOut() {
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncCredentials credentials = SyncCredentials.usernamePassword(username, password, true);
        SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        SyncManager.addAuthenticationListener(new AuthenticationListener() {
            @Override
            public void loggedIn(SyncUser user) {
                SyncManager.removeAuthenticationListener(this);
                // callback is happening on different thread, all assertions needs to be done on looper thread
                looperThread.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        fail("loggedIn should not be invoked");
                    }
                });
            }

            @Override
            public void loggedOut(SyncUser user) {
                SyncManager.removeAuthenticationListener(this);
                try {
                    user.changePassword("new-password");
                    looperThread.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            fail("changePassword should throw ObjectServerError (INVALID CREDENTIALS)");
                        }
                    });
                } catch (ObjectServerError expected) {
                }
                looperThread.testComplete();
            }
        });
        user.logout();
    }

    @Test
    public void cachedInstanceShouldNotThrowIfRefreshTokenExpires() throws InterruptedException {
        String username = UUID.randomUUID().toString();
        String password = "password";

        SyncCredentials credentials = SyncCredentials.usernamePassword(username, password, true);
        final SyncUser user = spy(SyncUser.login(credentials, Constants.AUTH_URL));

        when(user.isValid()).thenReturn(true, false);

        final RealmConfiguration configuration = new SyncConfiguration.Builder(user, Constants.USER_REALM).build();
        Realm realm = Realm.getInstance(configuration);

        assertFalse(user.isValid());
        verify(user, times(2)).isValid();

        final CountDownLatch backgroundThread = new CountDownLatch(1);
        // Should not throw when using an expired refresh_token form a different thread
        // It should be able to open a Realm with an expired token
        new Thread() {
            @Override
            public void run() {
                Realm instance = Realm.getInstance(configuration);
                instance.close();
                backgroundThread.countDown();
            }
        }.start();

        backgroundThread.await();

        // It should be possible to open a cached Realm with expired token
        Realm cachedInstance = Realm.getInstance(configuration);
        assertNotNull(cachedInstance);

        realm.close();
        cachedInstance.close();
        user.logout();
    }

    @Test
    public void buildingSyncConfigurationShouldThrowIfInvalidUser() {
        String username = UUID.randomUUID().toString();
        String password = "password";

        SyncCredentials credentials = SyncCredentials.usernamePassword(username, password, true);
        SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        SyncUser currentUser = SyncUser.currentUser();
        user.logout();

        assertFalse(user.isValid());

        try {
            // We should not be able to build a configuration with an invalid/logged out user
            new SyncConfiguration.Builder(user, Constants.USER_REALM).build();
            fail("Invalid user, it should not be possible to create a SyncConfiguration");
        } catch (IllegalArgumentException expected) {
            // User not authenticated or authentication expired.
        }

        try {
            // We should not be able to build a configuration with an invalid/logged out user
            new SyncConfiguration.Builder(currentUser, Constants.USER_REALM).build();
            fail("Invalid currentUser, it should not be possible to create a SyncConfiguration");
        } catch (IllegalArgumentException expected) {
            // User not authenticated or authentication expired.
        }
    }

    // using a logout user should not throw
    @Test
    public void usingConfigurationWithInvalidUserShouldThrow() {
        String username = UUID.randomUUID().toString();
        String password = "password";

        SyncCredentials credentials = SyncCredentials.usernamePassword(username, password, true);
        SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        RealmConfiguration configuration = new SyncConfiguration.Builder(user, Constants.USER_REALM).build();
        user.logout();
        assertFalse(user.isValid());

        Realm instance = Realm.getInstance(configuration);
        instance.close();
    }

    @Test
    public void logout_currentUserMoreThanOne() {
        UserFactory.createUniqueUser(Constants.AUTH_URL);
        SyncUser.currentUser().logout();
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        assertEquals(user, SyncUser.currentUser());
    }

    // logging out 'user' should have the same impact on other instance(s) of the same user
    @Test
    public void loggingOutUserShouldImpactOtherInstances() throws InterruptedException {
        String username = UUID.randomUUID().toString();
        String password = "password";

        SyncCredentials credentials = SyncCredentials.usernamePassword(username, password, true);
        SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        SyncUser currentUser = SyncUser.currentUser();

        assertTrue(user.isValid());
        assertEquals(user, currentUser);

        user.logout();

        assertFalse(user.isValid());
        assertFalse(currentUser.isValid());
    }

    // logging out 'currentUser' should have the same impact on other instance(s) of the user
    @Test
    public void loggingOutCurrentUserShouldImpactOtherInstances() throws InterruptedException {
        String username = UUID.randomUUID().toString();
        String password = "password";

        SyncCredentials credentials = SyncCredentials.usernamePassword(username, password, true);
        SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        SyncUser currentUser = SyncUser.currentUser();

        assertTrue(user.isValid());
        assertEquals(user, currentUser);

        SyncUser.currentUser().logout();

        assertFalse(user.isValid());
        assertFalse(currentUser.isValid());
        assertNull(SyncUser.currentUser());
    }

    // verify that multiple users can be logged in at the same time
    @Test
    public void multipleUsersCanBeLoggedInSimultaneously() {
        final String password = "password";
        final SyncUser[] users = new SyncUser[3];

        for (int i = 0; i < users.length; i++) {
            SyncCredentials credentials = SyncCredentials.usernamePassword(UUID.randomUUID().toString(), password,
                    true);
            users[i] = SyncUser.login(credentials, Constants.AUTH_URL);
        }

        for (int i = 0; i < users.length; i++) {
            assertTrue(users[i].isValid());
        }

        for (int i = 0; i < users.length; i++) {
            users[i].logout();
        }

        for (int i = 0; i < users.length; i++) {
            assertFalse(users[i].isValid());
        }
    }

    // verify that a single user can be logged out and back in.
    @Test
    public void singleUserCanBeLoggedInAndOutRepeatedly() {
        final String username = UUID.randomUUID().toString();
        final String password = "password";

        // register the user the first time
        SyncCredentials credentials = SyncCredentials.usernamePassword(username, password, true);

        SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        assertTrue(user.isValid());
        user.logout();
        assertFalse(user.isValid());

        // on subsequent logins, the user is already registered.
        credentials = credentials = SyncCredentials.usernamePassword(username, password, false);
        for (int i = 0; i < 3; i++) {
            user = SyncUser.login(credentials, Constants.AUTH_URL);
            assertTrue(user.isValid());
            user.logout();
            assertFalse(user.isValid());
        }
    }

    @Test
    public void revokedRefreshTokenIsNotSameAfterLogin() throws InterruptedException {
        final String uniqueName = UUID.randomUUID().toString();

        SyncCredentials credentials = SyncCredentials.usernamePassword(uniqueName, "password", true);
        SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        Token revokedRefreshToken = user.getAccessToken();

        user.logout();

        credentials = SyncCredentials.usernamePassword(uniqueName, "password", false);
        SyncUser loggedInUser = SyncUser.login(credentials, Constants.AUTH_URL);

        // still comparing the same user
        Assert.assertEquals(revokedRefreshToken.identity(), loggedInUser.getAccessToken().identity());
        // different tokens
        assertNotEquals(revokedRefreshToken.value(), loggedInUser.getAccessToken().value());
    }

    // The pre-emptive token refresh subsystem should function, and properly refresh the access token.
    // WARNING: this test can fail if there's a difference between the server's and device's clock, causing the
    // refresh access token to be too far in time.
    @Test(timeout = 30000)
    public void preemptiveTokenRefresh() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);

        // make the access tokens map accessible
        Field realmsField = SyncUser.class.getDeclaredField("realms");
        realmsField.setAccessible(true);
        @SuppressWarnings("unchecked") // using reflection
        Map<SyncConfiguration, Token> accessTokens = (Map<SyncConfiguration, Token>) realmsField.get(user);

        final SyncConfiguration syncConfiguration = configFactory
                .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .modules(new StringOnlyModule())
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        Assert.fail(error.getErrorMessage());
                    }
                })
                .build();
        Realm realm = Realm.getInstance(syncConfiguration);

        // create and wait for a transaction to be uploaded,
        // this guarantees that an accessToken is available
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObject(StringOnly.class).setChars("1");
            }
        });
        SyncSession session = SyncManager.getSession(syncConfiguration);
        session.uploadAllLocalChanges();

        assertFalse(accessTokens.isEmpty());
        Assert.assertEquals(1, accessTokens.size());
        Map.Entry<SyncConfiguration, Token> entry = accessTokens.entrySet().iterator().next();
        Assert.assertEquals(syncConfiguration, entry.getKey());

        final Token accessToken = entry.getValue();
        Assert.assertNotNull(accessToken);

        // getting refresh token delay
        Field refreshTokenTaskField = SyncSession.class.getDeclaredField("refreshTokenTask");
        refreshTokenTaskField.setAccessible(true);
        RealmAsyncTaskImpl task = (RealmAsyncTaskImpl) refreshTokenTaskField.get(session);
        Field pendingTaskField = RealmAsyncTaskImpl.class.getDeclaredField("pendingTask");
        pendingTaskField.setAccessible(true);
        ScheduledFuture<?> pendingTask = (ScheduledFuture<?>) pendingTaskField.get(task);
        long nextRefreshTokenRefreshQueryDelay = pendingTask.getDelay(TimeUnit.MILLISECONDS);

        // current configuration 'realm-java/tools/sync_test_server/configuration.yml'
        // is setting the access token to expire every 20 seconds 'access_token: 20'
        // we wait approximately actually 10 seconds since the SyncSession.REFRESH_MARGIN_DELAY is 10s
        SystemClock.sleep(nextRefreshTokenRefreshQueryDelay);

        // allow 3 seconds for the query to perform and complete
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(3));

        Token newAccessToken = accessTokens.get(syncConfiguration);

        assertThat("new Token is not expired", newAccessToken.expiresMs(), greaterThan(System.currentTimeMillis()));
        assertNotEquals(accessToken, newAccessToken);

        // refresh_token identity is the same
        Assert.assertEquals(user.getAccessToken().identity(), newAccessToken.identity());
        Assert.assertEquals(accessToken.identity(), newAccessToken.identity());

        realm.close();
    }

    @Test
    public void retrieve() {
        final SyncUser adminUser = UserFactory.createAdminUser(Constants.AUTH_URL);

        final String username = UUID.randomUUID().toString();
        final String password = "password";
        final SyncCredentials credentials = SyncCredentials.usernamePassword(username, password, true);
        final SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        assertTrue(user.isValid());

        String identity = user.getIdentity();

        SyncUserInfo userInfo = adminUser.retrieveInfoForUser(username, SyncCredentials.IdentityProvider.USERNAME_PASSWORD);

        assertNotNull(userInfo);
        assertEquals(SyncCredentials.IdentityProvider.USERNAME_PASSWORD, userInfo.getProvider());
        assertEquals(username, userInfo.getProviderUserIdentity());
        assertEquals(identity, userInfo.getIdentity());
        assertFalse(userInfo.isAdmin());
    }


    // retrieving a logged out user
    @Test
    @RunTestInLooperThread
    public void retrieve_logout() {
        final SyncUser adminUser = UserFactory.createAdminUser(Constants.AUTH_URL);

        final String username = UUID.randomUUID().toString();
        final String password = "password";
        final SyncCredentials credentials = SyncCredentials.usernamePassword(username, password, true);
        final SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        final String identity = user.getIdentity();

        // unless the refresh_token is revoked (via logout) the admin user can still retrieve the user
        // we make sure the token is revoked before trying to retrieve the user
        SyncManager.addAuthenticationListener(new AuthenticationListener() {
            @Override
            public void loggedIn(SyncUser user) {
                SyncManager.removeAuthenticationListener(this);
                looperThread.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        fail("loggedIn should not be invoked");
                    }
                });
            }

            @Override
            public void loggedOut(final SyncUser user) {
                SyncManager.removeAuthenticationListener(this);
                looperThread.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        assertFalse(user.isValid());
                        SyncUserInfo userInfo = adminUser.retrieveInfoForUser(username, SyncCredentials.IdentityProvider.USERNAME_PASSWORD);

                        assertNotNull(userInfo);
                        assertEquals(SyncCredentials.IdentityProvider.USERNAME_PASSWORD, userInfo.getProvider());
                        assertEquals(username, userInfo.getProviderUserIdentity());
                        assertEquals(identity, userInfo.getIdentity());
                        assertFalse(userInfo.isAdmin());

                        looperThread.testComplete();
                    }
                });

            }
        });
        user.logout();
    }

    @Test
    public void retrieve_AdminUser() {
        final SyncUser adminUser = UserFactory.createAdminUser(Constants.AUTH_URL);
        SyncUserInfo userInfo = adminUser.retrieveInfoForUser("admin", SyncCredentials.IdentityProvider.DEBUG);// TODO use enum for auth provider
        assertNotNull(userInfo);
        assertEquals(adminUser.getIdentity(), userInfo.getIdentity());
        assertTrue(userInfo.isAdmin());
    }

    @Test
    public void retrieve_unknownProviderId() {
        final SyncUser adminUser = UserFactory.createAdminUser(Constants.AUTH_URL);
        SyncUserInfo userInfo = adminUser.retrieveInfoForUser("doesNotExist", SyncCredentials.IdentityProvider.USERNAME_PASSWORD);
        assertNull(userInfo);
    }

    @Test
    public void retrieve_invalidProvider() {
        final SyncUser adminUser = UserFactory.createAdminUser(Constants.AUTH_URL);
        final String username = UUID.randomUUID().toString();
        final String password = "password";
        final SyncCredentials credentials = SyncCredentials.usernamePassword(username, password, true);
        final SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        assertTrue(user.isValid());

        SyncUserInfo userInfo = adminUser.retrieveInfoForUser("username", "invalid");
        assertNull(userInfo);
    }

    @Test
    public void retrieve_notAdmin() {
        final String username1 = UUID.randomUUID().toString();
        final String password1 = "password";
        final SyncCredentials credentials1 = SyncCredentials.usernamePassword(username1, password1, true);
        final SyncUser user1 = SyncUser.login(credentials1, Constants.AUTH_URL);
        assertTrue(user1.isValid());

        final String username2 = UUID.randomUUID().toString();
        final String password2 = "password";
        final SyncCredentials credentials2 = SyncCredentials.usernamePassword(username2, password2, true);
        final SyncUser user2 = SyncUser.login(credentials2, Constants.AUTH_URL);
        assertTrue(user2.isValid());

        // trying to lookup user2 using user1 should not work (requires admin token)
        try {
            user1.retrieveInfoForUser(SyncCredentials.IdentityProvider.USERNAME_PASSWORD, username2);
            fail("It should not be possible to lookup a user using non admin token");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    @RunTestInLooperThread
    public void retrieve_async() {
        final String username = UUID.randomUUID().toString();
        final String password = "password";
        final SyncCredentials credentials = SyncCredentials.usernamePassword(username, password, true);
        final SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        assertTrue(user.isValid());

        // Login an admin user
        final SyncUser adminUser = UserFactory.createAdminUser(Constants.AUTH_URL);
        assertTrue(adminUser.isValid());
        assertTrue(adminUser.isAdmin());

        final String identity = user.getIdentity();
        adminUser.retrieveInfoForUserAsync(username, SyncCredentials.IdentityProvider.USERNAME_PASSWORD, new SyncUser.RequestCallback<SyncUserInfo>() {
            @Override
            public void onSuccess(SyncUserInfo userInfo) {
                assertNotNull(userInfo);
                assertEquals(SyncCredentials.IdentityProvider.USERNAME_PASSWORD, userInfo.getProvider());
                assertEquals(username, userInfo.getProviderUserIdentity());
                assertEquals(identity, userInfo.getIdentity());
                assertFalse(userInfo.isAdmin());

                looperThread.testComplete();
            }

            @Override
            public void onError(ObjectServerError error) {
                fail(error.getErrorMessage());
            }
        });
    }
}
