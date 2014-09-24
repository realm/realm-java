package io.realm.examples.performance.realm;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.List;

import io.realm.Realm;
import io.realm.examples.performance.PerformanceTestFragment;
import io.realm.examples.performance.sqlite.EmployeeDatabaseHelper;

public class RealmFragment extends PerformanceTestFragment {

    public static final String TAG = RealmFragment.class.getName();

    public static RealmFragment newInstance() {
        RealmFragment fragment = new RealmFragment();
        return fragment;
    }

    public RealmFragment() {
        // Required empty public constructor
    }

    public String testInserts() {
        Realm realm = new Realm(getActivity());

        long startTime = System.currentTimeMillis();

        realm.beginWrite();
        for(int row = 0; row < NUM_INSERTS; row++) {
            Employee employee = realm.create(Employee.class);
            employee.setName(getName(row));
            employee.setAge(getAge(row));
            employee.setHired(getHired(row));
        }
        realm.commit();

        return "testInserts " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    public String testQueries() {
        Realm realm = new Realm(getActivity());

        long startTime = System.currentTimeMillis();

        List<Employee> results
                = realm.where(Employee.class)
                .equalTo("hired", 0)
                .between("age", 20, 50)
                .equalTo("name", "Foo0").findAll();

        return "testQueries " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    public String testCounts() {
        Realm realm = new Realm(getActivity());

        long startTime = System.currentTimeMillis();

        List<Employee> results
                = realm.where(Employee.class)
                .equalTo("hired", 0)
                .between("age", 20, 50)
                .equalTo("name", "Foo0").findAll();
        results.size();

        return "testCounts " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }
}
