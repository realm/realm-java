package io.realm;

import android.os.SystemClock;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.UUID;

import io.realm.log.RealmLog;
import io.realm.objectserver.BaseIntegrationTest;
import io.realm.objectserver.utils.Constants;
import io.realm.permissions.Permission;
import io.realm.permissions.PermissionManager;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestSyncConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class PermissionManagerTests extends BaseIntegrationTest {

    @Rule
    public RunInLooperThread looperThread = new RunInLooperThread();

    @Rule
    public final TestSyncConfigurationFactory configurationFactory = new TestSyncConfigurationFactory();

    private SyncUser user;

    @Before
    public void setUpTest() {
        String username = UUID.randomUUID().toString();
        RealmLog.error("Username %s", username);
        SyncCredentials credentials = SyncCredentials.usernamePassword(username, "password", true);
        user = SyncUser.login(credentials, Constants.AUTH_URL);
        // FIXME: Timing issue in ROS. Permission Realm might not be immediately available
        // after login.
        SystemClock.sleep(2000);
    }

    @Test
    @RunTestInLooperThread
    public void getPermissionsAsync_returnLoadedResults() {
        // For a new user, the PermissionManager should contain 1 entry for the __permission Realm
        PermissionManager pm = user.getPermissionManager();
        looperThread.keepStrongReference(pm);
        pm.getPermissionsAsync(new PermissionManager.Callback<RealmResults<Permission>>() {
            @Override
            public void onSuccess(RealmResults<Permission> permissions) {
                assertTrue(permissions.isLoaded());
                assertEquals(1, permissions.size());
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
    public void getPermissionsAsync_noLongerValidWhenPermissionManagerIsClosed() {
        final PermissionManager pm = user.getPermissionManager();
        looperThread.keepStrongReference(pm);
        pm.getPermissionsAsync(new PermissionManager.Callback<RealmResults<Permission>>() {
            @Override
            public void onSuccess(RealmResults<Permission> permissions) {
                assertTrue(permissions.isValid());
                pm.close();
                assertFalse(permissions.isValid());
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
    public void getPermissionsAsync_updatedWithNewRealms() {
        PermissionManager pm = user.getPermissionManager();
        looperThread.keepStrongReference(pm);
        pm.getPermissionsAsync(new PermissionManager.Callback<RealmResults<Permission>>() {
            @Override
            public void onSuccess(RealmResults<Permission> permissions) {
                assertTrue(permissions.isLoaded());
                assertEquals(1, permissions.size());

                // Create new Realm, which should create a new Permission entry
                SyncConfiguration config2 = new SyncConfiguration.Builder(user, Constants.USER_REALM_2).build();
                final Realm secondRealm = Realm.getInstance(config2);
                looperThread.keepStrongReference(secondRealm);

                // Wait for the permission Result to report the new Realm
                looperThread.keepStrongReference(permissions);
                permissions.addChangeListener(new RealmChangeListener<RealmResults<Permission>>() {
                    @Override
                    public void onChange(RealmResults<Permission> permissions) {
                        assertEquals(2, permissions.size());
                        secondRealm.close();
                        looperThread.testComplete();
                    }
                });
            }

            @Override
            public void onError(ObjectServerError error) {
                fail("Could not open Realm: " + error.toString());
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void getPermissionsAsync_closed() throws IOException {
        PermissionManager pm = user.getPermissionManager();
        looperThread.keepStrongReference(pm);
        pm.close();

        pm.getPermissionsAsync(new PermissionManager.Callback<RealmResults<Permission>>() {
            @Override
            public void onSuccess(RealmResults<Permission> permissions) {
                fail();
            }

            @Override
            public void onError(ObjectServerError error) {
                assertEquals(ErrorCode.UNKNOWN, error.getErrorCode());
                assertEquals(IllegalStateException.class, error.getException().getClass());
                looperThread.testComplete();
            }
        });


    }


}
