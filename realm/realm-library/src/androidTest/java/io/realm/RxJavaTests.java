package io.realm;

import android.test.AndroidTestCase;

import java.util.concurrent.atomic.AtomicBoolean;

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
        obj.observable().subscribe(new Action1<AllTypes>() {
            @Override
            public void call(AllTypes rxObject) {
                assertTrue(rxObject == obj);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
    }
}
