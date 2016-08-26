package io.realm.examples.objectserver;

import android.app.Application;

import io.realm.objectserver.util.SharedPrefsUserStore;
import io.realm.objectserver.util.UserStore;

public class MyApplication extends Application {

    public static final String APP_USER_KEY = "defaultAppUser";
    public static UserStore USER_STORE;

    @Override
    public void onCreate() {
        super.onCreate();
        USER_STORE = new SharedPrefsUserStore(this);
    }
}
