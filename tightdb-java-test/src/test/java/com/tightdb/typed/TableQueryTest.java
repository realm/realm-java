package com.tightdb.typed;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.tightdb.Table;
import com.tightdb.test.TestEmployeeQuery;
import com.tightdb.test.TestEmployeeView;
import com.tightdb.test.TestQueryTableQuery;
import com.tightdb.test.TestQueryTableTable;
import com.tightdb.test.TestQueryTableView;
import com.tightdb.test.TestQueryTableRow;

import java.util.Date;

public class TableQueryTest extends AbstractTest {

    @Test
    public void shouldMatchOnSimpleNumberCriteria() {
        assertEquals(1, employees.salary.equalTo(30000).findAll().size());

        assertEquals(2, employees.salary.notEqualTo(30000).findAll().size());

        assertEquals(2, employees.salary.lessThan(30000).findAll().size());

        assertEquals(3, employees.salary.lessThanOrEqual(30000).findAll()
                .size());

        assertEquals(3, employees.salary.greaterThan(5000).findAll().size());

        assertEquals(3, employees.salary.greaterThanOrEqual(10000).findAll()
                .size());

        assertEquals(2, employees.salary.between(5000, 15000).findAll().size());
    }

    @Test()
    public void shouldCalculateStatistics() {

        TestEmployeeQuery results = employees.firstName.equalTo("John").or().firstName.equalTo("Nikolche");
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
        TestEmployeeQuery results = employees.firstName.equalTo("John").or().firstName.equalTo("Nikolche");
    //  assertEquals(2, results.count());
        assertEquals(10000, results.salary.minimum(0, 5, Table.INFINITE)); // first
    }

    @Test
    public void shouldMatchOnSimpleStringCriteria() {
        assertEquals(1, employees.firstName.equalTo("John").findAll().size());
        assertEquals(1, employees.firstName.equalTo("John").findAll().size());


        assertEquals(2, employees.firstName.notEqualTo("John").findAll().size());
        assertEquals(2, employees.firstName.notEqualTo("John").findAll().size());


        assertEquals(2, employees.firstName.startsWith("J").findAll().size());
        assertEquals(1, employees.firstName.endsWith("hny").findAll().size());
        assertEquals(2, employees.firstName.contains("ohn").findAll().size());

        assertEquals(1, employees.firstName.equalTo("john", false).findAll().size());
        assertEquals(1, employees.firstName.equalTo("john", false).findAll().size());
        assertEquals(2, employees.firstName.startsWith("j", false).findAll().size());
        assertEquals(1, employees.firstName.endsWith("hnY", false).findAll().size());
        assertEquals(2, employees.firstName.contains("ohN", false).findAll().size());
        
    }

    @Test
    public void shouldMatchOnSimpleBooleanCriteria() {
        assertEquals(2, employees.driver.equalTo(true).findAll().size());
        assertEquals(2, employees.driver.equalTo(true).findAll().size());

        assertEquals(1, employees.driver.notEqual(true).findAll().size());
        assertEquals(1, employees.driver.notEqual(true).findAll().size());
    }

    @Test
    public void shouldMatchOnCombinedAndOrCriteria() {
        TestEmployeeView nikoOrJohn = employees.firstName.startsWith("Nik").lastName
                .contains("vski").or().firstName.equalTo("John").findAll();

        assertEquals(2, nikoOrJohn.size());
    }

    @Test
    public void shouldMatchOnCriteriaEndingWithGroup() {
        TestEmployeeView niko = employees.where().firstName.startsWith("Nik").salary
                .equalTo(30000).group().lastName.contains("vski").or().firstName
                .equalTo("John").endGroup().findAll();

        assertEquals(1, niko.size());
    }

    @Test
    public void shouldMatchOnCriteriaBeginingWithGroup() {
        TestEmployeeView niko = employees.where().group().lastName.contains(
                "vski").or().firstName.equalTo("John").endGroup().firstName
                .startsWith("Nik").salary.equalTo(30000).findAll();

        assertEquals(1, niko.size());
    }

    @Test
    public void shouldMatchOnCriteriaHavingGroupInMiddle() {
        TestEmployeeView niko = employees.where().firstName.startsWith("Nik")
                .group().lastName.contains("vski").or().firstName.equalTo("John")
                .endGroup().salary.equalTo(30000).findAll();

        assertEquals(1, niko.size());
    }

    @Test
    public void shouldMatchMultipleQueriesWithoutInterference() {
        TestEmployeeView niko1 = employees.firstName.startsWith("Nik").group().lastName
                .contains("vski").or().firstName.equalTo("John").endGroup()
                .findAll();
        TestEmployeeView niko2 = employees.where().group().lastName.contains(
                "vski").or().firstName.equalTo("John").endGroup().firstName
                .startsWith("Nik").findAll();

        assertEquals(1, niko1.size());
        assertEquals(1, niko2.size());
    }

