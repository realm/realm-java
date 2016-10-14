package io.realm.objectserver;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.Credentials;
import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.User;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.HttpUtils;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class AuthTests {
    @Rule
    public RunInLooperThread looperThread = new RunInLooperThread();

    @BeforeClass
    public static void setUp () throws Exception {
        Realm.init(InstrumentationRegistry.getContext());
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
            fail();
        } catch (ObjectServerError expected) {
            assertEquals(ErrorCode.UNKNOWN_ACCOUNT, expected.getErrorCode());
        }
    }

    @Test
    @RunTestInLooperThread
    public void loginAsync_userNotExist() {
        Credentials credentials = Credentials.usernamePassword("IWantToHackYou", "GeneralPassword", false);
        User.loginAsync(credentials, Constants.AUTH_URL, new User.Callback() {
            @Override
            public void onSuccess(User user) {
                fail();
            }

            @Override
            public void onError(ObjectServerError error) {
                assertEquals(ErrorCode.UNKNOWN_ACCOUNT, error.getErrorCode());
                looperThread.testComplete();
            }
        });
    }
}
