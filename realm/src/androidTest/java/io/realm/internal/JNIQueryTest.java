package io.realm.internal;

import junit.framework.TestCase;

import java.util.Date;

import io.realm.internal.test.TestHelper;
import io.realm.Case;
import io.realm.Sort;

public class JNIQueryTest extends TestCase {

    Table table;

    void init() {
        table = new Table();
        table.addColumn(ColumnType.INTEGER, "number");
        table.addColumn(ColumnType.STRING, "name");

        table.add(10, "A");
        table.add(11, "B");
        table.add(12, "C");
        table.add(13, "B");
        table.add(14, "D");
        table.add(16, "D");
        assertEquals(6, table.size());
    }

    public void shouldQueryOnSortedView() {

        init();

        TableView sortedView = table.getSortedView(0, Sort.DESCENDING);

        TableView result = sortedView.where().findAll();

        assertEquals(result.size(), table.size());

    }

    public void testShouldQuery() {
        init();
        TableQuery query = table.where();

        long cnt = query.equalTo(new long[]{1}, "D").count();
        assertEquals(2, cnt);

        cnt = query.minimumInt(0);
        assertEquals(14, cnt);

        cnt = query.maximumInt(0);
        assertEquals(16, cnt);

        cnt = query.sumInt(0);
        assertEquals(14+16, cnt);

        double avg = query.averageInt(0);
        assertEquals(15.0, avg);

        // TODO: Add tests with all parameters
    }


    public void testNonCompleteQuery() {
        init();

        // All the following queries are not valid, e.g contain a group but not a closing group, an or() but not a second filter etc
        try { table.where().equalTo(new long[]{0}, 1).or().findAll();       fail("missing a second filter"); }      catch (UnsupportedOperationException e) { }
        try { table.where().or().findAll();                                 fail("just an or()"); }                 catch (UnsupportedOperationException e) { }
        try { table.where().group().equalTo(new long[]{0}, 1).findAll();    fail("messing a clsong group"); }       catch (UnsupportedOperationException e) { }
        try { table.where().endGroup().equalTo(new long[]{0}, 1).findAll(); fail("ends group, no start"); }         catch (UnsupportedOperationException e) { }
        try { table.where().equalTo(new long[]{0}, 1).endGroup().findAll(); fail("ends group, no start"); }         catch (UnsupportedOperationException e) { }

        try { table.where().equalTo(new long[]{0}, 1).endGroup().find();    fail("ends group, no start"); }         catch (UnsupportedOperationException e) { }
        try { table.where().equalTo(new long[]{0}, 1).endGroup().find(0);   fail("ends group, no start"); }         catch (UnsupportedOperationException e) { }
        try { table.where().equalTo(new long[]{0}, 1).endGroup().find(1);   fail("ends group, no start"); }         catch (UnsupportedOperationException e) { }

        try { table.where().equalTo(new long[]{0}, 1).endGroup().findAll(0, -1, -1); fail("ends group, no start"); }         catch (UnsupportedOperationException e) { }


        // step by step buildup
        TableQuery q = table.where().equalTo(new long[]{0}, 1); // valid
        q.findAll();
        q.or();                                                 // not valid
        try { q.findAll();     fail("no start group"); }         catch (UnsupportedOperationException e) { }
        q.equalTo(new long[]{0}, 100);                          // valid again
        q.findAll();
        q.equalTo(new long[]{0}, 200);                          // still valid
        q.findAll();
    }

