package io.realm.examples.performance.sugar_orm;

import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.List;

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

        long duration = (System.currentTimeMillis() - startTime);
        String status = "testInserts " + duration + " ms.";

        List<SugarEmployee> list = SugarEmployee.listAll(SugarEmployee.class);
        if(list.size() < getNumInserts()) {
            status = "Failed to complete " + getNumInserts();
        }

        timings.put("testInserts", (getNumInserts() / (double)duration));
        //status += "...Found " + list.size() + " inserts\n";

        return status;
    }

    public String testQueries() {

        Select outcome = Select.from(SugarEmployee.class)
                .where(Condition.prop("name").eq("Foo0"),
                        Condition.prop("age").gt(20).lt(50),
                        Condition.prop("hired").eq(0));
        loopResults(outcome);

        //Throw away the first query
        long startTime = System.currentTimeMillis();
        outcome = Select.from(SugarEmployee.class)
                .where(Condition.prop("name").eq("Foo1"),
                        Condition.prop("age").gt(20).lt(50),
                        Condition.prop("hired").eq(1));
        loopResults(outcome);

        outcome = Select.from(SugarEmployee.class)
                .where(Condition.prop("name").eq("Foo3"),
                        Condition.prop("age").gt(20).lt(50),
                        Condition.prop("hired").eq(1));
        loopResults(outcome);

        outcome = Select.from(SugarEmployee.class)
                .where(Condition.prop("name").eq("Foo2"),
                        Condition.prop("age").gt(20).lt(50),
                        Condition.prop("hired").eq(0));
        loopResults(outcome);

        outcome = Select.from(SugarEmployee.class)
                .where(Condition.prop("name").eq("Foo330"),
                        Condition.prop("age").gt(20).lt(50),
                        Condition.prop("hired").eq(0));
        loopResults(outcome);

        long duration = (System.currentTimeMillis() - startTime);
        timings.put("testQueries", (4 / (double)duration));

        return "testQueries " + duration + " ms.";
    }

    private void loopResults(Select results) {
        for (Object e : results.list()) {
            SugarEmployee emp = (SugarEmployee) e;
            emp.getId();
        }
    }

    public String testCounts() {
        Select outcome = Select.from(SugarEmployee.class)
                .where(Condition.prop("name").eq("Foo0"),
                        Condition.prop("age").gt(20).lt(50),
                        Condition.prop("hired").eq(0));
        outcome.list().size();

        //Throw away the first query
        long startTime = System.currentTimeMillis();

        outcome = Select.from(SugarEmployee.class)
                .where(Condition.prop("name").eq("Foo1"),
                        Condition.prop("age").gt(20).lt(50),
                        Condition.prop("hired").eq(1));
        outcome.list().size();

        outcome = Select.from(SugarEmployee.class)
                .where(Condition.prop("name").eq("Foo3"),
                        Condition.prop("age").gt(20).lt(50),
                        Condition.prop("hired").eq(1));
        outcome.list().size();

        outcome = Select.from(SugarEmployee.class)
                .where(Condition.prop("name").eq("Foo2"),
                        Condition.prop("age").gt(20).lt(50),
                        Condition.prop("hired").eq(0));
        outcome.list().size();

        outcome = Select.from(SugarEmployee.class)
                .where(Condition.prop("name").eq("Foo330"),
                        Condition.prop("age").gt(20).lt(50),
                        Condition.prop("hired").eq(0));
        outcome.list().size();

        long duration = (System.currentTimeMillis() - startTime);
        timings.put("testCounts", (4 / (double)duration));

        return "testCounts " + duration + " ms.";
    }
}
