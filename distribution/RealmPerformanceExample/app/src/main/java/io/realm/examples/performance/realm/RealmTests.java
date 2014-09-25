package io.realm.examples.performance.realm;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.examples.performance.PerformanceTest;

public class RealmTests extends PerformanceTest {

    private Realm realm = null;

    public RealmTests() {
        testName = "Realm";
    }

    public void clearDevice() {
        realm = new Realm(getActivity());
        realm.clear();
    }

    public String testInserts() {
        realm = new Realm(getActivity());

        long startTime = System.currentTimeMillis();

        realm.beginWrite();
        for (int row = 0; row < getNumInserts(); row++) {
            RealmEmployee employee = realm.create(RealmEmployee.class);
            employee.setName(getEmployeeName(row));
            employee.setAge(getEmployeeAge(row));
            employee.setHired(getEmployeeHiredStatus(row));
        }
        realm.commit();

        long duration = (System.currentTimeMillis() - startTime);

        String status = "testInserts " + duration + " ms.";

        //Verify writes were successful
        RealmResults<RealmEmployee> results = realm.where(RealmEmployee.class).findAll();

        if(results.size() < getNumInserts()) {
            status = "Failed to complete " + getNumInserts();
        }

        //status += "...Found " + results.size() + " inserts\n";
        timings.put("testInserts", (getNumInserts() / (double)duration));

        return status;
    }

    public String testQueries() {
        realm = new Realm(getActivity());

        //Throw away first query
        List<RealmEmployee> results
                = realm.where(RealmEmployee.class)
                .equalTo("hired", 0)
                .between("age", 20, 50)
                .equalTo("name", "Foo0").findAll();
        loopResults(results);

        long startTime = System.currentTimeMillis();
        results = realm.where(RealmEmployee.class)
                .equalTo("hired", 1)
                .between("age", 20, 50)
                .equalTo("name", "Foo1").findAll();
        loopResults(results);

        results = realm.where(RealmEmployee.class)
                .equalTo("hired", 1)
                .between("age", 20, 50)
                .equalTo("name", "Foo3").findAll();
        loopResults(results);

        results = realm.where(RealmEmployee.class)
                .equalTo("hired", 0)
                .between("age", 20, 50)
                .equalTo("name", "Foo2").findAll();
        loopResults(results);

        results = realm.where(RealmEmployee.class)
                .equalTo("hired", 0)
                .between("age", 20, 50)
                .equalTo("name", "Foo330").findAll();
        loopResults(results);

        long duration = (System.currentTimeMillis() - startTime);
        timings.put("testQueries", (getNumInserts() / (double)duration));

        return "testQueries " + duration + " ms.";
    }

    private void loopResults(List<RealmEmployee> results) {
        for (RealmEmployee e : results) {
            e.getHired();
        }
    }

    public String testCounts() {
        realm = new Realm(getActivity());

        //Throw away first query
        List<RealmEmployee> results
                = realm.where(RealmEmployee.class)
                .equalTo("hired", 0)
                .between("age", 20, 50)
                .equalTo("name", "Foo0").findAll();
        //String status = "...Count Acquired: " + results.size() + " inserts\n";

        long startTime = System.currentTimeMillis();
        results = realm.where(RealmEmployee.class)
                .equalTo("hired", 1)
                .between("age", 20, 50)
                .equalTo("name", "Foo1").findAll();
        //status += "...Count Acquired: " + results.size() + " inserts\n";

        results = realm.where(RealmEmployee.class)
                .equalTo("hired", 1)
                .between("age", 20, 50)
                .equalTo("name", "Foo3").findAll();
        //status += "...Count Acquired: " + results.size() + " inserts\n";

        results = realm.where(RealmEmployee.class)
                .equalTo("hired", 0)
                .between("age", 20, 50)
                .equalTo("name", "Foo2").findAll();
        //status += "...Count Acquired: " + results.size() + " inserts\n";

        results = realm.where(RealmEmployee.class)
                .equalTo("hired", 0)
                .between("age", 20, 50)
                .equalTo("name", "Foo330").findAll();
        //status += "...Count Acquired: " + results.size() + " inserts\n";

        long duration = (System.currentTimeMillis() - startTime);
        timings.put("testCounts", (getNumInserts() / (double)duration));

        return "testCounts " + duration + " ms.";
    }

}
