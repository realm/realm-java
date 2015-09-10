package io.realm.instrumentation;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.entities.AllTypes;

/**
 * Created by Nabil on 09/09/15.
 */
public class ActivityLifecycle implements Lifecycle, RealmChangeListener {
    private final RealmConfiguration realmConfiguration;
    private Realm realm;
    private RealmResults<AllTypes> mAllTypes;

    public ActivityLifecycle (RealmConfiguration realmConfiguration) {
        this.realmConfiguration = realmConfiguration;
    }

    @Override
    public void onStart() {
        realm = Realm.getInstance(realmConfiguration);
        mAllTypes = realm.where(AllTypes.class).findAllAsync();
        mAllTypes.addChangeListener(this);
    }

    @Override
    public void onStop() {
        mAllTypes.removeChangeListener(this);
        realm.close();
    }

    @Override
    public void onChange() {

    }
}
