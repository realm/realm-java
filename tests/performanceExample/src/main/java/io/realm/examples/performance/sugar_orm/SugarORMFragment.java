package io.realm.examples.performance.sugar_orm;

import io.realm.examples.performance.PerformanceTestFragment;

public class SugarORMFragment extends PerformanceTestFragment {

    public static final String TAG = SugarORMFragment.class.getName();

    public static SugarORMFragment newInstance() {
        SugarORMFragment fragment = new SugarORMFragment();
        return fragment;
    }

    public SugarORMFragment() {
        this.tests.add(new SugarORMTests());
    }

}
