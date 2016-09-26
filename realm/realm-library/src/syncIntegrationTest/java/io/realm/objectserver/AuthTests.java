package io.realm.objectserver;

import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.Credentials;
import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.User;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.HttpUtils;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class AuthTests {
    @Rule
    public UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

    @BeforeClass
    public static void setUp () throws Exception {
        HttpUtils.startSyncServer();
    }

    @AfterClass
    public static void tearDown () throws Exception {
        HttpUtils.stopSyncServer();
    }

    @Test
    public void login_userNotExist() {
        Credentials credentials = Credentials.usernamePassword("IWantToHackYou", "GeneralPassword", false);
        try {
            User.login(credentials, Constants.AUTH_URL);
        } catch (ObjectServerError expected) {
            // FIXME: It doesn't throw the right exception!
            // The auth server needs an admin user to be created first! Find a workaround for it!!!
            assertEquals(ErrorCode.MISSING_PARAMETERS, expected.getErrorCode());
        }
    }

    // FIXME: Need LooperThread support!
    /*
    @Test
    @UiThreadTest
    public void loginAsync_userNotExist() {
        Credentials credentials = Credentials.usernamePassword("IWantToHackYou", "GeneralPassword", false);
        User.loginAsync(credentials, Constants.AUTH_SERVER_URL, new User.Callback() {
            @Override
            public void onSuccess(User user) {
                fail();
            }

            @Override
            public void onError(ObjectServerError error) {
                // FIXME: It should check the error code!!
                assertNotNull(error);
            }
        });
        Looper.loop();
    }
    */
}
