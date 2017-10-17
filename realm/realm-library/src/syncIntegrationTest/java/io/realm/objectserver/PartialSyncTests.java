package io.realm.objectserver;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.StandardIntegrationTest;
import io.realm.SyncConfiguration;
import io.realm.SyncManager;
import io.realm.SyncUser;
import io.realm.TestHelper;
import io.realm.TestSyncConfigurationFactory;
import io.realm.exceptions.RealmException;
import io.realm.objectserver.model.PartialSyncModule;
import io.realm.objectserver.model.PartialSyncObjectA;
import io.realm.objectserver.model.PartialSyncObjectB;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.UserFactory;

import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class PartialSyncTests extends StandardIntegrationTest {
    @Rule
    public TestSyncConfigurationFactory configFactory = new TestSyncConfigurationFactory();

    @Test
    public void partialSync() throws InterruptedException {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);

        final SyncConfiguration syncConfig = configFactory
                .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .waitForInitialRemoteData()
                .modules(new PartialSyncModule())
                .build();

        final SyncConfiguration partialSyncConfig = configFactory
                .createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .name("partialSync")
                .modules(new PartialSyncModule())
                .partialRealm()
                .build();

        Realm realm = Realm.getInstance(syncConfig);
        realm.beginTransaction();
        PartialSyncObjectA objectA = realm.createObject(PartialSyncObjectA.class);
        objectA.setNumber(0);
        objectA.setString("realm");
        objectA = realm.createObject(PartialSyncObjectA.class);
        objectA.setNumber(1);
        objectA.setString("");
        objectA = realm.createObject(PartialSyncObjectA.class);
        objectA.setNumber(2);
        objectA.setString("");
        objectA = realm.createObject(PartialSyncObjectA.class);
        objectA.setNumber(3);
        objectA.setString("");
        objectA = realm.createObject(PartialSyncObjectA.class);
        objectA.setNumber(4);
        objectA.setString("realm");
        objectA = realm.createObject(PartialSyncObjectA.class);
        objectA.setNumber(5);
        objectA.setString("sync");
        objectA = realm.createObject(PartialSyncObjectA.class);
        objectA.setNumber(6);
        objectA.setString("partial");
        objectA = realm.createObject(PartialSyncObjectA.class);
        objectA.setNumber(7);
        objectA.setString("partial");
        objectA = realm.createObject(PartialSyncObjectA.class);
        objectA.setNumber(8);
        objectA.setString("partial");
        objectA = realm.createObject(PartialSyncObjectA.class);
        objectA.setNumber(9);
        objectA.setString("partial");

        for (int i = 0; i < 10; i++) {
            realm.createObject(PartialSyncObjectB.class).setNumber(i);
        }
        realm.commitTransaction();

        SyncManager.getSession(syncConfig).uploadAllLocalChanges();
        realm.close();

        final CountDownLatch latch = new CountDownLatch(2);

        HandlerThread handlerThread = new HandlerThread("background");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                final Realm partialSyncRealm = Realm.getInstance(partialSyncConfig);
                assertTrue(partialSyncRealm.isEmpty());

                partialSyncRealm.subscribeToObjects(PartialSyncObjectA.class, "number > 5", new Realm.PartialSyncCallback<PartialSyncObjectA>() {

                    @Override
                    public void onSuccess(RealmResults<PartialSyncObjectA> results) {
                        assertEquals(4, results.size());
                        for (PartialSyncObjectA object : results) {
                            assertThat(object.getNumber(), greaterThan(5));
                            assertEquals("partial", object.getString());
                        }
                        // make sure the Realm contains only PartialSyncObjectA
                        assertEquals(0, partialSyncRealm.where(PartialSyncObjectB.class).count());
                        latch.countDown();
                    }

                    @Override
                    public void onError(RealmException error) {
                        fail(error.getMessage());
                    }
                });

                // Invalid query
                partialSyncRealm.subscribeToObjects(PartialSyncObjectA.class, "invalid_property > 5", new Realm.PartialSyncCallback<PartialSyncObjectA>() {

                    @Override
                    public void onSuccess(RealmResults<PartialSyncObjectA> results) {
                        fail("Invalid query should not succeed");
                    }

                    @Override
                    public void onError(RealmException error) {
                        assertNotNull(error);
                        partialSyncRealm.close();
                        latch.countDown();
                    }
                });

            }
        });

        TestHelper.awaitOrFail(latch);
    }
}
