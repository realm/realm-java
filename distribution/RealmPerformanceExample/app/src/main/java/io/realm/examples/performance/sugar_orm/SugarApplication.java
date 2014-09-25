package io.realm.examples.performance.sugar_orm;

import android.content.Context;

import com.orm.SugarApp;

public class SugarApplication extends SugarApp {

    public static SugarApplication from(Context context) {
        return (SugarApplication) context.getApplicationContext();
    }
}