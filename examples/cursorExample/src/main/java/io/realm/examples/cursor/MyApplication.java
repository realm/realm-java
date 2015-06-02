package io.realm.examples.cursor;

import android.app.Application;

import io.realm.Realm;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
//        Realm.deleteRealmFile(this);
        Realm realm = Realm.getInstance(this);
        PrimaryKeyFactory.initialize(realm);
        realm.close();
    }
}
