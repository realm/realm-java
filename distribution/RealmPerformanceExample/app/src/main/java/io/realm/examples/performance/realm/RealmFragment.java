package io.realm.examples.performance.realm;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.examples.performance.PerformanceTestFragment;

public class RealmFragment extends PerformanceTestFragment {

    public static final String TAG = RealmFragment.class.getName();

    public static RealmFragment newInstance() {
        RealmFragment fragment = new RealmFragment();
        return fragment;
    }

    private Realm realm = null;

    public RealmFragment() {
        // Required empty public constructor
    }

    public void clearDevice() {
        realm = new Realm(getActivity());
        realm.clear();
    }

    public String testInserts() {
        realm = new Realm(getActivity());

        long startTime = System.currentTimeMillis();

        realm.beginWrite();
        for(int row = 0; row < getNumInserts(); row++) {
            RealmEmployee employee = realm.create(RealmEmployee.class);
            employee.setName(getName(row));
            employee.setAge(getAge(row));
            employee.setHired(getHired(row));
        }
        realm.commit();

        String status = "testInserts " + (System.currentTimeMillis() - startTime) + " ms.\n";

        //Verify writes were successful
        RealmResults<RealmEmployee> results = realm.where(RealmEmployee.class).findAll();

        status += "...Completed " + results.size() + " inserts\n";
        return status;
    }

    public String testQueries() {
        realm = new Realm(getActivity());

        long startTime = System.currentTimeMillis();

        List<RealmEmployee> results
                = realm.where(RealmEmployee.class)
                .equalTo("hired", 0)
                .between("age", 20, 50)
                .equalTo("name", "Foo0").findAll();

//        results = realm.where(RealmEmployee.class)
//                .equalTo("hired", 1)
//                .between("age", 20, 40)
//                .equalTo("name", "Foo0").findAll();
//
//        results = realm.where(RealmEmployee.class)
//                .equalTo("hired", 1)
//                .between("age", 20, 40)
//                .equalTo("name", "Foo1").findAll();

        return "testQueries " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    public String testCounts() {
        realm = new Realm(getActivity());

        long startTime = System.currentTimeMillis();

        List<RealmEmployee> results
                = realm.where(RealmEmployee.class)
                .equalTo("hired", 0)
                .between("age", 20, 50)
                .equalTo("name", "Foo0").findAll();
        results.size();

//        results = realm.where(RealmEmployee.class)
//                .equalTo("hired", 1)
//                .between("age", 20, 40)
//                .equalTo("name", "Foo0").findAll();
//        results.size();
//
//        results = realm.where(RealmEmployee.class)
//                .equalTo("hired", 1)
//                .between("age", 20, 40)
//                .equalTo("name", "Foo1").findAll();
//        results.size();

        return "testCounts " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }
}
