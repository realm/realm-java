package io.realm.examples.performance.ormlite;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import io.realm.examples.performance.PerformanceTestFragment;

public class ORMLiteFragment extends PerformanceTestFragment {

    public static final String TAG = ORMLiteFragment.class.getName();

    public static ORMLiteFragment newInstance() {
        ORMLiteFragment fragment = new ORMLiteFragment();
        return fragment;
    }

    public ORMLiteFragment() {
        // Required empty public constructor
    }

    public String testInserts() {
        String status = "";

        final ORMLiteDatabaseHelper helper = new ORMLiteDatabaseHelper(getActivity());

        final RuntimeExceptionDao<ORMLiteEmployee, Integer> employeeDao
                = helper.getEmployeeDao();

        long startTime = System.currentTimeMillis();

        employeeDao.callBatchTasks(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                for (int row = 0; row < NUM_INSERTS; row++) {
                    ORMLiteEmployee employee = new ORMLiteEmployee();
                    employee.setName(getName(row));
                    employee.setAge(getAge(row));
                    employee.setHired(getHiredBool(row));
                    employeeDao.create(employee);
                }
                return null;
            }
        });

        status += "testInserts " + (System.currentTimeMillis() - startTime) + " ms.\n";

        //Verify writes were successful
        GenericRawResults<String[]> rawResults =
                employeeDao.queryRaw(
                        "SELECT * from Employee");
        List<String[]> results = null;
        try {
            results = rawResults.getResults();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        status += "...Completed " + results.size() + " inserts\n";
        return status;
    }

    public String testQueries() {

        final ORMLiteDatabaseHelper helper = new ORMLiteDatabaseHelper(getActivity());

        final RuntimeExceptionDao<ORMLiteEmployee, Integer> employeeDao
                = helper.getEmployeeDao();

        long startTime = System.currentTimeMillis();

        GenericRawResults<String[]> rawResults =
                employeeDao.queryRaw(
                        "SELECT * from Employee " +
                                "WHERE name = 'Foo0' " +
                                "AND age >= 20 AND age <= 50 " +
                                "AND hired = 0");

        return "testQueries " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    public String testCounts() {

        final ORMLiteDatabaseHelper helper = new ORMLiteDatabaseHelper(getActivity());

        final RuntimeExceptionDao<ORMLiteEmployee, Integer> employeeDao
                = helper.getEmployeeDao();

        long startTime = System.currentTimeMillis();

        GenericRawResults<String[]> rawResults =
                employeeDao.queryRaw(
                        "SELECT * from Employee " +
                                "WHERE name = 'Foo0' " +
                                "AND age >= 20 AND age <= 50 " +
                                "AND hired = 0");

        List<String[]> results = null;
        try {
            results = rawResults.getResults();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        results.size();

        return "testCounts " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    public String testCountsBuilder() {

        final ORMLiteDatabaseHelper helper = new ORMLiteDatabaseHelper(getActivity());

        final RuntimeExceptionDao<ORMLiteEmployee, Integer> employeeDao
                = helper.getEmployeeDao();

        long startTime = System.currentTimeMillis();

        QueryBuilder<ORMLiteEmployee, Integer> queryBuilder =
                employeeDao.queryBuilder();

        PreparedQuery<ORMLiteEmployee> preparedQuery = null;
        try {
            queryBuilder.where()
                    .eq("name", "Name")
                    .and().between("age", 500, 50000)
                    .and().eq("hired", true);
            preparedQuery = queryBuilder.prepare();
        } catch(SQLException e) {
            e.printStackTrace();
        }

        List<ORMLiteEmployee> employeeList = employeeDao.query(preparedQuery);
        employeeList.size();

        return "testCounts " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }
}
