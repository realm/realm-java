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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.BaseIntegrationTest;
import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncManager;
import io.realm.SyncSession;
import io.realm.SyncUser;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.UserFactory;
import io.realm.rule.RunTestInLooperThread;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertFalse;


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

}
