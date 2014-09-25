package io.realm.examples.performance.sqlite;

import io.realm.examples.performance.PerformanceTestFragment;
import io.realm.examples.performance.realm.RealmTests;

public class SQLiteFragment extends PerformanceTestFragment {

    public static final String TAG = SQLiteFragment.class.getName();

    private EmployeeDatabaseHelper databaseHelper = null;

    public static SQLiteFragment newInstance() {
        SQLiteFragment fragment = new SQLiteFragment();
        return fragment;
    }

    public SQLiteFragment() {
        this.tests.add(new SQLiteTests());
    }
}
