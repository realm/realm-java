package com.example.testapplication;

import android.app.Application;

import io.realm.Realm;
import io.realm.log.LogLevel;
import io.realm.log.RealmLog;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmLog.setLevel(LogLevel.INFO);
    }
}
