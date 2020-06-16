package io.realm;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Ignore;
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
import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.StandardIntegrationTest;
import io.realm.SyncConfiguration;
import io.realm.SyncManager;
import io.realm.SyncSession;
import io.realm.SyncTestUtils;
import io.realm.SyncUser;
import io.realm.SyncUserInfo;
import io.realm.TestHelper;
import io.realm.entities.StringOnly;
import io.realm.internal.Util;
import io.realm.internal.async.RealmAsyncTaskImpl;
import io.realm.internal.objectserver.Token;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.StringOnlyModule;
import io.realm.objectserver.utils.UserFactory;
import io.realm.rule.RunTestInLooperThread;

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
public class AuthTests extends StandardIntegrationTest {

    @Test
    public void cachedInstanceShouldNotThrowIfRefreshTokenExpires() throws InterruptedException {
        String username = UUID.randomUUID().toString();
        String password = "password";

        SyncCredentials credentials = SyncCredentials.usernamePassword(username, password, true);
        // TODO: A bit unsure how to make the user invalid ... maybe delete the user on the server using the admin SDK?
        final SyncUser user = spy(SyncUser.logIn(credentials, Constants.AUTH_URL));

        when(user.isValid()).thenReturn(true, true, false);

        final RealmConfiguration configuration = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM).build();
        Realm realm = Realm.getInstance(configuration);

        assertFalse(user.isValid());
        verify(user, times(3)).isValid();

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
        user.logOut();
    }

    // TODO: Not sure if we already have tests for this....This sounds like it should be in SyncConfigurationTests
    @Test
    public void buildingSyncConfigurationShouldThrowIfInvalidUser() {
        String username = UUID.randomUUID().toString();
        String password = "password";

        SyncCredentials credentials = SyncCredentials.usernamePassword(username, password, true);
        SyncUser user = SyncUser.logIn(credentials, Constants.AUTH_URL);
        SyncUser currentUser = SyncUser.current();
        user.logOut();

        assertFalse(user.isValid());

        try {
            // We should not be able to build a configuration with an invalid/logged out user
            configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM).build();
            fail("Invalid user, it should not be possible to create a SyncConfiguration");
        } catch (IllegalStateException expected) {
            // User not authenticated or authentication expired.
        }

        try {
            // We should not be able to build a configuration with an invalid/logged out user
            configurationFactory.createSyncConfigurationBuilder(currentUser, Constants.USER_REALM).build();
            fail("Invalid currentUser, it should not be possible to create a SyncConfiguration");
        } catch (IllegalStateException expected) {
            // User not authenticated or authentication expired.
        }
    }

    // Same as above test
    // using a logout user should not throw
    @Test
    public void usingConfigurationWithInvalidUserShouldNotThrow() {
        String username = UUID.randomUUID().toString();
        String password = "password";

        SyncCredentials credentials = SyncCredentials.usernamePassword(username, password, true);
        SyncUser user = SyncUser.logIn(credentials, Constants.AUTH_URL);
        RealmConfiguration configuration = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM).build();
        user.logOut();
        assertFalse(user.isValid());
        Realm instance = Realm.getInstance(configuration);
        instance.close();
    }

    // TODO: Check if this test is in UserTests
    @Test
    public void logout_currentUserMoreThanOne() {
        UserFactory.createUniqueUser(Constants.AUTH_URL);
        SyncUser.current().logOut();
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        assertEquals(user, SyncUser.current());
    }

    // TODO: Check if this test is in UserTests
    // logging out 'user' should have the same impact on other instance(s) of the same user
    @Test
    public void loggingOutUserShouldImpactOtherInstances() throws InterruptedException {
        String username = UUID.randomUUID().toString();
        String password = "password";

        SyncCredentials credentials = SyncCredentials.usernamePassword(username, password, true);
        SyncUser user = SyncUser.logIn(credentials, Constants.AUTH_URL);
        SyncUser currentUser = SyncUser.current();

        assertTrue(user.isValid());
        assertEquals(user, currentUser);

        user.logOut();

        assertFalse(user.isValid());
        assertFalse(currentUser.isValid());
    }

    // TODO: Check if this test is in UserTests
    // logging out 'current' should have the same impact on other instance(s) of the user
    @Test
    public void loggingOutCurrentUserShouldImpactOtherInstances() throws InterruptedException {
        String username = UUID.randomUUID().toString();
        String password = "password";

        SyncCredentials credentials = SyncCredentials.usernamePassword(username, password, true);
        SyncUser user = SyncUser.logIn(credentials, Constants.AUTH_URL);
        SyncUser currentUser = SyncUser.current();

        assertTrue(user.isValid());
        assertEquals(user, currentUser);

        SyncUser.current().logOut();

        assertFalse(user.isValid());
        assertFalse(currentUser.isValid());
        assertNull(SyncUser.current());
    }

    // TODO: Check if this test is in UserTests
    // verify that a single user can be logged out and back in.
    @Test
    public void singleUserCanBeLoggedInAndOutRepeatedly() {
        final String username = UUID.randomUUID().toString();
        final String password = "password";

        // register the user the first time
        SyncCredentials credentials = SyncCredentials.usernamePassword(username, password, true);

        SyncUser user = SyncUser.logIn(credentials, Constants.AUTH_URL);
        assertTrue(user.isValid());
        user.logOut();
        assertFalse(user.isValid());

        // on subsequent logins, the user is already registered.
        credentials = credentials = SyncCredentials.usernamePassword(username, password, false);
        for (int i = 0; i < 3; i++) {
            user = SyncUser.logIn(credentials, Constants.AUTH_URL);
            assertTrue(user.isValid());
            user.logOut();
            assertFalse(user.isValid());
        }
    }

    // TODO: Check if this test is in UserTests
    @Test
    public void revokedRefreshTokenIsNotSameAfterLogin() throws InterruptedException {
        final CountDownLatch userLoggedInAgain = new CountDownLatch(1);
        final String uniqueName = UUID.randomUUID().toString();

        final SyncCredentials credentials = SyncCredentials.usernamePassword(uniqueName, "password", true);
        SyncUser user = SyncUser.logIn(credentials, Constants.AUTH_URL);
        final Token revokedRefreshToken = SyncTestUtils.getRefreshToken(user);

        SyncManager.addAuthenticationListener(new AuthenticationListener() {
            @Override
            public void loggedIn(SyncUser user) {

            }

            @Override
            public void loggedOut(SyncUser user) {
                SyncCredentials credentials = SyncCredentials.usernamePassword(uniqueName, "password", false);
                SyncUser loggedInUser = SyncUser.logIn(credentials, Constants.AUTH_URL);

                Token token = SyncTestUtils.getRefreshToken(loggedInUser);
                // still comparing the same user
                assertEquals(revokedRefreshToken.identity(), token.identity());

                // different tokens
                assertNotEquals(revokedRefreshToken.value(), token.value());
                SyncManager.removeAuthenticationListener(this);
                userLoggedInAgain.countDown();
            }
        });

        user.logOut();
        TestHelper.awaitOrFail(userLoggedInAgain);
    }

}
