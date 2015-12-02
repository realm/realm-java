package io.realm;

import android.test.AndroidTestCase;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.AllTypes;
import rx.functions.Action1;
import rx.functions.Func1;

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
        obj.observable().subscribe(new Action1<AllTypes>() {
            @Override
            public void call(AllTypes rxObject) {
                assertTrue(rxObject == obj);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
    }

    public void testAsyncRealmResultsEmittedOnSubscribe() {
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        final RealmResults<AllTypes> results = realm.where(AllTypes.class).findAllAsync();
        results.observable().subscribe(new Action1<RealmResults<AllTypes>>() {
            @Override
            public void call(RealmResults<AllTypes> rxResults) {
                assertTrue(rxResults == results);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
    }

    public void testRealmResultsEmittedOnSubscribe() {
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        final RealmResults<AllTypes> results = realm.allObjects(AllTypes.class);
        results.observable().subscribe(new Action1<RealmResults<AllTypes>>() {
            @Override
            public void call(RealmResults<AllTypes> rxResults) {
                assertTrue(rxResults == results);
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

        obj.observable().subscribe(new Action1<AllTypes>() {
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

    public void testResultsEmittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);

        realm.beginTransaction();
        RealmResults<AllTypes> results = realm.allObjects(AllTypes.class);
        realm.commitTransaction();

        results.observable().subscribe(new Action1<RealmResults<AllTypes>>() {
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
}
