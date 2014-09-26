package io.realm.examples.performance.realm;

import java.io.File;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.examples.performance.PerformanceTest;
import io.realm.examples.performance.PerformanceTestException;

public class RealmTests extends PerformanceTest {

    private Realm realm = null;

    public RealmTests() {
        testName = "Realm";
    }

    public void clearDatabase() throws PerformanceTestException {
        realm = Realm.getInstance(getActivity());

        //Clear the Realm...
        File file = new File("files/default.realm");
        file.delete();
        file = new File("files/default.realm.lock");
        file.delete();
    }

    public void testBootstrap() throws PerformanceTestException {
        realm = Realm.getInstance(getActivity());
        realm.beginTransaction();
        realm.createObject(RealmEmployee.class);
        realm.commitTransaction();
        //Throw away first query
        List<RealmEmployee> results
                = realm.where(RealmEmployee.class)
                .equalTo("hired", 0)
                .between("age", 20, 50)
                .equalTo("name", "Foo0").findAll();
    }

    public void testInserts() throws PerformanceTestException {
        realm.beginTransaction();
        for (int row = 0; row < getNumInserts(); row++) {
            RealmEmployee employee = realm.createObject(RealmEmployee.class);
            employee.setName(getEmployeeName(row));
            employee.setAge(getEmployeeAge(row));
            employee.setHired(getEmployeeHiredStatus(row));
        }
        realm.commitTransaction();

        //Verify writes were successful
        RealmResults<RealmEmployee> results = realm.where(RealmEmployee.class).findAll();

        if(results.size() < getNumInserts()) {
            throw new PerformanceTestException();
        }
    }

    public void testQueries() throws PerformanceTestException {
        List<RealmEmployee> results
                = realm.where(RealmEmployee.class)
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
    }

    private void loopResults(List<RealmEmployee> results) {
        for (RealmEmployee e : results) {
            e.getHired();
        }
    }

    public void testCounts() throws PerformanceTestException {
        List<RealmEmployee> results
                = realm.where(RealmEmployee.class)
                .equalTo("hired", 1)
                .between("age", 20, 50)
                .equalTo("name", "Foo1").findAll();
        results.size();
        results = realm.where(RealmEmployee.class)
                .equalTo("hired", 1)
                .between("age", 20, 50)
                .equalTo("name", "Foo3").findAll();
        results.size();
        results = realm.where(RealmEmployee.class)
                .equalTo("hired", 0)
                .between("age", 20, 50)
                .equalTo("name", "Foo2").findAll();
        results.size();
        results = realm.where(RealmEmployee.class)
                .equalTo("hired", 0)
                .between("age", 20, 50)
                .equalTo("name", "Foo330").findAll();
        results.size();
    }

}
