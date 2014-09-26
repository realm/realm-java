package io.realm.examples.performance.greendao;

import android.database.sqlite.SQLiteDatabase;
import android.widget.EditText;

import io.realm.examples.performance.PerformanceTestFragment;

public class GreenDAOFragment extends PerformanceTestFragment {

    public static final String TAG = GreenDAOFragment.class.getName();

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private EmployeeDao employeeDao;

    public static GreenDAOFragment newInstance() {
        GreenDAOFragment fragment = new GreenDAOFragment();
        return fragment;
    }

    public GreenDAOFragment() {
        this.tests.add(new GreenDAOTests());
    }
}
