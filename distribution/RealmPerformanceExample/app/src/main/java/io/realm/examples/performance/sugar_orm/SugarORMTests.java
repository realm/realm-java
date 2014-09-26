package io.realm.examples.performance.sugar_orm;

import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.List;

import io.realm.examples.performance.PerformanceTest;
import io.realm.examples.performance.PerformanceTestException;

public class SugarORMTests extends PerformanceTest {

    public SugarORMTests() {
        testName = "SugarORM";
    }

    public void clearDatabase() throws PerformanceTestException {
        SugarEmployee.deleteAll(SugarEmployee.class);
    }

    public void testBootstrap() throws PerformanceTestException {
        SugarEmployee employee = new SugarEmployee();
        employee.setName("EmployeeBootstrap");
        employee.save();
        employee.delete();
        Select outcome = Select.from(SugarEmployee.class)
                .where(Condition.prop("name").eq("Foo0"),
                        Condition.prop("age").gt(20).lt(50),
                        Condition.prop("hired").eq(0));
        outcome.list().size();
    }

    public void testInserts() throws PerformanceTestException {
        for (int row = 0; row < getNumInserts(); row++) {
            SugarEmployee employee
                    = new SugarEmployee(getEmployeeName(row),
                    getEmployeeAge(row),
                    getEmployeeHiredStatus(row));
            employee.save();
        }

        List<SugarEmployee> list = SugarEmployee.listAll(SugarEmployee.class);
        if(list.size() < getNumInserts()) {
            throw new PerformanceTestException();
        }
    }

    public void testQueries() throws PerformanceTestException {
        Select outcome = Select.from(SugarEmployee.class)
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
    }

    private void loopResults(Select results) {
        for (Object e : results.list()) {
            SugarEmployee emp = (SugarEmployee) e;
            emp.getId();
        }
    }

    public void testCounts() throws PerformanceTestException {
        Select outcome = Select.from(SugarEmployee.class)
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
    }
}