    @Test
    public void shouldRemoveAllMatchingRows() {
        // Remove all
        TestEmployeeQuery q = employees.where().salary.lessThan(100000000);
        // EmployeeQuery q = employees.where().firstName.notEqual("xy");

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
        assertEquals(1, employees.birthdate.equalTo(new Date(2222)).findAll().size());
        assertEquals(1, employees.birthdate.equalTo(new Date(2222)).findAll().size());

        // Test not equal
        assertEquals(2, employees.birthdate.notEqualTo(new Date(2222)).findAll().size());
        assertEquals(2, employees.birthdate.notEqualTo(new Date(2222)).findAll().size());

        // Test greater than
        assertEquals(2, employees.birthdate.greaterThan(new Date(2222)).findAll().size());

        // Test greater than or equal
        assertEquals(2, employees.birthdate.greaterThanOrEqual(new Date(111111)).findAll().size());

        // Test less than
        assertEquals(2, employees.birthdate.lessThan(new Date(333343333)).findAll().size());

        // Test less than or equal
        assertEquals(2, employees.birthdate.lessThanOrEqual(new Date(111111)).findAll().size());

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

    @Test
    public void queryOnView() {
       
        TestQueryTableTable table = new TestQueryTableTable();
        table.add(10, 10f, 10d, "s10");
        table.add(20, 20f, 20d, "s20");
        table.add(20, 20f, 20d, "s20");
        table.add(100, 100f, 100d, "s100");
        table.add(1000, 1000f, 1000d, "s1000");

        TestQueryTableView view = table.where().findAll();
        
        // Average
        assertEquals(230d, view.floatNum.average() ); // average on float column returns a double
        assertEquals(230d, view.doubleNum.average() );
        
        // maximum
        assertEquals(1000f, view.floatNum.maximum() );
        assertEquals(1000d, view.doubleNum.maximum() );
        
        // minimum
        assertEquals(10f, view.floatNum.minimum() );
        assertEquals(10d, view.doubleNum.minimum() );
        
        // sum
        assertEquals(1150d, view.floatNum.sum() ); // Sum on float column returns a double
        assertEquals(1150d, view.doubleNum.sum() );
        
    }

    @Test
    public void queryFindFirst() {

        TestQueryTableTable table = new TestQueryTableTable();
        table.add(10, 10f, 10d, "s10");
        table.add(20, 20f, 20d, "s20");
        table.add(20, 20f, 20d, "s20");
        table.add(100, 100f, 100d, "s100");
        table.add(1000, 1000f, 1000d, "s1000");

        TestQueryTableRow res = table.where().longNum.equalTo(9).findFirst();
        assertEquals(null, res ); // no match found

        TestQueryTableRow res2 = table.where().longNum.equalTo(100).findFirst();
        assertEquals("s100", res2.getStringVal() );
    }

    @Test
    public void queryFindFrom() {

        TestQueryTableTable table = new TestQueryTableTable();
        table.add(10, 10f, 10d, "s10");
        table.add(20, 20f, 20d, "s20");
        table.add(20, 20f, 20d, "s20");
        table.add(100, 100f, 100d, "s100");
        table.add(1000, 1000f, 1000d, "s1000");

        TestQueryTableRow res1 = table.where().longNum.equalTo(20).findFirst();
        assertEquals(1, res1.getPosition() );

        TestQueryTableRow res2 = table.where().longNum.equalTo(20).findFrom(res1.getPosition()+1);
        assertEquals(2, res2.getPosition() );

        TestQueryTableRow res3 = table.where().doubleNum.equalTo(1000d).findFrom(res2.getPosition()+1);
        assertEquals(4, res3.getPosition() );

        TestQueryTableRow res4 = table.where().doubleNum.equalTo(1000d).findFrom(res3.getPosition()+1);
        assertEquals(null, res4);
    }

    @Test
    public void queryAggregates() {

        TestQueryTableTable table = new TestQueryTableTable();
        table.add(10, 10f, 10d, "s10");
        table.add(20, 20f, 20d, "s20");
        table.add(20, 20f, 20d, "s20");
        table.add(100, 100f, 100d, "s100");
        table.add(1000, 1000f, 1000d, "s1000");

        long res1 = table.where().longNum.greaterThan(10).count();
        assertEquals(4, res1 );

        long res2 = table.where().longNum.greaterThan(10).longNum.sum();
        assertEquals(1140, res2 );

        long res3 = table.where().longNum.greaterThan(10).longNum.maximum();
        assertEquals(1000, res3 );

        long res4 = table.where().longNum.greaterThan(10).longNum.minimum();
        assertEquals(20, res4 );

        double res5 = table.where().longNum.greaterThan(10).longNum.average();
        assertEquals(285d, res5 );
    }

}
