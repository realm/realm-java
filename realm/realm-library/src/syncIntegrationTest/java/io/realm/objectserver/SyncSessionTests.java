package io.realm.objectserver;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import io.realm.BaseIntegrationTest;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncManager;
import io.realm.SyncSession;
import io.realm.SyncUser;
import io.realm.TestHelper;
import io.realm.entities.AllTypes;
import io.realm.entities.StringOnly;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.StringOnlyModule;
import io.realm.objectserver.utils.UserFactory;
import io.realm.rule.TestSyncConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class SyncSessionTests extends BaseIntegrationTest {
    @Rule
    public TestSyncConfigurationFactory configFactory = new TestSyncConfigurationFactory();

    @Test(timeout=3000)
    public void getState_active() {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        SyncConfiguration syncConfiguration = configFactory
                .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .build();
        Realm realm = Realm.getInstance(syncConfiguration);

        SyncSession session = SyncManager.getSession(syncConfiguration);

        // make sure the `access_token` is acquired. otherwise we can still be
        // in WAITING_FOR_ACCESS_TOKEN state
        while(session.getState() != SyncSession.State.ACTIVE) {
            SystemClock.sleep(200);
        }

        realm.close();
    }

    @Test
    public void getState_throwOnClosedSession() {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        SyncConfiguration syncConfiguration = configFactory
                .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .build();
        Realm realm = Realm.getInstance(syncConfiguration);

        SyncSession session = SyncManager.getSession(syncConfiguration);
        realm.close();
        user.logout();
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Could not find session, Realm was probably closed");
        session.getState();
    }

    @Test
    public void getState_loggedOut() {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        SyncConfiguration syncConfiguration = configFactory
                .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .build();
        Realm realm = Realm.getInstance(syncConfiguration);

        SyncSession session = SyncManager.getSession(syncConfiguration);

        user.logout();

        SyncSession.State state = session.getState();
        assertEquals(SyncSession.State.INACTIVE, state);

        realm.close();
    }

    @Test
    public void uploadDownloadAllChanges() throws InterruptedException {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        SyncUser adminUser = UserFactory.createAdminUser(Constants.AUTH_URL);
        SyncConfiguration userConfig = configFactory
                .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .build();
        SyncConfiguration adminConfig = configFactory
                .createSyncConfigurationBuilder(adminUser, userConfig.getServerUrl().toString())
                .build();

        Realm userRealm = Realm.getInstance(userConfig);
        userRealm.beginTransaction();
        userRealm.createObject(AllTypes.class);
        userRealm.commitTransaction();
        SyncManager.getSession(userConfig).uploadAllLocalChanges();
        userRealm.close();

        Realm adminRealm = Realm.getInstance(adminConfig);
        SyncManager.getSession(adminConfig).downloadAllServerChanges();
        adminRealm.refresh();
        assertEquals(1, adminRealm.where(AllTypes.class).count());
        adminRealm.close();
    }

    // check that logging out a SyncUser used by different Realm will
    // affect all associated sessions.
    @Test(timeout=5000)
    public void logout_sameSyncUserMultipleSessions() {
        String uniqueName = UUID.randomUUID().toString();
        SyncCredentials credentials = SyncCredentials.usernamePassword(uniqueName, "password", true);
        SyncUser user =  SyncUser.login(credentials, Constants.AUTH_URL);

        SyncConfiguration syncConfiguration1 = configFactory
                .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .build();
        Realm realm1 = Realm.getInstance(syncConfiguration1);

        SyncConfiguration syncConfiguration2 = configFactory
                .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL_2)
                .build();
        Realm realm2 = Realm.getInstance(syncConfiguration2);

        SyncSession session1 = SyncManager.getSession(syncConfiguration1);
        SyncSession session2 = SyncManager.getSession(syncConfiguration2);

        // make sure the `access_token` is acquired. otherwise we can still be
        // in WAITING_FOR_ACCESS_TOKEN state
        while(session1.getState() != SyncSession.State.ACTIVE || session2.getState() != SyncSession.State.ACTIVE) {
            SystemClock.sleep(200);
        }
        assertEquals(SyncSession.State.ACTIVE, session1.getState());
        assertEquals(SyncSession.State.ACTIVE, session2.getState());
        assertNotEquals(session1, session2);

        assertEquals(session1.getUser(), session2.getUser());

        user.logout();

        assertEquals(SyncSession.State.INACTIVE, session1.getState());
        assertEquals(SyncSession.State.INACTIVE, session2.getState());

        credentials = SyncCredentials.usernamePassword(uniqueName, "password", false);
        SyncUser.login(credentials, Constants.AUTH_URL);

        // reviving the sessions
        assertEquals(SyncSession.State.WAITING_FOR_ACCESS_TOKEN, session1.getState());
        assertEquals(SyncSession.State.WAITING_FOR_ACCESS_TOKEN, session2.getState());

        realm1.close();
        realm2.close();
    }

    // A Realm that was opened before a user logged out should be able to resume uploading if the user logs back in.
    @Test
    public void logBackResumeUpload() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        final String uniqueName = UUID.randomUUID().toString();
        SyncCredentials credentials = SyncCredentials.usernamePassword(uniqueName, "password", true);
        SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);

        final SyncConfiguration syncConfiguration = configFactory
                .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .modules(new StringOnlyModule())
                .waitForInitialRemoteData()
                .build();
        final Realm realm = Realm.getInstance(syncConfiguration);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObject(StringOnly.class).setChars("1");
            }
        });

        final SyncSession session = SyncManager.getSession(syncConfiguration);
        session.uploadAllLocalChanges();

        user.logout();

        // add a commit while we're still offline
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObject(StringOnly.class).setChars("2");
            }
        });

        final CountDownLatch testCompleted = new CountDownLatch(1);

        final HandlerThread handlerThread = new HandlerThread("HandlerThread");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        handler.post(new Runnable() {
            @Override
            public void run() {
                // access the Realm from an different path on the device (using admin user), then monitor
                // when the offline commits get synchronized
                SyncUser admin = UserFactory.createAdminUser(Constants.AUTH_URL);
                SyncCredentials credentialsAdmin = SyncCredentials.accessToken(admin.getAccessToken().value(), "custom-admin-user");
                SyncUser adminUser = SyncUser.login(credentialsAdmin, Constants.AUTH_URL);

                SyncConfiguration adminConfig = configurationFactory.createSyncConfigurationBuilder(adminUser, syncConfiguration.getServerUrl().toString())
                        .modules(new StringOnlyModule())
                        .waitForInitialRemoteData()
                        .build();
                final Realm adminRealm = Realm.getInstance(adminConfig);

                RealmResults<StringOnly> all = adminRealm.where(StringOnly.class).findAllSorted(StringOnly.FIELD_CHARS);
                RealmChangeListener<RealmResults<StringOnly>> realmChangeListener = new RealmChangeListener<RealmResults<StringOnly>>() {
                    @Override
                    public void onChange(RealmResults<StringOnly> stringOnlies) {
                        if (stringOnlies.size() == 2) {
                            Assert.assertEquals("1", stringOnlies.get(0).getChars());
                            Assert.assertEquals("2", stringOnlies.get(1).getChars());
                            adminRealm.close();
                            testCompleted.countDown();
                            handlerThread.quit();
                        }
                    }
                };
                all.addChangeListener(realmChangeListener);

                // login again to re-activate the user
                SyncCredentials credentials = SyncCredentials.usernamePassword(uniqueName, "password", false);
                // this login will re-activate the logged out user, and resume all it's pending sessions
                // the OS will trigger bindSessionWithConfig with the new refresh_token, in order to obtain
                // a new access_token.
                SyncUser.login(credentials, Constants.AUTH_URL);
            }
        });

        TestHelper.awaitOrFail(testCompleted, 60);
    }

    // A Realm that was opened before a user logged out should be able to resume uploading if the user logs back in.
    // this test validate the behaviour of SyncSessionStopPolicy::AfterChangesUploaded
    @Test
    public void uploadChangesWhenRealmOutOfScope() throws InterruptedException {
        final String uniqueName = UUID.randomUUID().toString();
        SyncCredentials credentials = SyncCredentials.usernamePassword(uniqueName, "password", true);
        SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);

        final char[] chars = new char[1_000_000];// 2MB
        Arrays.fill(chars, '.');
        final String twoMBString = new String(chars);

        final SyncConfiguration syncConfiguration = configFactory
                .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .modules(new StringOnlyModule())
                .build();
        Realm realm = Realm.getInstance(syncConfiguration);

        realm.beginTransaction();
        // upload 50MB
        for (int i = 0; i < 25; i++) {
            realm.createObject(StringOnly.class).setChars(twoMBString);
        }
        realm.commitTransaction();
        realm.close();

        final CountDownLatch testCompleted = new CountDownLatch(1);

        final HandlerThread handlerThread = new HandlerThread("HandlerThread");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        handler.post(new Runnable() {
            @Override
            public void run() {
                // using an admin user to open the Realm on different path on the device to monitor when all the uploads are done
                SyncUser admin = UserFactory.createAdminUser(Constants.AUTH_URL);
                SyncCredentials credentialsAdmin = SyncCredentials.accessToken(admin.getAccessToken().value(), "custom-admin-user");
                SyncUser adminUser = SyncUser.login(credentialsAdmin, Constants.AUTH_URL);

                SyncConfiguration adminConfig = configurationFactory.createSyncConfigurationBuilder(adminUser, syncConfiguration.getServerUrl().toString())
                        .modules(new StringOnlyModule())
                        .build();
                final Realm adminRealm = Realm.getInstance(adminConfig);
                RealmResults<StringOnly> all = adminRealm.where(StringOnly.class).findAll();
                RealmChangeListener<RealmResults<StringOnly>> realmChangeListener = new RealmChangeListener<RealmResults<StringOnly>>() {
                    @Override
                    public void onChange(RealmResults<StringOnly> stringOnlies) {
                        if (stringOnlies.size() == 25) {
                            for (int i = 0; i < 25; i++) {
                                assertEquals(1_000_000, stringOnlies.get(i).getChars().length());
                            }
                            adminRealm.close();
                            testCompleted.countDown();
                            handlerThread.quit();
                        }
                    }
                };
                all.addChangeListener(realmChangeListener);
            }
        });

        TestHelper.awaitOrFail(testCompleted, 60);

        user.logout();
        realm.close();
    }

    // A Realm that was opened before a user logged out should be able to resume downloading if the user logs back in.
    @Test
    public void downloadChangesWhenRealmOutOfScope() throws InterruptedException {
        final String uniqueName = UUID.randomUUID().toString();
        SyncCredentials credentials = SyncCredentials.usernamePassword(uniqueName, "password", true);
        SyncUser user = SyncUser.login(credentials, Constants.AUTH_URL);

        final SyncConfiguration syncConfiguration = configFactory
                .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .modules(new StringOnlyModule())
                .build();
        Realm realm = Realm.getInstance(syncConfiguration);

        realm.beginTransaction();
        realm.createObject(StringOnly.class).setChars("1");
        realm.commitTransaction();

        SyncSession session = SyncManager.getSession(syncConfiguration);
        session.uploadAllLocalChanges();

        // Log out the user.
        user.logout();

        // Log the user back in.
        credentials = SyncCredentials.usernamePassword(uniqueName, "password", false);
        SyncUser.login(credentials, Constants.AUTH_URL);

        // now let the admin upload some commits
        final CountDownLatch backgroundUpload = new CountDownLatch(1);

        final HandlerThread handlerThread = new HandlerThread("HandlerThread");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        handler.post(new Runnable() {
            @Override
            public void run() {
                // using an admin user to open the Realm on different path on the device then some commits
                SyncUser admin = UserFactory.createAdminUser(Constants.AUTH_URL);
                SyncCredentials credentialsAdmin = SyncCredentials.accessToken(admin.getAccessToken().value(), "custom-admin-user");
                SyncUser adminUser = SyncUser.login(credentialsAdmin, Constants.AUTH_URL);

                SyncConfiguration adminConfig = configurationFactory.createSyncConfigurationBuilder(adminUser, syncConfiguration.getServerUrl().toString())
                        .modules(new StringOnlyModule())
                        .waitForInitialRemoteData()
                        .build();

                final Realm adminRealm = Realm.getInstance(adminConfig);
                adminRealm.beginTransaction();
                adminRealm.createObject(StringOnly.class).setChars("2");
                adminRealm.createObject(StringOnly.class).setChars("3");
                adminRealm.commitTransaction();

                try {
                    SyncManager.getSession(adminConfig).uploadAllLocalChanges();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    fail(e.getMessage());
                }

                backgroundUpload.countDown();
                handlerThread.quit();
            }
        });

        TestHelper.awaitOrFail(backgroundUpload, 60);
        // Resume downloading
        session.downloadAllServerChanges();
        realm.refresh();//FIXME not calling refresh will still point to the previous version of the Realm count == 1
        assertEquals(3, realm.where(StringOnly.class).count());
        realm.close();
    }
}
