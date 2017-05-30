package io.realm.objectserver;

import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;

import org.junit.Rule;
import org.junit.Test;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncManager;
import io.realm.SyncSession;
import io.realm.SyncUser;
import io.realm.TestHelper;
import io.realm.entities.StringOnly;
import io.realm.exceptions.RealmFileException;
import io.realm.log.LogLevel;
import io.realm.log.RealmLog;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.StringOnlyModule;
import io.realm.rule.TestSyncConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class EncryptedSynchronizedRealmTests extends BaseIntegrationTest {
//    @Rule
//    public Timeout globalTimeout = Timeout.seconds(10);

//    @Rule
//    public final TestSyncConfigurationFactory configurationFactory = new TestSyncConfigurationFactory();
//
    @Test
    public void setEncryptionKey_canOpenRealmWithout() {
//        SyncManager.reset();

        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.login(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);

        final byte[] randomKey = TestHelper.getRandomKey();

        SyncConfiguration configWithEncryption = new SyncConfiguration.Builder(user, Constants.USER_REALM)
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

        // TODO we should not Delete the Realm as the password is not synchronized (local) or not ?
        // make sure the changes gets to the server
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(2));  // FIXME: Replace with Sync Progress Notifications once available.
        realm.close();
        user.logout();
        Realm.deleteRealm(configWithEncryption);

        // Try to open again the Realm without the encryption key should fail
        user = SyncUser.login(SyncCredentials.usernamePassword(username, password, false), Constants.AUTH_URL);
        SyncConfiguration configWithoutEncryption = new SyncConfiguration.Builder(user, Constants.USER_REALM)
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
        user.logout();
    }

    @Test
    public void setEncryptionKey_shouldCrashIfKeyNotProvided() {
        RealmLog.setLevel(LogLevel.ALL);
        String username = UUID.randomUUID().toString();
        String password = "password";
        SyncUser user = SyncUser.login(SyncCredentials.usernamePassword(username, password, true), Constants.AUTH_URL);

        final byte[] randomKey = TestHelper.getRandomKey();

        SyncConfiguration configWithEncryption = new SyncConfiguration.Builder(user, Constants.USER_REALM)
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

        // TODO we should not Delete the Realm as the password is not synchronized (local) or not ?
        // make sure the changes gets to the server
        SystemClock.sleep(TimeUnit.SECONDS.toMillis(2));  // FIXME: Replace with Sync Progress Notifications once available.
        realm.close();
        user.logout();

        // Try to open again the Realm without the encryption key should fail
        user = SyncUser.login(SyncCredentials.usernamePassword(username, password, false), Constants.AUTH_URL);
        SyncConfiguration configWithoutEncryption = new SyncConfiguration.Builder(user, Constants.USER_REALM)
                .modules(new StringOnlyModule())
                .waitForInitialRemoteData()
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        fail(error.getErrorMessage());
                    }
                })
                .build();

        Thread.setDefaultUncaughtExceptionHandler( new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.out.println(">>>>>>>>>>>>>>>>>>>>> HANDLER <<<<<<<<<<<<<<<<<<<<<<");
                e.printStackTrace();
            }
        });

        try {
            realm = Realm.getInstance(configWithoutEncryption);
            fail("It should not be possible to open the Realm without the encryption key set previously.");
        } catch (RealmFileException ignored) {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> STILL: " + ignored.getMessage());

//            final Thread.UncaughtExceptionHandler uncaughtExceptionHandler =;
//            Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
//            Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
//            for (Thread thread : threadArray) {
//                thread.setDefaultUncaughtExceptionHandler();
//            }


//            Thread.setDefaultUncaughtExceptionHandler();
//            realm = Realm.getInstance(configurationFactory
//                    .createSyncConfigurationBuilder(user, Constants.USER_REALM)
//                    .modules(new StringOnlyModule())
//                    .waitForInitialRemoteData()
//                    .errorHandler(new SyncSession.ErrorHandler() {
//                        @Override
//                        public void onError(SyncSession session, ObjectServerError error) {
//                            System.out.println(">>>>>>>>>>>>>>>>>>>>> HANDLER <<<<<<<<<<<<<<<<<<<<<<");
//                            fail(error.getErrorMessage());
//                        }
//                    })
//                    .encryptionKey(randomKey)
//                    .build());
//            System.out.println(">>>>>>>>>>>>>>> REOPENING WITH EXNCRYPTION SUCCEED!");
//            SyncManager.reset();
        }
    }

//    @Test
    public void test() {
        Realm.init(InstrumentationRegistry.getTargetContext());

        final byte[] randomKey = TestHelper.getRandomKey();
        RealmConfiguration configuration = new RealmConfiguration.Builder().modules(new StringOnlyModule()).encryptionKey(randomKey).build();

        Realm instance = Realm.getInstance(configuration);
        instance.beginTransaction();
        instance.createObject(StringOnly.class).setChars("foo");
        instance.commitTransaction();

        instance.close();

        RealmConfiguration configurationWithout = new RealmConfiguration.Builder().modules(new StringOnlyModule()).build();

        try {
            instance = Realm.getInstance(configurationWithout);
        } catch (RealmFileException e) {
            e.printStackTrace();
        }

        assertEquals("foo", instance.where(StringOnly.class).findFirst().getChars());
        instance.close();
    }

    // If client B encrypts its synced Realm, client A should be able to access that Realm with a different encryption key.

    // If an encrypted synced Realm is re-opened with the wrong key, throw an exception.



}
