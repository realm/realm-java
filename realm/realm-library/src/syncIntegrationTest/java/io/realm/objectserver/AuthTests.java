package io.realm.objectserver;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncManager;
import io.realm.SyncSession;
import io.realm.SyncUser;
import io.realm.log.LogLevel;
import io.realm.log.RealmLog;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.UserFactory;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class AuthTests extends BaseIntegrationTest {
    @Rule
    public RunInLooperThread looperThread = new RunInLooperThread();

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
    public void login_withAccessToken() {
        SyncUser admin = UserFactory.createAdminUser(Constants.AUTH_URL);
        SyncCredentials credentials = SyncCredentials.accessToken(admin.getAccessToken().value(), "custom-admin-user");
        SyncUser.loginAsync(credentials, Constants.AUTH_URL, new SyncUser.Callback() {
            @Override
            public void onSuccess(SyncUser user) {
                final SyncConfiguration config = new SyncConfiguration.Builder(user, Constants.SYNC_SERVER_URL)
                        .errorHandler(new SyncSession.ErrorHandler() {
                            @Override
                            public void onError(SyncSession session, ObjectServerError error) {
                                fail("Session failed: " + error);
                            }
                        })
                        .build();

                final Realm realm = Realm.getInstance(config);
                looperThread.testRealms.add(realm);

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

    // The error handler throws an exception but it is ignored (but logged). That means, this test should not
    // pass and not be stopped by an IllegalArgumentException.
    @Test
    @RunTestInLooperThread
    public void loginAsync_errorHandlerThrows() {
        // set log level to info to make sure the IllegalArgumentException
        // thrown in the test is visible in Logcat
        final int defaultLevel = RealmLog.getLevel();
        RealmLog.setLevel(LogLevel.INFO);
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

        looperThread.postRunnableDelayed(new Runnable() {
            @Override
            public void run() {
                RealmLog.setLevel(defaultLevel);
                looperThread.testComplete();
            }
        }, 1000);
    }

    @Test
    @RunTestInLooperThread
    public void currentUser_multipleUsersThrows() {
        SyncCredentials credentials = SyncCredentials.usernamePassword("user1", "user1", true);
        SyncUser.loginAsync(credentials, Constants.AUTH_URL, new SyncUser.Callback() {
            @Override
            public void onSuccess(final SyncUser user1) {
                // We already have a current user,
                // usually we need to logout before switching to another user
                SyncCredentials credentials = SyncCredentials.usernamePassword("user2", "user2", true);
                SyncUser.loginAsync(credentials, Constants.AUTH_URL, new SyncUser.Callback() {

                    @Override
                    public void onSuccess(SyncUser user2) {
                        assertNotEquals(user1, user2);
                        try {
                            SyncUser.currentUser();
                            fail("Login another user should fail, we support one active user only");
                        } catch (IllegalStateException expected) {
                            looperThread.testComplete();
                        }
                    }

                    @Override
                    public void onError(ObjectServerError error) {
                        fail();
                    }
                });
            }

            @Override
            public void onError(ObjectServerError error) {
                fail();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void currentUser_canSwitchUser() {
        SyncCredentials credentials = SyncCredentials.usernamePassword("user1", "user1", true);
        SyncUser.loginAsync(credentials, Constants.AUTH_URL, new SyncUser.Callback() {
            @Override
            public void onSuccess(final SyncUser user1) {
                SyncCredentials credentials = SyncCredentials.usernamePassword("user2", "user2", true);
                SyncUser.loginAsync(credentials, Constants.AUTH_URL, new SyncUser.Callback() {

                    @Override
                    public void onSuccess(SyncUser user2) {
                        assertNotEquals(user1, user2);

                        user1.logout();
                        SyncUser currentUser = SyncUser.currentUser();
                        assertEquals(user2, currentUser);
                        looperThread.testComplete();
                    }

                    @Override
                    public void onError(ObjectServerError error) {
                        fail();
                    }
                });
            }

            @Override
            public void onError(ObjectServerError error) {
                fail();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void currentUser_logoutThenLogging() {
        // Create and logging with `user1`
        SyncCredentials credentials = SyncCredentials.usernamePassword("user1", "user1", true);
        SyncUser.loginAsync(credentials, Constants.AUTH_URL, new SyncUser.Callback() {
            @Override
            public void onSuccess(final SyncUser user1) {
                assertEquals(user1, SyncUser.currentUser());

                // logout `user1`
                user1.logout();

                // No current active user
                assertNull(SyncUser.currentUser());

                // logging again with `user1`
                SyncCredentials credentials = SyncCredentials.usernamePassword("user1", "user1", false);
                SyncUser.loginAsync(credentials, Constants.AUTH_URL, new SyncUser.Callback() {
                    @Override
                    public void onSuccess(SyncUser user) {
                        // currentUser doesn't throw
                        assertEquals(user, SyncUser.currentUser());

                        assertEquals(user.getIdentity(), user1.getIdentity());
                        looperThread.testComplete();
                    }

                    @Override
                    public void onError(ObjectServerError error) {
                        fail();
                    }
                });
            }

            @Override
            public void onError(ObjectServerError error) {
                fail();
            }
        });
    }

}
