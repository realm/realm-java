package io.realm.objectserver;

import android.os.Handler;
import android.os.Looper;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.UserFactory;
import io.realm.rule.RunTestInLooperThread;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;


@RunWith(AndroidJUnit4.class)
public class AuthTests extends BaseIntegrationTest {

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
    public void changePassword_throwWhenUserIsLoggedOut() {
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncCredentials credentials = SyncCredentials.usernamePassword(username, password, true);
        SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        user.logout();

        thrown.expect(ObjectServerError.class);
        user.changePassword("new-password");
    }

    // Cached instances of RealmConfiguration should not be allowed to be used if the user is no longer valid
    @Test
    public void cachedInstanceShouldThrowIfUserBecomeInvalid() throws InterruptedException {
        String username = UUID.randomUUID().toString();
        String password = "password";

        SyncCredentials credentials = SyncCredentials.usernamePassword(username, password, true);
        SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        final RealmConfiguration configuration = new SyncConfiguration.Builder(user, Constants.USER_REALM).build();
        Realm realm = Realm.getInstance(configuration);

        user.logout();
        assertFalse(user.isValid());

        final CountDownLatch backgroundThread = new CountDownLatch(1);
        // Should throw when using the invalid configuration form a different thread
        new Thread() {
            @Override
            public void run() {
                try {
                    Realm.getInstance(configuration);
                    fail("Invalid SyncConfiguration should throw");
                } catch (IllegalStateException expected) {
                } finally {
                    backgroundThread.countDown();
                }
            }
        }.start();

        backgroundThread.await();

        // it is ok to return the cached instance, since this use case is legit
        // user refresh token can timeout, or the token can be revoked from ROS
        // while running the Realm instance. So it doesn't make sense to break this behaviour
        Realm cachedInstance = Realm.getInstance(configuration);
        assertNotNull(cachedInstance);

        realm.close();
        cachedInstance.close();
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

    // using a logout user should throw
    @Test
    public void usingConfigurationWithInvalidUserShouldThrow() {
        String username = UUID.randomUUID().toString();
        String password = "password";

        SyncCredentials credentials = SyncCredentials.usernamePassword(username, password, true);
        SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        RealmConfiguration configuration = new SyncConfiguration.Builder(user, Constants.USER_REALM).build();
        user.logout();
        assertFalse(user.isValid());

        try {
            Realm.getInstance(configuration);
            fail("SyncUser is not longer valid, it should not be possible to get a Realm instance");
        } catch (IllegalStateException expected) {
        }
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

    @Test
    public void retrieve() {
        final SyncUser adminUser = UserFactory.createAdminUser(Constants.AUTH_URL);

        final String username = UUID.randomUUID().toString();
        final String password = "password";
        final SyncCredentials credentials = SyncCredentials.usernamePassword(username, password, true);
        final SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        assertTrue(user.isValid());

        String identity = user.getIdentity();
        SyncUser syncUser = adminUser.retrieveUser(SyncCredentials.IdentityProvider.USERNAME_PASSWORD, username);
        assertNotNull(syncUser);
        assertEquals(identity, syncUser.getIdentity());
        assertFalse(syncUser.isAdmin());
        assertTrue(syncUser.isValid());
    }

    @Test
    public void retrieve_logout() {
        final SyncUser adminUser = UserFactory.createAdminUser(Constants.AUTH_URL);

        final String username = UUID.randomUUID().toString();
        final String password = "password";
        final SyncCredentials credentials = SyncCredentials.usernamePassword(username, password, true);
        final SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        final String identity = user.getIdentity();
        user.logout();
        assertFalse(user.isValid());

        SyncUser syncUser = adminUser.retrieveUser(SyncCredentials.IdentityProvider.USERNAME_PASSWORD, username);
        assertNotNull(syncUser);
        assertEquals(identity, syncUser.getIdentity());
        assertFalse(syncUser.isAdmin());
        assertFalse(syncUser.isValid());
    }

    @Test
    public void retrieve_AdminUser() {
        final SyncUser adminUser = UserFactory.createAdminUser(Constants.AUTH_URL);
        SyncUser syncUser = adminUser.retrieveUser(SyncCredentials.IdentityProvider.DEBUG, "admin");// TODO use enum for auth provider
        assertNotNull(syncUser);
        assertEquals(adminUser.getIdentity(), syncUser.getIdentity());
        assertTrue(syncUser.isAdmin());
        assertTrue(syncUser.isValid());
    }

    @Test
    public void retrieve_unknownProviderId() {
        final SyncUser adminUser = UserFactory.createAdminUser(Constants.AUTH_URL);
        SyncUser syncUser = adminUser.retrieveUser(SyncCredentials.IdentityProvider.USERNAME_PASSWORD, "doesNotExist");
        assertNull(syncUser);
    }

    @Test
    public void retrieve_invalidProvider() {
        final SyncUser adminUser = UserFactory.createAdminUser(Constants.AUTH_URL);
        final String username = UUID.randomUUID().toString();
        final String password = "password";
        final SyncCredentials credentials = SyncCredentials.usernamePassword(username, password, true);
        final SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);
        assertTrue(user.isValid());

        SyncUser syncUser = adminUser.retrieveUser("invalid", "username");
        assertNull(syncUser);
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
            user1.retrieveUser(SyncCredentials.IdentityProvider.USERNAME_PASSWORD, username2);
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
        adminUser.retrieveUserAsync("password", username, new SyncUser.Callback() {
            @Override
            public void onSuccess(SyncUser syncUser) {

                assertNotNull(syncUser);
                assertEquals(identity, syncUser.getIdentity());
                assertFalse(syncUser.isAdmin());
                assertTrue(syncUser.isValid());
                looperThread.testComplete();
            }

            @Override
            public void onError(ObjectServerError error) {
                fail(error.getErrorMessage());
            }
        });
    }
}
