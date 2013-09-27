package com.tightdb.typed;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.tightdb.Table;
import com.tightdb.test.TestEmployeeQuery;
import com.tightdb.test.TestEmployeeView;
import com.tightdb.test.TestQueryTableQuery;
import com.tightdb.test.TestQueryTableTable;

import java.util.Date;

public class TableQueryTest extends AbstractTest {

    @Test
    public void shouldMatchOnSimpleNumberCriteria() {
        assertEquals(1, employees.salary.equal(30000).findAll().size());
        assertEquals(1, employees.salary.eq(30000).findAll().size());

        assertEquals(2, employees.salary.notEqual(30000).findAll().size());
        assertEquals(2, employees.salary.neq(30000).findAll().size());

        assertEquals(2, employees.salary.lessThan(30000).findAll().size());
        assertEquals(2, employees.salary.lt(30000).findAll().size());

        assertEquals(3, employees.salary.lessThanOrEqual(30000).findAll()
                .size());
        assertEquals(3, employees.salary.lte(30000).findAll().size());

        assertEquals(3, employees.salary.greaterThan(5000).findAll().size());
        assertEquals(3, employees.salary.gt(5000).findAll().size());

        assertEquals(3, employees.salary.greaterThanOrEqual(10000).findAll()
                .size());
        assertEquals(3, employees.salary.gte(10000).findAll().size());

        assertEquals(2, employees.salary.between(5000, 15000).findAll().size());
    }

    @Test()
    public void shouldCalculateStatistics() {

        TestEmployeeQuery results = employees.firstName.eq("John").or().firstName.eq("Nikolche");
        assertEquals(2, results.count());

        assertEquals(10000, results.salary.minimum());
        assertEquals(10000, results.salary.minimum(0, 1, 1)); // first
        assertEquals(30000, results.salary.minimum(1, 2, 1)); // second
        assertEquals(10000, results.salary.minimum(0, Table.INFINITE, Table.INFINITE)); // both
        // TODO: Check invalid parameters

        assertEquals(30000, results.salary.maximum());
        assertEquals(10000, results.salary.maximum(0, 1, 1)); // first
        assertEquals(30000, results.salary.maximum(1, 2, 1)); // second
        assertEquals(30000, results.salary.maximum(0, Table.INFINITE, Table.INFINITE)); // both

        assertEquals(40000, results.salary.sum());
        assertEquals(10000, results.salary.sum(0, 1, 1)); // first
        assertEquals(30000, results.salary.sum(1, 2, 1)); // second
        assertEquals(40000, results.salary.sum(0, Table.INFINITE, Table.INFINITE)); // both

        assertEquals(20000.0, results.salary.average());
        assertEquals(30000.0, results.salary.average(1, 2, 1)); // second
        assertEquals(20000.0, results.salary.average(0, Table.INFINITE, Table.INFINITE)); // both
        assertEquals(10000.0, results.salary.average(0, 1, 1)); // first
    }

    @Test( expectedExceptions = ArrayIndexOutOfBoundsException.class)
    public void shouldCheckWrongParameters() {
        TestEmployeeQuery results = employees.firstName.eq("John").or().firstName.eq("Nikolche");
    //  assertEquals(2, results.count());
        assertEquals(10000, results.salary.minimum(0, 5, Table.INFINITE)); // first
    }

    @Test
    public void shouldMatchOnSimpleStringCriteria() {
        assertEquals(1, employees.firstName.eq("John").findAll().size());
        assertEquals(1, employees.firstName.equal("John").findAll().size());

        assertEquals(2, employees.firstName.neq("John").findAll().size());
        assertEquals(2, employees.firstName.notEqual("John").findAll().size());
        assertEquals(2, employees.firstName.neq("John", true).findAll().size());
        assertEquals(2, employees.firstName.notEqual("John", true).findAll().size());
        
        assertEquals(2, employees.firstName.neq("johN", false).findAll().size());
        assertEquals(2, employees.firstName.notEqual("johN", false).findAll().size());


        assertEquals(2, employees.firstName.startsWith("J").findAll().size());
        assertEquals(1, employees.firstName.endsWith("hny").findAll().size());
        assertEquals(2, employees.firstName.contains("ohn").findAll().size());

        assertEquals(1, employees.firstName.eq("john", false).findAll().size());
        assertEquals(1, employees.firstName.equal("john", false).findAll().size());
        assertEquals(2, employees.firstName.startsWith("j", false).findAll().size());
        assertEquals(1, employees.firstName.endsWith("hnY", false).findAll().size());
        assertEquals(2, employees.firstName.contains("ohN", false).findAll().size());
        
    }

    @Test
    public void shouldMatchOnSimpleBooleanCriteria() {
        assertEquals(2, employees.driver.eq(true).findAll().size());
        assertEquals(2, employees.driver.equal(true).findAll().size());

        assertEquals(1, employees.driver.neq(true).findAll().size());
        assertEquals(1, employees.driver.notEqual(true).findAll().size());
    }

    @Test
    public void shouldMatchOnCombinedAndOrCriteria() {
        TestEmployeeView nikoOrJohn = employees.firstName.startsWith("Nik").lastName
                .contains("vski").or().firstName.eq("John").findAll();

        assertEquals(2, nikoOrJohn.size());
    }

    @Test
    public void shouldMatchOnCriteriaEndingWithGroup() {
        TestEmployeeView niko = employees.where().firstName.startsWith("Nik").salary
                .eq(30000).group().lastName.contains("vski").or().firstName
                .eq("John").endGroup().findAll();

        assertEquals(1, niko.size());
    }

