package io.realm.examples.performance.sugar_orm;

import com.orm.query.Condition;
import com.orm.query.Select;

import io.realm.examples.performance.PerformanceTest;

public class SugarORMTests extends PerformanceTest {

    public SugarORMTests() {
        testName = "SugarORM";
    }

    public void clearDevice() {
        SugarEmployee.deleteAll(SugarEmployee.class);
    }

    public String testInserts() {
        long startTime = System.currentTimeMillis();

        for (int row = 0; row < getNumInserts(); row++) {
            SugarEmployee employee
                    = new SugarEmployee(getEmployeeName(row),
                    getEmployeeAge(row),
                    getEmployeeHiredStatus(row));
            employee.save();
        }

        return "testInserts " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    public String testQueries() {
        long startTime = System.currentTimeMillis();

        Select outcome = Select.from(SugarEmployee.class)
                .where(Condition.prop("name").eq("Foo0"),
                        Condition.prop("age").gt(20).lt(50),
                        Condition.prop("hired").eq(1));

        return "testQueries " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }

    public String testCounts() {
        long startTime = System.currentTimeMillis();

        Select outcome = Select.from(SugarEmployee.class)
                .where(Condition.prop("name").eq("Foo0"),
                        Condition.prop("age").gt(20).lt(50),
                        Condition.prop("hired").eq(1));
        outcome.list().size();

        return "testCounts " + (System.currentTimeMillis() - startTime) + " ms.\n";
    }
}
