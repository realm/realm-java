package io.realm.objectserver;

import android.os.SystemClock;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.realm.DynamicRealm;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.StandardIntegrationTest;
import io.realm.SyncConfiguration;
import io.realm.SyncManager;
import io.realm.SyncTestUtils;
import io.realm.SyncUser;
import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.entities.Dog;
import io.realm.log.RealmLog;
import io.realm.objectserver.model.PartialSyncModule;
import io.realm.objectserver.model.PartialSyncObjectA;
import io.realm.objectserver.model.PartialSyncObjectB;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.UserFactory;
import io.realm.rule.RunTestInLooperThread;
import io.realm.sync.Subscription;

import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
    public void namedSubscription_update() {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        final Realm realm = getPartialRealm(user);
        looperThread.closeAfterTest(realm);

        Date now = new Date();
        SystemClock.sleep(2);

        RealmQuery<PartialSyncObjectA> query1 = realm.where(PartialSyncObjectA.class).greaterThan("number", 5);
        RealmResults<PartialSyncObjectA> results = query1.findAllAsync("update-test");
        results.addChangeListener((objects1, changeSet1) -> {
            if (changeSet1.isCompleteResult()) {
                results.removeAllChangeListeners();
                final Subscription sub1 = realm.getSubscription("update-test");
                final Date firstUpdated = sub1.getUpdatedAt();
                assertEquals(query1.getDescription(), sub1.getQueryDescription());
                assertTrue(now.getTime() < sub1.getUpdatedAt().getTime());
                assertEquals(sub1.getCreatedAt(), sub1.getUpdatedAt());
                assertEquals(Long.MAX_VALUE, sub1.getExpiresAt().getTime());
                assertEquals(Long.MAX_VALUE, sub1.getTimeToLive());

                SystemClock.sleep(2);
                RealmQuery<PartialSyncObjectA> query2 = realm.where(PartialSyncObjectA.class).equalTo("string", "foo");
                RealmResults<PartialSyncObjectA> results2 = query2.findAllAsync("update-test", true);
                results2.addChangeListener((objects2, changeSet2) -> {
                    if (changeSet2.isCompleteResult()) {
                        assertEquals(query2.getDescription(), sub1.getQueryDescription());
                        assertTrue(firstUpdated.getTime() < sub1.getUpdatedAt().getTime());
                        looperThread.testComplete();
                    }
                });
                looperThread.keepStrongReference(results2);
            }
        });
        looperThread.keepStrongReference(results);
    }

    @Test
    @RunTestInLooperThread
    public void namedSubscription_update_timeToLive() {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        final Realm realm = getPartialRealm(user);
        looperThread.closeAfterTest(realm);

        RealmQuery<PartialSyncObjectA> query1 = realm.where(PartialSyncObjectA.class).greaterThan("number", 5);
        RealmResults<PartialSyncObjectA> results = query1.findAllAsync("update-test-ttl");
        results.addChangeListener((objects1, changeSet1) -> {
            if (changeSet1.isCompleteResult()) {
                results.removeAllChangeListeners();
                final Subscription sub1 = realm.getSubscription("update-test-ttl");
                final Date firstUpdatedAt = sub1.getUpdatedAt();
                final Date firstExpiresAt = sub1.getExpiresAt();
                assertEquals(Long.MAX_VALUE, sub1.getExpiresAt().getTime());
                assertEquals(Long.MAX_VALUE, sub1.getTimeToLive());

                SystemClock.sleep(2);
                RealmQuery<PartialSyncObjectA> query2 = realm.where(PartialSyncObjectA.class).equalTo("string", "foo");
                RealmResults<PartialSyncObjectA> results2 = query2.findAllAsync("update-test-ttl", 10, TimeUnit.MILLISECONDS, true);
                results2.addChangeListener((objects2, changeSet2) -> {
                    if (changeSet2.isCompleteResult()) {
                        assertEquals(10, sub1.getTimeToLive());
                        assertTrue(sub1.getExpiresAt().getTime() < firstExpiresAt.getTime());
                        assertTrue(firstUpdatedAt.getTime() < sub1.getUpdatedAt().getTime());
                        looperThread.testComplete();
                    }
                });
                looperThread.keepStrongReference(results2);
            }
        });
        looperThread.keepStrongReference(results);
    }

    @Test
    @RunTestInLooperThread
    public void namedSubscription_withTimeToLive() {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        final Realm realm = getPartialRealm(user);
        looperThread.closeAfterTest(realm);

        RealmQuery<PartialSyncObjectA> query = realm.where(PartialSyncObjectA.class);
        Date now = new Date();
        Date now_plus_10_sec = new Date(now.getTime() + 10000);
        RealmResults<PartialSyncObjectA> results = query.findAllAsync("test-ttl", 5, TimeUnit.SECONDS);
        results.addChangeListener((objects, changeSet) -> {
            if (changeSet.isCompleteResult()) {
                results.removeAllChangeListeners();
                final Subscription sub = realm.getSubscription("test-ttl");
                // Fuzzy check of expiresAt since we don't control exactly when the Subscription is created.
                assertTrue(now.getTime() <= sub.getExpiresAt().getTime());
                assertTrue(sub.getExpiresAt().getTime() < now_plus_10_sec.getTime());
                assertEquals(5000, sub.getTimeToLive());
                looperThread.testComplete();
            }
        });
        looperThread.keepStrongReference(results);
    }

    @Test
    @RunTestInLooperThread
    public void namedSubscription_update_throwsIfDifferentQueryType() {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        final Realm realm = getPartialRealm(user);
        looperThread.closeAfterTest(realm);

        RealmResults<PartialSyncObjectA> results = realm.where(PartialSyncObjectA.class).findAllAsync("type-conflict");
        results.addChangeListener((objects1, changeSet1) -> {
            if (changeSet1.isCompleteResult()) {
                results.removeAllChangeListeners();
                RealmResults<PartialSyncObjectB> results2 = realm.where(PartialSyncObjectB.class).findAllAsync("type-conflict", true);
                results2.addChangeListener((objects2, changeSet2) -> {
                    if (changeSet2.getState() == OrderedCollectionChangeSet.State.ERROR) {
                        assertTrue(changeSet2.getError() instanceof IllegalArgumentException);
                        assertTrue(changeSet2.getError().getMessage().startsWith("Replacing an existing query with a query on a different type is not allowed"));
                        looperThread.testComplete();
                    }
                });
                looperThread.keepStrongReference(results2);
            }
        });
        looperThread.keepStrongReference(results);
    }

    @RunTestInLooperThread
    public void creatingSubscriptionsAlsoCleanupExpiredSubscriptions() {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        final Realm realm = getPartialRealm(user);
        looperThread.closeAfterTest(realm);

        RealmResults<PartialSyncObjectA> results = realm.where(PartialSyncObjectA.class).findAllAsync("sub1", 0, TimeUnit.MILLISECONDS);
        results.addChangeListener((objects1, changeSet1) -> {
            if (changeSet1.isCompleteResult()) {
                results.removeAllChangeListeners();
                assertEquals(1, realm.getSubscriptions().size());
                final Subscription firstSub = realm.getSubscription("sub1");
                SystemClock.sleep(2);

                RealmResults<PartialSyncObjectB> results2 = realm.where(PartialSyncObjectB.class).findAllAsync("sub2");
                results2.addChangeListener((objects2, changeSet2) -> {
                    if (changeSet2.isCompleteResult()) {
                        assertEquals(1, realm.getSubscriptions().size());
                        assertEquals("sub2", realm.getSubscriptions().first().getName());
                        assertFalse(firstSub.isValid());
                        looperThread.testComplete();
                    }
                });
                looperThread.keepStrongReference(results2);
            }
        });
        looperThread.keepStrongReference(results);
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

    @Test
    @RunTestInLooperThread
    public void downloadLimitedData() throws InterruptedException {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        createServerData(user, Constants.SYNC_SERVER_URL);
        Realm realm = getPartialRealm(user);
        looperThread.closeAfterTest(realm);

        RealmResults<PartialSyncObjectA> results = realm.where(PartialSyncObjectA.class)
                .notEqualTo("string", "")
                .distinct("string")
                .sort("string", Sort.ASCENDING)
                .limit(2)
                .findAllAsync();
        looperThread.keepStrongReference(results);

        results.addChangeListener((objects, changeSet) -> {
            RealmLog.error(changeSet.getState().toString());
            if (changeSet.getState() == OrderedCollectionChangeSet.State.ERROR) {
                RealmLog.error(changeSet.getError().toString());
            }
            if (changeSet.isCompleteResult()) {
                assertEquals(2, results.size());
                PartialSyncObjectA obj = objects.first();
                assertEquals(6, obj.getNumber());
                assertEquals("partial", obj.getString());
                obj = objects.last();
                assertEquals(0, obj.getNumber());
                assertEquals("realm", obj.getString());
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void initialDataAndWaitForRemoteInitialData() throws InterruptedException {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        createServerData(user, Constants.SYNC_SERVER_URL);

        // Create partial Realm that will wait for the subscriptions
        final SyncConfiguration partialSyncConfig = configurationFactory.createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .name("partialSync")
                .initialData(r -> {
                    r.where(PartialSyncObjectA.class).greaterThan("number", 5).subscribe("my-sub");
                })
                .waitForInitialRemoteData()
                .modules(new PartialSyncModule())
                .build();
        Realm realm = Realm.getInstance(partialSyncConfig);
        looperThread.closeAfterTest(realm);

        // Check the state of subscriptions. Sync automatically creates subscriptions for fine-grained permission classes.
        assertEquals(6, realm.getSubscriptions().size());
        assertTrue(realm.getSubscriptions().where().equalTo("status", 0).findAll().isEmpty());
        Subscription sub = realm.getSubscription("my-sub");
        assertEquals(Subscription.State.ACTIVE, sub.getState());

        // Check that data is downloaded
        assertFalse(realm.isEmpty());
        assertEquals(4, realm.where(PartialSyncObjectA.class).findAll().size());
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void unsubscribe_synchronous() throws InterruptedException {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        createServerData(user, Constants.SYNC_SERVER_URL);

        // Create partial Realm that will wait for the subscriptions
        final SyncConfiguration partialSyncConfig = configurationFactory.createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .name("partialSync")
                .initialData(r -> {
                    r.where(PartialSyncObjectA.class).greaterThan("number", 5).subscribe("my-sub");
                })
                .waitForInitialRemoteData()
                .addModule(new PartialSyncModule())
                .build();
        Realm realm = Realm.getInstance(partialSyncConfig);
        looperThread.closeAfterTest(realm);

        realm.executeTransaction(r -> {
            Subscription sub = r.getSubscription("my-sub");
            assertEquals(Subscription.State.ACTIVE, sub.getState());
            sub.unsubscribe();
            assertEquals(Subscription.State.INVALIDATED, sub.getState());
        });

        // Objects should eventually disappear from the device
        RealmResults<PartialSyncObjectA> results = realm.where(PartialSyncObjectA.class).findAll();
        results.addChangeListener((objects, changeSet) -> {
            if (objects.isEmpty()) {
                looperThread.testComplete();
            }
        });
    }


    @Test
    @RunTestInLooperThread
    public void deletingSubscriptionObjectUnsubscribes() throws InterruptedException {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        createServerData(user, Constants.SYNC_SERVER_URL);

        // Create partial Realm that will wait for the subscriptions
        final SyncConfiguration partialSyncConfig = configurationFactory.createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .name("partialSync")
                .initialData(r -> {
                    r.where(PartialSyncObjectA.class).greaterThan("number", 5).subscribe("my-sub");
                })
                .waitForInitialRemoteData()
                .addModule(new PartialSyncModule())
                .build();
        Realm realm = Realm.getInstance(partialSyncConfig);
        looperThread.closeAfterTest(realm);

        realm.executeTransaction(r -> {
            Subscription sub = r.getSubscription("my-sub");
            assertEquals(Subscription.State.ACTIVE, sub.getState());
            sub.deleteFromRealm(); // Equivalent of calling `sub.unsubscribe()`.
            assertEquals(Subscription.State.INVALIDATED, sub.getState());
        });

        // Objects should eventually disappear from the device
        RealmResults<PartialSyncObjectA> results = realm.where(PartialSyncObjectA.class).findAll();
        results.addChangeListener((objects, changeSet) -> {
            if (objects.isEmpty()) {
                looperThread.testComplete();
            }
        });
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