    public void testInvalidColumnIndexEqualTo() {
        Table table = TestHelper.getTableWithAllColumnTypes();
        TableQuery query = table.where();

        // Boolean
        try { query.equalTo(new long[]{-1}, true).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(new long[]{9}, true).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(new long[]{10}, true).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}

        // Date
        try { query.equalTo(new long[]{-1}, new Date()).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(new long[]{9}, new Date()).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(new long[]{10}, new Date()).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}

        // Double
        try { query.equalTo(new long[]{-1}, 4.5d).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(new long[]{9}, 4.5d).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(new long[]{10}, 4.5d).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}


        // Float
        try { query.equalTo(new long[]{-1}, 1.4f).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(new long[]{9}, 1.4f).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(new long[]{10}, 1.4f).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}

        // Int / long
        try { query.equalTo(new long[]{-1}, 1).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(new long[]{9}, 1).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(new long[]{10}, 1).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}

        // String
        try { query.equalTo(new long[]{-1}, "a").findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(new long[]{9}, "a").findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(new long[]{10}, "a").findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}

        // String case true
        try { query.equalTo(new long[]{-1}, "a", Case.SENSITIVE).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(new long[]{9}, "a", Case.SENSITIVE).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(new long[]{10}, "a", Case.SENSITIVE).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}

        // String case false
        try { query.equalTo(new long[]{-1}, "a", Case.INSENSITIVE).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(new long[]{9}, "a", Case.INSENSITIVE).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(new long[]{10}, "a", Case.INSENSITIVE).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
    }

    public void testInvalidColumnIndexNotEqualTo() {
        Table table = TestHelper.getTableWithAllColumnTypes();
        TableQuery query = table.where();


        // Date
        try { query.notEqualTo(new long[]{-1}, new Date()).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(new long[]{9}, new Date()).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(new long[]{10}, new Date()).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}

        // Double
        try { query.notEqualTo(new long[]{-1}, 4.5d).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(new long[]{9}, 4.5d).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(new long[]{10}, 4.5d).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}


        // Float
        try { query.notEqualTo(new long[]{-1}, 1.4f).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(new long[]{9}, 1.4f).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(new long[]{10}, 1.4f).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}

        // Int / long
        try { query.notEqualTo(new long[]{-1}, 1).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(new long[]{9}, 1).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(new long[]{10}, 1).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}

        // String
        try { query.notEqualTo(new long[]{-1}, "a").findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(new long[]{9}, "a").findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(new long[]{10}, "a").findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}

        // String case true
        try { query.notEqualTo(new long[]{-1}, "a", Case.SENSITIVE).findAll(); fail("-1column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(new long[]{9}, "a", Case.SENSITIVE).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(new long[]{10}, "a", Case.SENSITIVE).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}

        // String case false
        try { query.notEqualTo(new long[]{-1}, "a", Case.INSENSITIVE).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(new long[]{9}, "a", Case.INSENSITIVE).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(new long[]{10}, "a", Case.INSENSITIVE).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
    }


    public void testInvalidColumnIndexGreaterThan() {
        Table table = TestHelper.getTableWithAllColumnTypes();
        TableQuery query = table.where();

        // Date
        try { query.greaterThan(new long[]{-1}, new Date()).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(new long[]{9}, new Date()).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(new long[]{10}, new Date()).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}

        // Double
        try { query.greaterThan(new long[]{-1}, 4.5d).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(new long[]{9}, 4.5d).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(new long[]{10}, 4.5d).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}


        // Float
        try { query.greaterThan(new long[]{-1}, 1.4f).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(new long[]{9}, 1.4f).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(new long[]{10}, 1.4f).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}

        // Int / long
        try { query.greaterThan(new long[]{-1}, 1).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(new long[]{9}, 1).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(new long[]{10}, 1).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
    }


    public void testInvalidColumnIndexGreaterThanOrEqual() {
        Table table = TestHelper.getTableWithAllColumnTypes();
        TableQuery query = table.where();

        // Date
        try { query.greaterThanOrEqual(new long[]{-1}, new Date()).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(new long[]{9}, new Date()).findAll(); fail("9 column index"); }   catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(new long[]{10}, new Date()).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}

        // Double
        try { query.greaterThanOrEqual(new long[]{-1}, 4.5d).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(new long[]{9}, 4.5d).findAll(); fail("9 column index"); }   catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(new long[]{10}, 4.5d).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}


        // Float
        try { query.greaterThanOrEqual(new long[]{-1}, 1.4f).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(new long[]{9}, 1.4f).findAll(); fail("9 column index"); }   catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(new long[]{10}, 1.4f).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}

        // Int / long
        try { query.greaterThanOrEqual(new long[]{-1}, 1).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(new long[]{9}, 1).findAll(); fail("9 column index"); }   catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(new long[]{10}, 1).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
    }


    public void testInvalidColumnIndexLessThan() {
        Table table = TestHelper.getTableWithAllColumnTypes();
        TableQuery query = table.where();

        // Date
        try { query.lessThan(new long[]{-1}, new Date()).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(new long[]{9}, new Date()).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(new long[]{10}, new Date()).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}

        // Double
        try { query.lessThan(new long[]{-1}, 4.5d).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(new long[]{9}, 4.5d).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(new long[]{10}, 4.5d).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}


        // Float
        try { query.lessThan(new long[]{-1}, 1.4f).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(new long[]{9}, 1.4f).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(new long[]{10}, 1.4f).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}

        // Int / long
        try { query.lessThan(new long[]{-1}, 1).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(new long[]{9}, 1).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(new long[]{10}, 1).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
    }

    public void testInvalidColumnIndexLessThanOrEqual() {
        Table table = TestHelper.getTableWithAllColumnTypes();
        TableQuery query = table.where();

        // Date
        try { query.lessThanOrEqual(new long[]{-1}, new Date()).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(new long[]{9}, new Date()).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(new long[]{10}, new Date()).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}

        // Double
        try { query.lessThanOrEqual(new long[]{-1}, 4.5d).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(new long[]{9}, 4.5d).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(new long[]{10}, 4.5d).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}


        // Float
        try { query.lessThanOrEqual(new long[]{-1}, 1.4f).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(new long[]{9}, 1.4f).findAll(); fail("9 column index"); }   catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(new long[]{10}, 1.4f).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}

        // Int / long
        try { query.lessThanOrEqual(new long[]{-1}, 1).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(new long[]{9}, 1).findAll(); fail("9 column index"); }   catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(new long[]{10}, 1).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
    }


    public void testInvalidColumnIndexBetween() {
        Table table = TestHelper.getTableWithAllColumnTypes();
        TableQuery query = table.where();

        // Date
        try { query.between(new long[]{-1}, new Date(), new Date()).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.between(new long[]{9}, new Date(), new Date()).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.between(new long[]{10}, new Date(), new Date()).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}

        // Double
        try { query.between(new long[]{-1}, 4.5d, 6.0d).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.between(new long[]{9}, 4.5d, 6.0d).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.between(new long[]{10}, 4.5d, 6.0d).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}


        // Float
        try { query.between(new long[]{-1}, 1.4f, 5.8f).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.between(new long[]{9}, 1.4f, 5.8f).findAll(); fail("9 column index"); }   catch (ArrayIndexOutOfBoundsException e) {}
        try { query.between(new long[]{10}, 1.4f, 5.8f).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}

        // Int / long
        try { query.between(new long[]{-1}, 1, 10).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.between(new long[]{9}, 1, 10).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.between(new long[]{10}, 1, 10).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
    }


    public void testInvalidColumnIndexContains() {
        Table table = TestHelper.getTableWithAllColumnTypes();
        TableQuery query = table.where();

        // String
        try { query.contains(new long[]{-1}, "hey").findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.contains(new long[]{9}, "hey").findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.contains(new long[]{10}, "hey").findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}

        // String case true
        try { query.contains(new long[]{-1}, "hey", Case.SENSITIVE).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.contains(new long[]{9}, "hey", Case.SENSITIVE).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.contains(new long[]{10}, "hey", Case.SENSITIVE).findAll(); fail("-0 column index"); } catch (ArrayIndexOutOfBoundsException e) {}

        // String case false
        try { query.contains(new long[]{-1}, "hey", Case.INSENSITIVE).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.contains(new long[]{9}, "hey", Case.INSENSITIVE).findAll();  fail("9 column index"); }  catch (ArrayIndexOutOfBoundsException e) {}
        try { query.contains(new long[]{10}, "hey", Case.INSENSITIVE).findAll(); fail("10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
    }

    public void testNullInputQuery() {
        Table t = new Table();
        t.addColumn(ColumnType.DATE, "dateCol");
        t.addColumn(ColumnType.STRING, "stringCol");
        
        Date nullDate = null;
        try { t.where().equalTo(new long[]{0}, nullDate);               fail("Date is null"); } catch (IllegalArgumentException e) { }
        try { t.where().notEqualTo(new long[]{0}, nullDate);            fail("Date is null"); } catch (IllegalArgumentException e) { }
        try { t.where().greaterThan(new long[]{0}, nullDate);           fail("Date is null"); } catch (IllegalArgumentException e) { }
        try { t.where().greaterThanOrEqual(new long[]{0}, nullDate);    fail("Date is null"); } catch (IllegalArgumentException e) { }
        try { t.where().lessThan(new long[]{0}, nullDate);              fail("Date is null"); } catch (IllegalArgumentException e) { }
        try { t.where().lessThanOrEqual(new long[]{0}, nullDate);       fail("Date is null"); } catch (IllegalArgumentException e) { }
        try { t.where().between(new long[]{0}, nullDate, new Date());   fail("Date is null"); } catch (IllegalArgumentException e) { }
        try { t.where().between(new long[]{0}, new Date(), nullDate);   fail("Date is null"); } catch (IllegalArgumentException e) { }
        try { t.where().between(new long[]{0}, nullDate, nullDate);     fail("Dates are null"); } catch (IllegalArgumentException e) { }
        
        String nullString = null;
        try { t.where().equalTo(new long[]{1}, nullString);                         fail("String is null"); } catch (IllegalArgumentException e) { }
        try { t.where().equalTo(new long[]{1}, nullString, Case.INSENSITIVE);       fail("String is null"); } catch (IllegalArgumentException e) { }
        try { t.where().notEqualTo(new long[]{1}, nullString);                      fail("String is null"); } catch (IllegalArgumentException e) { }
        try { t.where().notEqualTo(new long[]{1}, nullString, Case.INSENSITIVE);    fail("String is null"); } catch (IllegalArgumentException e) { }
        try { t.where().contains(new long[]{1}, nullString);                        fail("String is null"); } catch (IllegalArgumentException e) { }
        try { t.where().contains(new long[]{1}, nullString, Case.INSENSITIVE);      fail("String is null"); } catch (IllegalArgumentException e) { }
        try { t.where().beginsWith(new long[]{1}, nullString);                      fail("String is null"); } catch (IllegalArgumentException e) { }
        try { t.where().beginsWith(new long[]{1}, nullString, Case.INSENSITIVE);    fail("String is null"); } catch (IllegalArgumentException e) { }
        try { t.where().endsWith(new long[]{1}, nullString);                        fail("String is null"); } catch (IllegalArgumentException e) { }
        try { t.where().endsWith(new long[]{1}, nullString, Case.INSENSITIVE);      fail("String is null"); } catch (IllegalArgumentException e) { }
    }


    
    public void testShouldFind() {
        // Create a table
        Table table = new Table();

        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");

        // Insert some values
        table.add("Arnold", 420, false);    // 0
        table.add("Jane", 770, false);      // 1 *
        table.add("Erik", 600, false);      // 2
        table.add("Henry", 601, false);     // 3 *
        table.add("Bill", 564, true);       // 4
        table.add("Janet", 875, false);     // 5 *

        TableQuery query = table.where().greaterThan(new long[]{1}, 600);

        // find first match
        assertEquals(1, query.find());
        assertEquals(1, query.find());
        assertEquals(1, query.find(0));
        assertEquals(1, query.find(1));
        // find next
        assertEquals(3, query.find(2));
        assertEquals(3, query.find(3));
        // find next
        assertEquals(5, query.find(4));
        assertEquals(5, query.find(5));

        // test backwards
        assertEquals(5, query.find(4));
        assertEquals(3, query.find(3));
        assertEquals(3, query.find(2));
        assertEquals(1, query.find(1));
        assertEquals(1, query.find(0));

        // test out of range
        assertEquals(-1, query.find(6));
        try {  query.find(7);  fail("Exception expected");  } catch (ArrayIndexOutOfBoundsException e) {  }
    }


    
    public void testQueryTestForNoMatches() {
        Table t = new Table();
        t = TestHelper.getTableWithAllColumnTypes();

        t.add(new byte[]{1,2,3}, true, new Date(1384423149761l), 4.5d, 5.7f, 100, new Mixed("mixed"), "string", null);

        TableQuery q = t.where().greaterThan(new long[]{5}, 1000); // No matches

        assertEquals(-1, q.find());
        assertEquals(-1, q.find(1));
    }


    
    public void testQueryWithWrongDataType() {

        Table table = TestHelper.getTableWithAllColumnTypes();

        // Query the table
        TableQuery query = table.where();

        // Compare strings in non string columns
        for (int i = 0; i <= 8; i++) {
            if (i != 7) {
                try { query.equalTo(new long[]{i}, "string");                 assert(false); } catch(IllegalArgumentException e) {}
                try { query.notEqualTo(new long[]{i}, "string");              assert(false); } catch(IllegalArgumentException e) {}
                try { query.beginsWith(new long[]{i}, "string");            assert(false); } catch(IllegalArgumentException e) {}
                try { query.endsWith(new long[]{i}, "string");              assert(false); } catch(IllegalArgumentException e) {}
                try { query.contains(new long[]{i}, "string");              assert(false); } catch(IllegalArgumentException e) {}
            }
        }

        // Compare integer in non integer columns
        for (int i = 0; i <= 8; i++) {
            if (i != 5) {
                try { query.equalTo(new long[]{i}, 123);                      assert(false); } catch(IllegalArgumentException e) {}
                try { query.notEqualTo(new long[]{i}, 123);                   assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThan(new long[]{i}, 123);                     assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThanOrEqual(new long[]{i}, 123);              assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThan(new long[]{i}, 123);                  assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThanOrEqual(new long[]{i}, 123);           assert(false); } catch(IllegalArgumentException e) {}
                try { query.between(new long[]{i}, 123, 321);                 assert(false); } catch(IllegalArgumentException e) {}
            }
        }

        // Compare float in non float columns
        for (int i = 0; i <= 8; i++) {
            if (i != 4) {
                try { query.equalTo(new long[]{i}, 123F);                     assert(false); } catch(IllegalArgumentException e) {}
                try { query.notEqualTo(new long[]{i}, 123F);                  assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThan(new long[]{i}, 123F);                    assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThanOrEqual(new long[]{i}, 123F);             assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThan(new long[]{i}, 123F);                 assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThanOrEqual(new long[]{i}, 123F);          assert(false); } catch(IllegalArgumentException e) {}
                try { query.between(new long[]{i}, 123F, 321F);               assert(false); } catch(IllegalArgumentException e) {}
            }
        }

        // Compare double in non double columns
        for (int i = 0; i <= 8; i++) {
            if (i != 3) {
                try { query.equalTo(new long[]{i}, 123D);                     assert(false); } catch(IllegalArgumentException e) {}
                try { query.notEqualTo(new long[]{i}, 123D);                  assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThan(new long[]{i}, 123D);                    assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThanOrEqual(new long[]{i}, 123D);             assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThan(new long[]{i}, 123D);                 assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThanOrEqual(new long[]{i}, 123D);          assert(false); } catch(IllegalArgumentException e) {}
                try { query.between(new long[]{i}, 123D, 321D);               assert(false); } catch(IllegalArgumentException e) {}
            }
        }

        // Compare boolean in non boolean columns
        for (int i = 0; i <= 8; i++) {
            if (i != 1) {
              try { query.equalTo(new long[]{i}, true);                       assert(false); } catch(IllegalArgumentException e) {}
            }
        }

        // Compare date
        /* TODO:
        for (int i = 0; i <= 8; i++) {
            if (i != 2) {
                try { query.equal(i, new Date());                   assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThan(i, new Date());                assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThanOrEqual(i, new Date());         assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThan(i, new Date());             assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThanOrEqual(i, new Date());      assert(false); } catch(IllegalArgumentException e) {}
                try { query.between(i, new Date(), new Date());     assert(false); } catch(IllegalArgumentException e) {}
            }
        }
        */
    }

    
    public void testColumnIndexOutOfBounds() {
        Table table = TestHelper.getTableWithAllColumnTypes();

        // Query the table
        TableQuery query = table.where();

        try { query.minimumInt(0);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumFloat(0);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumDouble(0);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumInt(1);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumFloat(1);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumDouble(1);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumInt(2);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumFloat(2);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumDouble(2);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumInt(6);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumFloat(6);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumDouble(6);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumInt(7);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumFloat(7);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumDouble(7);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumInt(8);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumFloat(8);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.minimumDouble(8);           assert(false); } catch(IllegalArgumentException e) {}

        try { query.maximumInt(0);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumFloat(0);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumDouble(0);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumInt(1);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumFloat(1);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumDouble(1);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumInt(2);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumFloat(2);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumDouble(2);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumInt(6);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumFloat(6);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumDouble(6);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumInt(7);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumFloat(7);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumDouble(7);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumInt(8);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumFloat(8);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumDouble(8);           assert(false); } catch(IllegalArgumentException e) {}

        try { query.sumInt(0);                     assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumFloat(0);                assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumDouble(0);               assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumInt(1);                     assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumFloat(1);                assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumDouble(1);               assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumInt(2);                     assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumFloat(2);                assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumDouble(2);               assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumInt(6);                     assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumFloat(6);                assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumDouble(6);               assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumInt(7);                     assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumFloat(7);                assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumDouble(7);               assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumInt(8);                     assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumFloat(8);                assert(false); } catch(IllegalArgumentException e) {}
        try { query.sumDouble(8);               assert(false); } catch(IllegalArgumentException e) {}

        try { query.averageInt(0);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageFloat(0);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageDouble(0);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageInt(1);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageFloat(1);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageDouble(1);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageInt(2);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageFloat(2);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageDouble(2);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageInt(6);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageFloat(6);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageDouble(6);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageInt(7);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageFloat(7);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageDouble(7);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageInt(8);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageFloat(8);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.averageDouble(8);           assert(false); } catch(IllegalArgumentException e) {}
        // Out of bounds for string
        try { query.equalTo(new long[]{9}, "string");                 assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(new long[]{9}, "string");              assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.beginsWith(new long[]{9}, "string");            assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.endsWith(new long[]{9}, "string");              assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.contains(new long[]{9}, "string");              assert(false); } catch(ArrayIndexOutOfBoundsException e) {}


        // Out of bounds for integer
        try { query.equalTo(new long[]{9}, 123);                      assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(new long[]{9}, 123);                   assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(new long[]{9}, 123);                     assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(new long[]{9}, 123);              assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(new long[]{9}, 123);                  assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(new long[]{9}, 123);           assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.between(new long[]{9}, 123, 321);                 assert(false); } catch(ArrayIndexOutOfBoundsException e) {}


        // Out of bounds for float
        try { query.equalTo(new long[]{9}, 123F);                     assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(new long[]{9}, 123F);                  assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(new long[]{9}, 123F);                    assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(new long[]{9}, 123F);             assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(new long[]{9}, 123F);                 assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(new long[]{9}, 123F);          assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.between(new long[]{9}, 123F, 321F);               assert(false); } catch(ArrayIndexOutOfBoundsException e) {}


        // Out of bounds for double
        try { query.equalTo(new long[]{9}, 123D);                     assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(new long[]{9}, 123D);                  assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(new long[]{9}, 123D);                    assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(new long[]{9}, 123D);             assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(new long[]{9}, 123D);                 assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(new long[]{9}, 123D);          assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.between(new long[]{9}, 123D, 321D);               assert(false); } catch(ArrayIndexOutOfBoundsException e) {}


        // Out of bounds for boolean
        try { query.equalTo(new long[]{9}, true);                     assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
    }

    
    public void testQueryOnView() {
        Table table = new Table();

        // Specify the column types and names
        table.addColumn(ColumnType.STRING, "firstName");
        table.addColumn(ColumnType.STRING, "lastName");
        table.addColumn(ColumnType.INTEGER, "salary");

        // Add data to the table
        table.add("John", "Lee", 10000);
        table.add("Jane", "Lee", 15000);
        table.add("John", "Anderson", 20000);
        table.add("Erik", "Lee", 30000);
        table.add("Henry", "Anderson", 10000);

        TableView view = table.where().findAll();

        TableView view2 = view.where().equalTo(new long[]{0}, "John").findAll();

        assertEquals(2, view2.size());

        TableView view3 = view2.where().equalTo(new long[]{1}, "Anderson").findAll();

        assertEquals(1, view3.size());
    }

    
    public void testQueryOnViewWithAlreadyQueriedTable() {
        Table table = new Table();

        // Specify the column types and names
        table.addColumn(ColumnType.STRING, "firstName");
        table.addColumn(ColumnType.STRING, "lastName");
        table.addColumn(ColumnType.INTEGER, "salary");

        // Add data to the table
        table.add("John", "Lee", 10000);
        table.add("Jane", "Lee", 15000);
        table.add("John", "Anderson", 20000);
        table.add("Erik", "Lee", 30000);
        table.add("Henry", "Anderson", 10000);

        TableView view = table.where().equalTo(new long[]{0}, "John").findAll();

        TableView view2 = view.where().equalTo(new long[]{1}, "Anderson").findAll();

        assertEquals(1, view2.size());
    }


    
    public void testQueryWithSubtable() {
        Table table = new Table();
        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.TABLE, "tasks");
        table.addColumn(ColumnType.STRING, "username2");

        TableSchema tasks = table.getSubtableSchema(1);
        tasks.addColumn(ColumnType.STRING, "name");
        tasks.addColumn(ColumnType.INTEGER, "score");
        tasks.addColumn(ColumnType.BOOLEAN, "completed");

        // Insert some values
        table.add("Arnold", new Object[][] {{"task1", 120, false},
                                            {"task2", 321, false},
                                            {"task3", 78, false}}, "");
        table.add("Jane", new Object[][] {{"task2", 400, true},
                                          {"task3", 375, true}}, "");
        table.add("Erik", new Object[][] {{"task1", 562, true},
                                          {"task3", 14, false}}, "");

        // Query the table
        TableView view = table.where().subtable(1).equalTo(new long[]{2}, true).endSubtable().findAll();
        assertEquals(2, view.size());
    }

    
    public void testQueryWithUnbalancedSubtable() {
        Table table = new Table();
        table.addColumn(ColumnType.TABLE, "sub");
        
        TableSchema tasks = table.getSubtableSchema(0);
        tasks.addColumn(ColumnType.STRING, "name");
        
        try { table.where().subtable(0).count();               assert(false); } catch (UnsupportedOperationException e) {}
        try { table.where().endSubtable().count();             assert(false); } catch (UnsupportedOperationException e) {}
        try { table.where().endSubtable().subtable(0).count(); assert(false); } catch (UnsupportedOperationException e) {}
        try { table.where().subtable(0).endSubtable().count(); assert(false); } catch (UnsupportedOperationException e) {} 
    }

    
    public void testMaximumDate() {

        Table table = new Table();
        table.addColumn(ColumnType.DATE, "date");

        table.add(new Date(0));
        table.add(new Date(10000));
        table.add(new Date(1000));

        assertEquals(new Date(10000), table.where().maximumDate(0));

    }

    
    public void testMinimumDate() {

        Table table = new Table();
        table.addColumn(ColumnType.DATE, "date");

        table.add(new Date(10000));
        table.add(new Date(0));
        table.add(new Date(1000));

        assertEquals(new Date(0), table.where().minimumDate(0));

    }

}
