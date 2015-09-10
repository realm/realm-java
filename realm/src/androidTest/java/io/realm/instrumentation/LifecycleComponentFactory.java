package io.realm.instrumentation;

import io.realm.RealmConfiguration;

/**
 * Created by Nabil on 09/09/15.
 */
public class LifecycleComponentFactory {
    public static Lifecycle newInstance(RealmConfiguration realmConfiguration) {
        return new ActivityLifecycle(realmConfiguration);
    }
}
