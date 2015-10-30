package io.realm.examples.rxjava;

import android.app.Application;

public class MyApplication extends Application {

    private static MyApplication context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static MyApplication getContext() {
        return context;
    }
}
