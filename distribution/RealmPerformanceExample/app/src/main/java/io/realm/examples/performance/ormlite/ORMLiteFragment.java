package io.realm.examples.performance.ormlite;

import io.realm.examples.performance.PerformanceTestFragment;

public class ORMLiteFragment extends PerformanceTestFragment {

    public static final String TAG = ORMLiteFragment.class.getName();

    public static ORMLiteFragment newInstance() {
        ORMLiteFragment fragment = new ORMLiteFragment();
        return fragment;
    }

    public ORMLiteFragment() {
        this.tests.add(new ORMLiteTests());
    }
}
