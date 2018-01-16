package io.realm.objectserver;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

import io.realm.DynamicRealm;
import io.realm.OrderedCollectionChangeSet;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.StandardIntegrationTest;
import io.realm.SyncConfiguration;
import io.realm.SyncManager;
import io.realm.SyncUser;
import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.entities.Dog;
import io.realm.exceptions.RealmException;
import io.realm.log.RealmLog;
import io.realm.objectserver.model.PartialSyncModule;
import io.realm.objectserver.model.PartialSyncObjectA;
import io.realm.objectserver.model.PartialSyncObjectB;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.UserFactory;
import io.realm.rule.RunTestInLooperThread;

import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class PartialSyncTests extends StandardIntegrationTest {

    @Test
    @RunTestInLooperThread
    public void invalidQuery() {
        AtomicInteger callbacks = new AtomicInteger(0);
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);
        final SyncConfiguration partialSyncConfig = configurationFactory.createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .partialRealm()
                .build();

        final Realm realm = Realm.getInstance(partialSyncConfig);
        looperThread.closeAfterTest(realm);

        // Backlinks not yet supported: https://github.com/realm/realm-core/pull/2947
        RealmResults<AllJavaTypes> query = realm.where(AllJavaTypes.class).equalTo("objectParents.fieldString", "Foo").findAllAsync();
        query.addChangeListener((results, changeSet) -> {
            if (changeSet.getState() == OrderedCollectionChangeSet.State.ERROR) {
                assertTrue(changeSet.getError() instanceof IllegalArgumentException);
                Throwable iae = changeSet.getError();
                assertTrue(iae.getMessage().contains("ERROR: realm::QueryParser: Key path resolution failed"));
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
                .partialRealm()
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

        final SyncConfiguration syncConfig = configurationFactory.createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .waitForInitialRemoteData()
                .modules(new PartialSyncModule())
                .build();

        final SyncConfiguration partialSyncConfig = configurationFactory.createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .name("partialSync")
                .modules(new PartialSyncModule())
                .partialRealm()
                .build();

        createServerData(syncConfig);

        // Download data in partial Realm
        final Realm partialSyncRealm = Realm.getInstance(partialSyncConfig);
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

        final SyncConfiguration syncConfig = configurationFactory.createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .waitForInitialRemoteData()
                .modules(new PartialSyncModule())
                .build();

        final SyncConfiguration partialSyncConfig = configurationFactory.createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .name("partialSync")
                .modules(new PartialSyncModule())
                .partialRealm()
                .build();

        createServerData(syncConfig);

        // Download data in partial Realm
        final Realm partialSyncRealm = Realm.getInstance(partialSyncConfig);
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
        final SyncConfiguration partialSyncConfig = configurationFactory.createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .name("partialSync")
                .modules(new PartialSyncModule())
                .partialRealm()
                .build();

        Realm realm = Realm.getInstance(partialSyncConfig);
        looperThread.closeAfterTest(realm);

        RealmResults<PartialSyncObjectA> results1 = realm.where(PartialSyncObjectA.class)
                .greaterThan("number", 0) // Work-around Query serializer not accepting empty query for now
                .findAllAsync("my-id");
        results1.addChangeListener((results, changeSet) -> {
            // Ignore. Just used to trigger partial sync path
        });

        RealmResults<PartialSyncObjectB> results2 = realm.where(PartialSyncObjectB.class)
                .greaterThan("number", 0) // Work-around Query serializer not accepting empty query for now
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
    @Deprecated
    @RunTestInLooperThread
    public void partialSync_oldApi() throws InterruptedException {
        SyncUser user = UserFactory.createUniqueUser(Constants.AUTH_URL);

        final SyncConfiguration syncConfig = configurationFactory.createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .waitForInitialRemoteData()
                .modules(new PartialSyncModule())
                .build();

        final SyncConfiguration partialSyncConfig = configurationFactory.createSyncConfigurationBuilder(user, Constants.SYNC_SERVER_URL)
                .name("partialSync")
                .modules(new PartialSyncModule())
                .partialRealm()
                .build();

        createServerData(syncConfig);

        AtomicInteger countdown = new AtomicInteger(2);
        final Realm partialSyncRealm = Realm.getInstance(partialSyncConfig);
        looperThread.closeAfterTest(partialSyncRealm);
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
                if (countdown.decrementAndGet() == 0) {
                    looperThread.testComplete();
                }
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
                if (countdown.decrementAndGet() == 0) {
                    looperThread.testComplete();
                }
            }
        });
    }

    private void createServerData(SyncConfiguration syncConfig) throws InterruptedException {
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

        for (int i = 0; i < 10; i++) {
            realm.createObject(PartialSyncObjectB.class).setNumber(i);
        }
        realm.commitTransaction();
        SyncManager.getSession(syncConfig).uploadAllLocalChanges();
        realm.close();
    }

}
