package io.realm.objectserver;

import android.os.SystemClock;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.StandardIntegrationTest;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncManager;
import io.realm.SyncSession;
import io.realm.SyncTestUtils;
import io.realm.SyncUser;
import io.realm.TestHelper;
import io.realm.entities.StringOnly;
import io.realm.exceptions.RealmFileException;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.StringOnlyModule;
import io.realm.objectserver.utils.UserFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class EncryptedSynchronizedRealmTests extends StandardIntegrationTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10);

    // Make sure the encryption is local, i.e after deleting a synced Realm
    // re-open it again with no (or different) key, should be possible.
    @Test
    public void setEncryptionKey_canReOpenRealmWithoutKey() {

        // STEP 1: open a synced Realm using a local encryption key
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);

        final byte[] randomKey = TestHelper.getRandomKey();

        SyncConfiguration configWithEncryption = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .fullSynchronization()
                .modules(new StringOnlyModule())
                .waitForInitialRemoteData()
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        fail(error.getErrorMessage());
                    }
                })
                .encryptionKey(randomKey)
                .build();

        Realm realm = Realm.getInstance(configWithEncryption);
        assertTrue(realm.isEmpty());

        realm.beginTransaction();
        realm.createObject(StringOnly.class).setChars("Hi Alice");
        realm.commitTransaction();

        // STEP 2:  make sure the changes gets to the server
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(2));  // FIXME: Replace with Sync Progress Notifications once available.
        realm.close();
        user.logOut();

        // STEP 3: try to open again the same sync Realm but different local name without the encryption key should not
        // fail
        user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, false), Constants.AUTH_URL);
        SyncConfiguration configWithoutEncryption = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .fullSynchronization()
                .name("newName")
                .modules(new StringOnlyModule())
                .waitForInitialRemoteData()
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        fail(error.getErrorMessage());
                    }
                })
                .build();

        realm = Realm.getInstance(configWithoutEncryption);
        RealmResults<StringOnly> all = realm.where(StringOnly.class).findAll();
        assertEquals(1, all.size());
        assertEquals("Hi Alice", all.get(0).getChars());

        realm.close();
        user.logOut();
    }

    // If an encrypted synced Realm is re-opened with the wrong key, throw an exception.
    @Test
    public void setEncryptionKey_shouldCrashIfKeyNotProvided() throws InterruptedException {
        // STEP 1: open a synced Realm using a local encryption key
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);

        final byte[] randomKey = TestHelper.getRandomKey();

        SyncConfiguration configWithEncryption = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .modules(new StringOnlyModule())
                .waitForInitialRemoteData()
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        fail(error.getErrorMessage());
                    }
                })
                .encryptionKey(randomKey)
                .build();

        Realm realm = Realm.getInstance(configWithEncryption);
        assertTrue(realm.isEmpty());

        realm.beginTransaction();
        realm.createObject(StringOnly.class).setChars("Hi Alice");
        realm.commitTransaction();

        // STEP 2: make sure the changes gets to the server
        SyncManager.getSession(configWithEncryption).uploadAllLocalChanges();

        realm.close();
        user.logOut();

        // STEP 3: try to open again the Realm without the encryption key should fail
        user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, false), Constants.AUTH_URL);
        SyncConfiguration configWithoutEncryption = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .modules(new StringOnlyModule())
                .waitForInitialRemoteData()
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        fail(error.getErrorMessage());
                    }
                })
                .build();

        try {
            realm = Realm.getInstance(configWithoutEncryption);
            fail("It should not be possible to open the Realm without the encryption key set previously.");
        } catch (RealmFileException ignored) {
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    // If client B encrypts its synced Realm, client A should be able to access that Realm with a different encryption key.
    @Test
    public void setEncryptionKey_differentClientsWithDifferentKeys() throws InterruptedException {
        // STEP 1: prepare a synced Realm for client A
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.logIn(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);

        final byte[] randomKey = TestHelper.getRandomKey();

        SyncConfiguration configWithEncryption = configurationFactory.createSyncConfigurationBuilder(user, Constants.USER_REALM)
                .fullSynchronization()
                .modules(new StringOnlyModule())
                .waitForInitialRemoteData()
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        fail(error.getErrorMessage());
                    }
                })
                .encryptionKey(randomKey)
                .build();

        Realm realm = Realm.getInstance(configWithEncryption);
        assertTrue(realm.isEmpty());

        realm.beginTransaction();
        realm.createObject(StringOnly.class).setChars("Hi Alice");
        realm.commitTransaction();

        // STEP 2: make sure the changes gets to the server
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(2));  // FIXME: Replace with Sync Progress Notifications once available.
        realm.close();

        // STEP 3: prepare a synced Realm for client B (admin user)
        SyncUser admin = UserFactory.createAdminUser(Constants.AUTH_URL);
        SyncCredentials credentials = SyncCredentials.accessToken(SyncTestUtils.getRefreshToken(admin).value(), "custom-admin-user");
        SyncUser adminUser = SyncUser.logIn(credentials, Constants.AUTH_URL);

        final byte[] adminRandomKey = TestHelper.getRandomKey();

        SyncConfiguration adminConfigWithEncryption = configurationFactory.createSyncConfigurationBuilder(adminUser, configWithEncryption.getServerUrl().toString())
                .fullSynchronization()
                .modules(new StringOnlyModule())
                .waitForInitialRemoteData()
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        fail(error.getErrorMessage());
                    }
                })
                .encryptionKey(adminRandomKey)
                .build();

        Realm adminRealm = Realm.getInstance(adminConfigWithEncryption);
        RealmResults<StringOnly> all = adminRealm.where(StringOnly.class).findAll();
        assertEquals(1, all.size());
        assertEquals("Hi Alice", all.get(0).getChars());

        adminRealm.beginTransaction();
        adminRealm.createObject(StringOnly.class).setChars("Hi Bob");
        adminRealm.commitTransaction();

        SystemClock.sleep(TimeUnit.SECONDS.toMillis(2));
        adminRealm.close();

        // STEP 4: client A can see changes from client B (although they're using different encryption keys)
        realm = Realm.getInstance(configWithEncryption);
        SyncManager.getSession(configWithEncryption).downloadAllServerChanges();// force download latest commits from ROS
        realm.refresh();//FIXME not calling refresh will still point to the previous version of the Realm without the latest admin commit  "Hi Bob"
        assertEquals(2, realm.where(StringOnly.class).count());

        adminRealm = Realm.getInstance(adminConfigWithEncryption);

        RealmResults<StringOnly> allSorted = realm.where(StringOnly.class).sort(StringOnly.FIELD_CHARS).findAll();
        RealmResults<StringOnly> allSortedAdmin = adminRealm.where(StringOnly.class).sort(StringOnly.FIELD_CHARS).findAll();
        assertEquals("Hi Alice", allSorted.get(0).getChars());
        assertEquals("Hi Bob", allSorted.get(1).getChars());

        assertEquals("Hi Alice", allSortedAdmin.get(0).getChars());
        assertEquals("Hi Bob", allSortedAdmin.get(1).getChars());

        adminRealm.close();
        adminUser.logOut();

        realm.close();
        user.logOut();
    }
}
