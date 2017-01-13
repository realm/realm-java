package io.realm.objectserver;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.SyncCredentials;
import io.realm.SyncUser;
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

    // The error handler throws an exception but it is ignored (but logged). That means, this test should not
    // pass and not be stopped by an IllegalArgumentException.
    @Test
    @RunTestInLooperThread
    public void loginAsync_errorHandlerThrows() {
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

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
        looperThread.testComplete();
    }
}
