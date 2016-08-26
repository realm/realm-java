package io.realm.objectserver;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.rule.TestRealmConfigurationFactory;

@RunWith(AndroidJUnit4.class)
public class SyncConfigurationtests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    private Context context;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getTargetContext();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void user() {
//        new SyncConfiguration.Builder(context);
        // Check that user can be added
        // That the default local path is correct
    }

    @Test
    public void user_invalidUserThrows() {
        // Null user
        // Not authenticated user
    }

    @Test
    public void serverUrl() {

    }

    @Test
    public void serverUrl_invalidUrlThrows() {
        // null url
        // non-valid URI
        // Ending with .realm
    }

    @Test
    public void userAndServerUrlRequired() {
        // user/url is required
    }

    @Test
    public void autoConnect_true() {

    }

    @Test
    public void autoConnect_false() {

    }

    @Test
    public void errorHandler() {

    }

    @Test
    public void errorHandler_null() {

    }

    @Test
    public void syncPolicy() {

    }

    @Test
    public void syncPolicy_nullThrows() {

    }
}
