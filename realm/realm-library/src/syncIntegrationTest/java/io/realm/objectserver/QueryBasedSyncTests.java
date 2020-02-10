package io.realm.objectserver;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.DynamicRealm;
import io.realm.OrderedCollectionChangeSet;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.StandardIntegrationTest;
import io.realm.SyncConfiguration;
import io.realm.SyncManager;
import io.realm.SyncTestUtils;
import io.realm.SyncUser;
import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.entities.Dog;
import io.realm.objectserver.model.PartialSyncModule;
import io.realm.objectserver.model.PartialSyncObjectA;
import io.realm.objectserver.model.PartialSyncObjectB;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.UserFactory;
import io.realm.rule.RunTestInLooperThread;

import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class QueryBasedSyncTests extends StandardIntegrationTest {

    private static final int TEST_SIZE = 10;

    @Test
    @RunTestInLooperThread
    public void invalidQuery() {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        final SyncConfiguration partialSyncConfig = configurationFactory.createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .build();
        final Realm realm = Realm.getInstance(partialSyncConfig);
        looperThread.closeAfterTest(realm);

        // Backlinks not yet supported: https://github.com/realm/realm-core/pull/2947
        RealmResults<AllJavaTypes> query = realm.where(AllJavaTypes.class).equalTo("objectParents.fieldString", "Foo").findAllAsync();
        query.addChangeListener((results, changeSet) -> {
                    if (changeSet.getState() == OrderedCollectionChangeSet.State.ERROR) {
                        assertTrue(changeSet.getError() instanceof IllegalArgumentException);
                        Throwable iae = changeSet.getError();
                        assertTrue(iae.getMessage().contains("Querying over backlinks is disabled but backlinks were found"));
                        looperThread.testComplete();
                    }
                });
        looperThread.keepStrongReference(query);
    }

    // List queries are operating on data that are always up to date as data in a list will
    // always be fetched as part of another top-level subscription. Thus `remoteDataLoaded` is
    // always true and no queries on them can fail.
    @Test
    @RunTestInLooperThread
    public void listQueries_doNotCreateSubscriptions() {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        final SyncConfiguration partialSyncConfig = configurationFactory.createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .build();

        final DynamicRealm dRealm = DynamicRealm.getInstance(partialSyncConfig);
        final Realm realm = Realm.getInstance(partialSyncConfig);
        looperThread.closeAfterTest(dRealm);
        looperThread.closeAfterTest(realm);

        realm.beginTransaction();
        RealmList<Dog> list = realm.createObject(AllTypes.class).getColumnRealmList();
        list.add(new Dog("Fido"));
        list.add(new Dog("Eido"));
        realm.commitTransaction();

        RealmResults<Dog> query = list.where().sort("name").findAllAsync();
        query.addChangeListener((dogs, changeSet) -> {
            assertEquals(OrderedCollectionChangeSet.State.INITIAL, changeSet.getState());
            assertEquals(0, dRealm.where("__ResultSets").count());
            looperThread.testComplete();
        });
        looperThread.keepStrongReference(query);
    }

    @Test
    @RunTestInLooperThread
    public void anonymousSubscription() throws InterruptedException {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        createServerData(user, Constants.SYNC_SERVER_URL);

        // Download data in partial Realm
        final Realm partialSyncRealm = getPartialRealm(user);
        looperThread.closeAfterTest(partialSyncRealm);
        assertTrue(partialSyncRealm.isEmpty());

        RealmResults<PartialSyncObjectA> results = partialSyncRealm.where(PartialSyncObjectA.class)
                .greaterThan("number", 5)
                .findAllAsync();
        looperThread.keepStrongReference(results);

        results.addChangeListener((partialSyncObjectAS, changeSet) -> {
            if (changeSet.isCompleteResult()) {
                if (results.size() == 4) {
                    for (PartialSyncObjectA object : results) {
                        assertThat(object.getNumber(), greaterThan(5));
                        assertEquals("partial", object.getString());
                    }
                    // make sure the Realm contains only PartialSyncObjectA
                    assertEquals(0, partialSyncRealm.where(PartialSyncObjectB.class).count());
                    looperThread.testComplete();
                }
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void namedSubscription() throws InterruptedException {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        createServerData(user, Constants.SYNC_SERVER_URL);

        // Download data in partial Realm
        final Realm partialSyncRealm = getPartialRealm(user);
        looperThread.closeAfterTest(partialSyncRealm);
        assertTrue(partialSyncRealm.isEmpty());

        RealmResults<PartialSyncObjectA> results = partialSyncRealm.where(PartialSyncObjectA.class)
                .greaterThan("number", 5)
                .findAllAsync("my-subscription-id");
        looperThread.keepStrongReference(results);

        results.addChangeListener((partialSyncObjectAS, changeSet) -> {
            if (changeSet.isCompleteResult()) {
                if (results.size() == 4) {
                    for (PartialSyncObjectA object : results) {
                        assertThat(object.getNumber(), greaterThan(5));
                        assertEquals("partial", object.getString());
                    }
                    // make sure the Realm contains only PartialSyncObjectA
                    assertEquals(0, partialSyncRealm.where(PartialSyncObjectB.class).count());
                    looperThread.testComplete();
                }
            }
        });

    }

    @Test
    @RunTestInLooperThread
    public void partialSync_namedSubscriptionThrowsOnNonPartialRealms() {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        final SyncConfiguration fullSyncConfig = configurationFactory.createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .fullSynchronization()
                .name("fullySynchronizedRealm")
                .build();

        Realm realm = Realm.getInstance(fullSyncConfig);
        looperThread.closeAfterTest(realm);

        try {
           realm.where(PartialSyncObjectA.class).findAllAsync("my-id");
           fail();
        } catch (IllegalStateException ignore) {
            looperThread.testComplete();
        }
    }

    @Test
    @RunTestInLooperThread
    public void partialSync_namedSubscription_namedConflictThrows() {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        Realm realm = getPartialRealm(user);
        looperThread.closeAfterTest(realm);

        RealmResults<PartialSyncObjectA> results1 = realm.where(PartialSyncObjectA.class)
                .findAllAsync("my-id");
        results1.addChangeListener((results, changeSet) -> {
            // Ignore. Just used to trigger partial sync path
        });

        RealmResults<PartialSyncObjectB> results2 = realm.where(PartialSyncObjectB.class)
                .findAllAsync("my-id");
        results2.addChangeListener((results, changeSet) -> {
            if (changeSet.getState() == OrderedCollectionChangeSet.State.ERROR) {
                assertEquals(OrderedCollectionChangeSet.State.ERROR, changeSet.getState());
                assertTrue(changeSet.getError() instanceof IllegalArgumentException);
                looperThread.testComplete();
            }
        });

        looperThread.keepStrongReference(results1);
        looperThread.keepStrongReference(results2);
    }

    @Test
    @RunTestInLooperThread
    public void unsubscribeAsync() throws InterruptedException {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        createServerData(user, Constants.SYNC_SERVER_URL);
        Realm realm = getPartialRealm(user);
        looperThread.closeAfterTest(realm);

        final String subscriptionName = "my-objects";
        RealmResults<PartialSyncObjectB> r = realm.where(PartialSyncObjectB.class)
                .greaterThan("number", 0)
                .findAllAsync(subscriptionName);

        r.addChangeListener((results, changeSet) -> {
            if (changeSet.isCompleteResult()) {
                // 1. Partial sync downloaded all expected objects
                assertEquals(TEST_SIZE - 1, results.size());
                r.removeAllChangeListeners();

                // 2. Attempt to remove them again
                realm.unsubscribeAsync(subscriptionName, new Realm.UnsubscribeCallback() {
                    @Override
                    public void onSuccess(String subscriptionName) {
                        assertEquals(subscriptionName, subscriptionName);

                        // Use global Realm change listener to avoid re-subscribing
                        realm.addChangeListener(new RealmChangeListener<Realm>() {
                            @Override
                            public void onChange(Realm realm) {
                                // Eventually they should be removed
                                if (realm.where(PartialSyncObjectB.class).count() == 0) {
                                    looperThread.testComplete();
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String subscriptionName, Throwable error) {
                        fail(error.toString());
                    }
                });
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void unsubscribeAsync_nonExistingIdThrows() {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        Realm realm = getPartialRealm(user);
        looperThread.closeAfterTest(realm);

        realm.unsubscribeAsync("i-dont-exist", new Realm.UnsubscribeCallback() {
            @Override
            public void onSuccess(String subscriptionName) {
                fail();
            }

            @Override
            public void onError(String subscriptionName, Throwable error) {
                assertEquals("i-dont-exist", subscriptionName);
                assertTrue(error instanceof IllegalArgumentException);
                assertTrue(error.getMessage().contains("No active subscription named"));
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void clearTable() {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        Realm realm = getPartialRealm(user);
        looperThread.closeAfterTest(realm);

        // Create test data and make sure it is uploaded to the server
        RealmResults<PartialSyncObjectA> result = realm.where(PartialSyncObjectA.class).findAllAsync();
        realm.executeTransaction(r -> {
            r.createObject(PartialSyncObjectA.class).setString("ObjectA");
        });
        SyncTestUtils.syncRealm(realm);
        assertEquals(1, result.size());

        // Delete data and make sure it is accepted by the server
        realm.executeTransaction(r -> {
            // TODO the API's that actual use the clearTable instruction have all been disabled for now
            // and are throwing IllegalStateException (realm.delete(Class) and realm.deleteAll).
            // Keep the test for time being, but use the recommend workaround for deleting objects
            // instead.
            r.where(PartialSyncObjectA.class).findAll().deleteAllFromRealm();
        });
        SyncTestUtils.syncRealm(realm);
        assertTrue(result.isEmpty());
        looperThread.testComplete();
    }

    private Realm getPartialRealm(SyncUser user) {
        final SyncConfiguration partialSyncConfig = configurationFactory.createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .name("partialSync")
                .modules(new PartialSyncModule())
                .build();
        return Realm.getInstance(partialSyncConfig);
    }

    private void createServerData(SyncUser user, String url) throws InterruptedException {
        final SyncConfiguration syncConfig = configurationFactory.createSyncConfigurationBuilder(user, url)
                .waitForInitialRemoteData()
                .modules(new PartialSyncModule())
                .build();

        // Create server data
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

        for (int i = 0; i < TEST_SIZE; i++) {
            realm.createObject(PartialSyncObjectB.class).setNumber(i);
        }
        realm.commitTransaction();
        SyncManager.getSession(syncConfig).uploadAllLocalChanges();
        realm.close();
   }
}
