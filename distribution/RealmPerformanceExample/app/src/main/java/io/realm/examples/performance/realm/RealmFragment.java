package io.realm.examples.performance.realm;

import io.realm.examples.performance.PerformanceTestFragment;

public class RealmFragment extends PerformanceTestFragment {

    public static final String TAG = RealmFragment.class.getName();

    public static RealmFragment newInstance() {
        RealmFragment fragment = new RealmFragment();
        return fragment;
    }

    public RealmFragment() {
        this.tests.add(new RealmTests());
    }
}
