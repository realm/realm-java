package io.realm.examples.performance.ormlite;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import io.realm.examples.performance.PerformanceTest;
import io.realm.examples.performance.PerformanceTestException;

public class ORMLiteTests extends PerformanceTest {

    public ORMLiteTests() {
        testName = "ORMLite";
    }

    private OrmLiteDatabaseHelper helper = null;
    private RuntimeExceptionDao<OrmLiteEmployee, Integer> employeeDao = null;

    public void clearDatabase() throws PerformanceTestException {
        final OrmLiteDatabaseHelper helper = new OrmLiteDatabaseHelper(getActivity());
        helper.onUpgrade(helper.getWritableDatabase(), 2, 3);
    }

    public void testBootstrap() throws PerformanceTestException {
        helper = new OrmLiteDatabaseHelper(getActivity());
        employeeDao = helper.getEmployeeDao();

        List<String[]> rawResults = null;
        try {
            rawResults = employeeDao.queryRaw(QUERY1).getResults();
            loopResults(rawResults);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        loopResults(rawResults);
    }

    public void testInserts() throws PerformanceTestException {
        employeeDao.callBatchTasks(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                OrmLiteEmployee employee = new OrmLiteEmployee();
                for (int row = 0; row < getNumInserts(); row++) {
                    employee.setName(getEmployeeName(row));
                    employee.setAge(getEmployeeAge(row));
                    employee.setHired(getHiredBool(row));
                    employeeDao.create(employee);
                }
                return null;
            }
        });

        //Verify writes were successful
        GenericRawResults<String[]> rawResults =
                employeeDao.queryRaw(
                        "SELECT * from Employee");

        helper.close();

        //This was removed because in large data sizes sometimes there is a memory leak created.
        //        List<String[]> results = null;
        //        try {
        //            results = rawResults.getResults();
        //        } catch(SQLException e) {
        //            e.printStackTrace();
        //        }
        //        status += "...Completed " + results.size() + " inserts\n";
    }

    public void testQueries() throws PerformanceTestException {
        try {
            List<String[]> rawResults= employeeDao.queryRaw(QUERY2).getResults();
            loopResults(rawResults);
            rawResults = employeeDao.queryRaw(QUERY3).getResults();
            loopResults(rawResults);
            rawResults = employeeDao.queryRaw(QUERY4).getResults();
            loopResults(rawResults);
            rawResults = employeeDao.queryRaw(QUERY5).getResults();
            loopResults(rawResults);
            helper.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loopResults(List<String[]> results) {
        for (String[] arr : results) {
            int var = arr.length;
        }
    }

    public void testCounts() throws PerformanceTestException {
        try {
            List<String[]> rawResults = employeeDao.queryRaw(COUNT_QUERY2).getResults();
            rawResults.size();
            rawResults = employeeDao.queryRaw(COUNT_QUERY3).getResults();
            rawResults.size();
            rawResults = employeeDao.queryRaw(COUNT_QUERY4).getResults();
            rawResults.size();
            rawResults = employeeDao.queryRaw(COUNT_QUERY5).getResults();
            rawResults.size();
            helper.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //This is just an example in case you want to test the (longer) timings using the querybuilder
    public void testCountsBuilder() {
        QueryBuilder<OrmLiteEmployee, Integer> queryBuilder =
                employeeDao.queryBuilder();

        PreparedQuery<OrmLiteEmployee> preparedQuery = null;
        try {
            queryBuilder.where()
                    .eq("name", "Foo0")
                    .and().between("age", 20, 50)
                    .and().eq("hired", false);
            preparedQuery = queryBuilder.prepare();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<OrmLiteEmployee> employeeList = employeeDao.query(preparedQuery);
        employeeList.size();
    }
}
