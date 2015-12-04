package io.realm;

import android.test.AndroidTestCase;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.AllTypes;
import rx.functions.Action1;

public class RxJavaTests extends AndroidTestCase {

    private RealmConfiguration realmConfig;
    private Realm realm;

    @Override
    protected void setUp() throws Exception {
        realmConfig = new RealmConfiguration.Builder(getContext()).build();
        Realm.deleteRealm(realmConfig);
        realm = Realm.getInstance(realmConfig);
    }

    @Override
    protected void tearDown() throws Exception {
        realm.close();
    }

    public void testObjectEmittedOnSubscribe() {
        realm.beginTransaction();
        final AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();

        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        obj.asObservable().subscribe(new Action1<AllTypes>() {
            @Override
            public void call(AllTypes rxObject) {
                assertTrue(rxObject == obj);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
    }

    public void testObjectEmittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        realm.beginTransaction();
        final AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();

        obj.asObservable().subscribe(new Action1<AllTypes>() {
            @Override
            public void call(AllTypes rxObject) {
                subscriberCalled.addAndGet(1);
            }
        });

        realm.beginTransaction();
        obj.setColumnLong(1);
        realm.commitTransaction();

        assertEquals(2, subscriberCalled.get());
    }

    public void testAsyncObjectEmittedOnSubscribe() {
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        final AllTypes asyncObj = realm.where(AllTypes.class).findFirst();
        asyncObj.asObservable().subscribe(new Action1<AllTypes>() {
            @Override
            public void call(AllTypes rxObject) {
                assertTrue(rxObject == asyncObj);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
    }

    public void testAsyncObjectEmittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        realm.beginTransaction();
        AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();
        realm.where(AllTypes.class).findFirst().asObservable().subscribe(new Action1<AllTypes>() {
            @Override
            public void call(AllTypes rxObject) {
                subscriberCalled.addAndGet(1);
            }
        });

        realm.beginTransaction();
        obj.setColumnLong(1);
        realm.commitTransaction();

        assertEquals(2, subscriberCalled.get());
    }

    public void testRealmResultsEmittedOnSubscribe() {
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        final RealmResults<AllTypes> results = realm.allObjects(AllTypes.class);
        results.asObservable().subscribe(new Action1<RealmResults<AllTypes>>() {
            @Override
            public void call(RealmResults<AllTypes> rxResults) {
                assertTrue(rxResults == results);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
    }

    public void testResultsEmittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        realm.beginTransaction();
        RealmResults<AllTypes> results = realm.allObjects(AllTypes.class);
        realm.commitTransaction();

        results.asObservable().subscribe(new Action1<RealmResults<AllTypes>>() {
            @Override
            public void call(RealmResults<AllTypes> allTypes) {
                subscriberCalled.addAndGet(1);
            }
        });

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        assertEquals(2, subscriberCalled.get());
    }

    public void testAsyncRealmResultsEmittedOnSubscribe() {
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        final RealmResults<AllTypes> results = realm.where(AllTypes.class).findAllAsync();
        results.asObservable().subscribe(new Action1<RealmResults<AllTypes>>() {
            @Override
            public void call(RealmResults<AllTypes> rxResults) {
                assertTrue(rxResults == results);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
    }

    public void testAsyncResultsEmittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        realm.allObjects(AllTypes.class).asObservable().subscribe(new Action1<RealmResults<AllTypes>>() {
            @Override
            public void call(RealmResults<AllTypes> rxResults) {
                subscriberCalled.addAndGet(1);
            }
        });

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        assertEquals(2, subscriberCalled.get());
    }

    public void testRealmEmittedOnSubscribe() {
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        realm.asObservable().subscribe(new Action1<Realm>() {
            @Override
            public void call(Realm rxRealm) {
                assertTrue(rxRealm == realm);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
    }

    public void testRealmEmittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        realm.asObservable().subscribe(new Action1<Realm>() {
            @Override
            public void call(Realm rxRealm) {
                subscriberCalled.addAndGet(1);
            }
        });

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        assertEquals(2, subscriberCalled.get());
    }

    public void testDynamicRealmEmittedOnSubscribe() {
        final DynamicRealm dynamicRealm = DynamicRealm.createInstance(realm.getConfiguration());
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        dynamicRealm.asObservable().subscribe(new Action1<DynamicRealm>() {
            @Override
            public void call(DynamicRealm rxRealm) {
                assertTrue(rxRealm == dynamicRealm);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
        dynamicRealm.close();
    }

    public void testDynamicRealmEmittedOnUpdate() {
        final DynamicRealm dynamicRealm = DynamicRealm.createInstance(realm.getConfiguration());
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        dynamicRealm.asObservable().subscribe(new Action1<DynamicRealm>() {
            @Override
            public void call(DynamicRealm rxRealm) {
                subscriberCalled.addAndGet(1);
            }
        });

        dynamicRealm.beginTransaction();
        dynamicRealm.createObject("AllTypes");
        dynamicRealm.commitTransaction();

        assertEquals(2, subscriberCalled.get());
        dynamicRealm.close();
    }

}
