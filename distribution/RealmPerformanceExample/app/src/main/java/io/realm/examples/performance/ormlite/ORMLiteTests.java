package io.realm.examples.performance.ormlite;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import io.realm.examples.performance.PerformanceTest;

public class ORMLiteTests extends PerformanceTest {

    public ORMLiteTests() {
        testName = "ORMLite";
    }

    public void clearDevice() {
        final OrmLiteDatabaseHelper helper = new OrmLiteDatabaseHelper(getActivity());
        helper.onUpgrade(helper.getWritableDatabase(), 2, 3);
    }

    public String testInserts() {
        String status = "";

        final OrmLiteDatabaseHelper helper = new OrmLiteDatabaseHelper(getActivity());

        final RuntimeExceptionDao<OrmLiteEmployee, Integer> employeeDao
                = helper.getEmployeeDao();

        long startTime = System.currentTimeMillis();

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

        long duration = (System.currentTimeMillis() - startTime);
        status += "testInserts " + duration + " ms.";

        //Verify writes were successful
        GenericRawResults<String[]> rawResults =
                employeeDao.queryRaw(
                        "SELECT * from Employee");

        timings.put("testInserts", (getNumInserts() / (double)duration));

//This was removed because in large data sizes sometimes there is a memory leak created.
//        List<String[]> results = null;
//        try {
//            results = rawResults.getResults();
//        } catch(SQLException e) {
//            e.printStackTrace();
//        }
//        status += "...Completed " + results.size() + " inserts\n";

        return status;
    }

    public String testQueries() {

        final OrmLiteDatabaseHelper helper = new OrmLiteDatabaseHelper(getActivity());

        final RuntimeExceptionDao<OrmLiteEmployee, Integer> employeeDao
                = helper.getEmployeeDao();

        long startTime = -1;

        try {
            //Throw away first query
            List<String[]> rawResults = employeeDao.queryRaw(QUERY1).getResults();
            loopResults(rawResults);

            startTime = System.currentTimeMillis();

            rawResults = employeeDao.queryRaw(QUERY2).getResults();
            loopResults(rawResults);

            rawResults = employeeDao.queryRaw(QUERY3).getResults();
            loopResults(rawResults);

            rawResults = employeeDao.queryRaw(QUERY4).getResults();
            loopResults(rawResults);

            rawResults = employeeDao.queryRaw(QUERY5).getResults();
            loopResults(rawResults);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        long duration = (System.currentTimeMillis() - startTime);
        timings.put("testQueries", (getNumInserts() / (double)duration));

        return "testQueries " + duration + " ms.";
    }

    private void loopResults(List<String[]> results) {
        for (String[] arr : results) {
            int var = arr.length;
        }
    }

    public String testCounts() {

        final OrmLiteDatabaseHelper helper = new OrmLiteDatabaseHelper(getActivity());

        final RuntimeExceptionDao<OrmLiteEmployee, Integer> employeeDao
                = helper.getEmployeeDao();

        long startTime = -1;
        String status = "";

        try {
            //Throw away first query
            List<String[]> rawResults = employeeDao.queryRaw(COUNT_QUERY1).getResults();
            //status += "...Count Acquired: " + rawResults.size() + " inserts\n";

            startTime = System.currentTimeMillis();

            rawResults = employeeDao.queryRaw(COUNT_QUERY2).getResults();
            //status += "...Count Acquired: " + rawResults.size() + " inserts\n";

            rawResults = employeeDao.queryRaw(COUNT_QUERY3).getResults();
            //status += "...Count Acquired: " + rawResults.size() + " inserts\n";

            rawResults = employeeDao.queryRaw(COUNT_QUERY4).getResults();
            //status += "...Count Acquired: " + rawResults.size() + " inserts\n";

            rawResults = employeeDao.queryRaw(COUNT_QUERY5).getResults();
            //status += "...Count Acquired: " + rawResults.size() + " inserts\n";

        } catch (SQLException e) {
            e.printStackTrace();
        }

        long duration = (System.currentTimeMillis() - startTime);
        timings.put("testCounts", (getNumInserts() / (double)duration));

        status += "testCounts " + duration + " ms.";
        return status;
    }

    //This is just an example in case you want to test the (longer) timings using the querybuilder
    public String testCountsBuilder() {

        final OrmLiteDatabaseHelper helper = new OrmLiteDatabaseHelper(getActivity());

        final RuntimeExceptionDao<OrmLiteEmployee, Integer> employeeDao
                = helper.getEmployeeDao();

        long startTime = System.currentTimeMillis();

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

        return "testCounts " + (System.currentTimeMillis() - startTime) + " ms.";
    }
}