    @Test
    public void shouldMatchOnCriteriaBeginingWithGroup() {
        TestEmployeeView niko = employees.where().group().lastName.contains(
                "vski").or().firstName.eq("John").endGroup().firstName
                .startsWith("Nik").salary.eq(30000).findAll();

        assertEquals(1, niko.size());
    }

    @Test
    public void shouldMatchOnCriteriaHavingGroupInMiddle() {
        TestEmployeeView niko = employees.where().firstName.startsWith("Nik")
                .group().lastName.contains("vski").or().firstName.eq("John")
                .endGroup().salary.eq(30000).findAll();

        assertEquals(1, niko.size());
    }

    @Test
    public void shouldMatchMultipleQueriesWithoutInterference() {
        TestEmployeeView niko1 = employees.firstName.startsWith("Nik").group().lastName
                .contains("vski").or().firstName.eq("John").endGroup()
                .findAll();
        TestEmployeeView niko2 = employees.where().group().lastName.contains(
                "vski").or().firstName.eq("John").endGroup().firstName
                .startsWith("Nik").findAll();

        assertEquals(1, niko1.size());
        assertEquals(1, niko2.size());
    }

    @Test
    public void shouldRemoveAllMatchingRows() {
        // Remove all
        TestEmployeeQuery q = employees.where().salary.lessThan(100000000);
        // EmployeeQuery q = employees.where().firstName.neq("xy");

        assertEquals(3, q.count());

        long n = q.remove();
        assertEquals(3, n);
        assertEquals(0, employees.size());
    }

    @Test
    public void shouldRemoveSomeMatchingRows() {
        // Remove some
        TestEmployeeQuery q = employees.where().salary.lessThan(100000000);

        assertEquals(1, q.count(1, 2, Table.INFINITE));

        long n = q.remove(1, 2);
        assertEquals(1, n);
        assertEquals(2, employees.size());
    }

    @Test
    public void shouldntRemoveNonMatchingRows() {
        // Remove some
        TestEmployeeQuery q = employees.salary.lessThan(10000);

        assertEquals(0, q.count());

        long n = q.remove();
        assertEquals(0, n);
        assertEquals(3, employees.size());
    }

    @Test
    public void queryOnDates() {
        // Test equal
        assertEquals(1, employees.birthdate.equal(new Date(2222)).findAll().size());
        assertEquals(1, employees.birthdate.eq(new Date(2222)).findAll().size());

        // Test not equal
        assertEquals(2, employees.birthdate.notEqual(new Date(2222)).findAll().size());
        assertEquals(2, employees.birthdate.neq(new Date(2222)).findAll().size());

        // Test greater than
        assertEquals(2, employees.birthdate.greaterThan(new Date(2222)).findAll().size());
        assertEquals(2, employees.birthdate.gt(new Date(2222)).findAll().size());

        // Test greater than or equal
        assertEquals(2, employees.birthdate.greaterThanOrEqual(new Date(111111)).findAll().size());
        assertEquals(2, employees.birthdate.gte(new Date(111111)).findAll().size());

        // Test less than
        assertEquals(2, employees.birthdate.lessThan(new Date(333343333)).findAll().size());
        assertEquals(2, employees.birthdate.lt(new Date(333343333)).findAll().size());

        // Test less than or equal
        assertEquals(2, employees.birthdate.lessThanOrEqual(new Date(111111)).findAll().size());
        assertEquals(2, employees.birthdate.lte(new Date(111111)).findAll().size());

        // Test between
        assertEquals(1, employees.birthdate.between(new Date(3222), new Date(333342333)).findAll().size());

    }

    @Test
    public void aggregateWithLimit() {

        // SUM with limits
        assertEquals(10000, employees.salary.sum(0, Table.INFINITE, 1));
        assertEquals(40000, employees.salary.sum(0, Table.INFINITE, 2));
        assertEquals(50000, employees.salary.sum(0, Table.INFINITE, 3));

        // Average with limits
        assertEquals(10000d, employees.salary.average(0, Table.INFINITE, 1));
        assertEquals((10000d+30000d)/2, employees.salary.average(0, Table.INFINITE, 2));
        assertEquals((10000d+30000d+10000d)/3, employees.salary.average(0, Table.INFINITE, 3));

        // Maximum with limits
        assertEquals(10000, employees.salary.maximum(0, Table.INFINITE, 1));
        assertEquals(30000, employees.salary.maximum(0, Table.INFINITE, 2));
        assertEquals(30000, employees.salary.maximum(0, Table.INFINITE, 3));

        // Minimum with limits
        assertEquals(10000, employees.salary.minimum(0, Table.INFINITE, 1));
        assertEquals(10000, employees.salary.minimum(0, Table.INFINITE, 2));
        assertEquals(10000, employees.salary.minimum(0, Table.INFINITE, 3));

    }

    
    
    @Test
    public void queryNumbersTest() {
       
        TestQueryTableTable table = new TestQueryTableTable();
        table.add(10, 10f, 10d, "s10");
        table.add(20, 20f, 20d, "s20");
        table.add(20, 20f, 20d, "s20");
        table.add(100, 100f, 100d, "s100");
        table.add(1000, 1000f, 1000d, "s1000");
        
        TestQueryTableQuery query = table.where();
        
        // Average
        assertEquals(230d, query.floatNum.average() ); // average on float column returns a double
        assertEquals(230d, query.doubleNum.average() );
        
        // maximum
        assertEquals(1000f, query.floatNum.maximum() );
        assertEquals(1000d, query.doubleNum.maximum() );
        
        // minimum
        assertEquals(10f, query.floatNum.minimum() );
        assertEquals(10d, query.doubleNum.minimum() );
        
        // sum
        assertEquals(1150d, query.floatNum.sum() ); // Sum on float column returns a double
        assertEquals(1150d, query.doubleNum.sum() );
        
    }
}
