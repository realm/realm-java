package com.tightdb;

import static org.testng.AssertJUnit.*;

import java.util.Date;

import com.tightdb.test.TestHelper;

import org.testng.annotations.Test;


public class JNIQueryTest {

    Table table;

    void init() {
        table = new Table();
        TableSpec tableSpec = new TableSpec();
        tableSpec.addColumn(ColumnType.INTEGER, "number");
        tableSpec.addColumn(ColumnType.STRING, "name");
        table.updateFromSpec(tableSpec);

        table.add(10, "A");
        table.add(11, "B");
        table.add(12, "C");
        table.add(13, "B");
        table.add(14, "D");
        table.add(16, "D");
        assertEquals(6, table.size());
    }

    @Test
    public void shouldQuery() {
        init();
        TableQuery query = table.where();

        long cnt = query.equalTo(1, "D").count();
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
    
    
    @Test
    public void testNonCompleteQuery() {
        init();

        // All the following queries are not valid, e.g contain a group but not a closing group, an or() but not a second filter etc
        try { table.where().equalTo(0,1).or().findAll();                    fail("missing a second filter"); }      catch (UnsupportedOperationException e) { }
        try { table.where().or().findAll();                                 fail("just an or()"); }                 catch (UnsupportedOperationException e) { } 
        try { table.where().group().equalTo(0,1).findAll();                 fail("messing a clsong group"); }       catch (UnsupportedOperationException e) { } 
        try { table.where().endGroup().equalTo(0,1).findAll();              fail("ends group, no start"); }         catch (UnsupportedOperationException e) { }
        try { table.where().equalTo(0,1).endGroup().findAll();              fail("ends group, no start"); }         catch (UnsupportedOperationException e) { } 

        try { table.where().equalTo(0,1).endGroup().find();                 fail("ends group, no start"); }         catch (UnsupportedOperationException e) { } 
        try { table.where().equalTo(0,1).endGroup().find(0);                fail("ends group, no start"); }         catch (UnsupportedOperationException e) { } 
        try { table.where().equalTo(0,1).endGroup().find(1);                fail("ends group, no start"); }         catch (UnsupportedOperationException e) { } 
        
        try { table.where().equalTo(0,1).endGroup().findAll(0, -1, -1);     fail("ends group, no start"); }         catch (UnsupportedOperationException e) { } 

        
        // step by step buildup
        TableQuery q = table.where().equalTo(0,1);              // valid
        q.findAll();
        q.or();                                                 // not valid
        try { q.findAll();     fail("no start group"); }         catch (UnsupportedOperationException e) { } 
        q.equalTo(0, 100);                                      // valid again
        q.findAll();
        q.equalTo(0, 200);                                      // still valid
        q.findAll();
    }

    
    @Test
    public void testNegativeColumnIndexEqualTo() {
        Table table = TestHelper.getTableWithAllColumnTypes();
        TableQuery query = table.where();
        
        // Boolean
        try { query.equalTo(-1, true).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(-10, true).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(-100, true).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        // Date
        try { query.equalTo(-1, new Date()).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(-10, new Date()).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(-100, new Date()).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        // Double
        try { query.equalTo(-1, 4.5d).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(-10, 4.5d).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(-100, 4.5d).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        
        // Float
        try { query.equalTo(-1, 1.4f).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(-10, 1.4f).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(-100, 1.4f).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        // Int / long
        try { query.equalTo(-1, 1).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(-10, 1).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(-100, 1).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        // String
        try { query.equalTo(-1, "a").findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(-10, "a").findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(-100, "a").findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        // String case true
        try { query.equalTo(-1, "a", true).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(-10, "a", true).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(-100, "a", true).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        // String case false
        try { query.equalTo(-1, "a", false).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(-10, "a", false).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.equalTo(-100, "a", false).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
    }
    
    
    @Test
    public void testNegativeColumnIndexNotEqualTo() {
        Table table = TestHelper.getTableWithAllColumnTypes();
        TableQuery query = table.where();
        
        
        // Date
        try { query.notEqualTo(-1, new Date()).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(-10, new Date()).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(-100, new Date()).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        // Double
        try { query.notEqualTo(-1, 4.5d).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(-10, 4.5d).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(-100, 4.5d).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        
        // Float
        try { query.notEqualTo(-1, 1.4f).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(-10, 1.4f).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(-100, 1.4f).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        // Int / long
        try { query.notEqualTo(-1, 1).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(-10, 1).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(-100, 1).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        // String
        try { query.notEqualTo(-1, "a").findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(-10, "a").findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(-100, "a").findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        // String case true
        try { query.notEqualTo(-1, "a", true).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(-10, "a", true).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(-100, "a", true).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}      
        
        // String case false
        try { query.notEqualTo(-1, "a", false).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(-10, "a", false).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(-100, "a", false).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
    }
    
    
    @Test
    public void testNegativeColumnIndexGreaterThan() {
        Table table = TestHelper.getTableWithAllColumnTypes();
        TableQuery query = table.where();
        
        // Date
        try { query.greaterThan(-1, new Date()).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(-10, new Date()).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(-100, new Date()).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        // Double
        try { query.greaterThan(-1, 4.5d).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(-10, 4.5d).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(-100, 4.5d).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        
        // Float
        try { query.greaterThan(-1, 1.4f).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(-10, 1.4f).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(-100, 1.4f).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        // Int / long
        try { query.greaterThan(-1, 1).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(-10, 1).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(-100, 1).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
    }
    
    
    @Test
    public void testNegativeColumnIndexGreaterThanOrEqual() {
        Table table = TestHelper.getTableWithAllColumnTypes();
        TableQuery query = table.where();
        
        // Date
        try { query.greaterThanOrEqual(-1, new Date()).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(-10, new Date()).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(-100, new Date()).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        // Double
        try { query.greaterThanOrEqual(-1, 4.5d).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(-10, 4.5d).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(-100, 4.5d).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        
        // Float
        try { query.greaterThanOrEqual(-1, 1.4f).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(-10, 1.4f).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(-100, 1.4f).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        // Int / long
        try { query.greaterThanOrEqual(-1, 1).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(-10, 1).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(-100, 1).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
    }
    
    
    @Test
    public void testNegativeColumnIndexLessThan() {
        Table table = TestHelper.getTableWithAllColumnTypes();
        TableQuery query = table.where();
        
        // Date
        try { query.lessThan(-1, new Date()).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(-10, new Date()).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(-100, new Date()).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        // Double
        try { query.lessThan(-1, 4.5d).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(-10, 4.5d).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(-100, 4.5d).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        
        // Float
        try { query.lessThan(-1, 1.4f).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(-10, 1.4f).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(-100, 1.4f).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        // Int / long
        try { query.lessThan(-1, 1).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(-10, 1).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(-100, 1).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
    }
    
    @Test
    public void testNegativeColumnIndexLessThanOrEqual() {
        Table table = TestHelper.getTableWithAllColumnTypes();
        TableQuery query = table.where();
        
        // Date
        try { query.lessThanOrEqual(-1, new Date()).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(-10, new Date()).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(-100, new Date()).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        // Double
        try { query.lessThanOrEqual(-1, 4.5d).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(-10, 4.5d).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(-100, 4.5d).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        
        // Float
        try { query.lessThanOrEqual(-1, 1.4f).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(-10, 1.4f).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(-100, 1.4f).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        // Int / long
        try { query.lessThanOrEqual(-1, 1).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(-10, 1).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(-100, 1).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
    }
    
    
    @Test
    public void testNegativeColumnIndexBetween() {
        Table table = TestHelper.getTableWithAllColumnTypes();
        TableQuery query = table.where();
        
        // Date
        try { query.between(-1, new Date(), new Date()).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.between(-10, new Date(), new Date()).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.between(-100, new Date(), new Date()).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        // Double
        try { query.between(-1, 4.5d, 6.0d).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.between(-10, 4.5d, 6.0d).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.between(-100, 4.5d, 6.0d).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        
        // Float
        try { query.between(-1, 1.4f, 5.8f).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.between(-10, 1.4f, 5.8f).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.between(-100, 1.4f, 5.8f).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        // Int / long
        try { query.between(-1, 1, 10).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.between(-10, 1, 10).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.between(-100, 1, 10).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
    }
    
    
    @Test
    public void testNegativeColumnIndexContains() {
        Table table = TestHelper.getTableWithAllColumnTypes();
        TableQuery query = table.where();
        
        // String
        try { query.contains(-1, "hey").findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.contains(-1, "hey").findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.contains(-1, "hey").findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        // String case true
        try { query.contains(-1, "hey", true).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.contains(-1, "hey", true).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.contains(-1, "hey", true).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        // String case false
        try { query.contains(-1, "hey", false).findAll(); fail("-1 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.contains(-1, "hey", false).findAll(); fail("-10 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { query.contains(-1, "hey", false).findAll(); fail("-100 column index"); } catch (ArrayIndexOutOfBoundsException e) {}
    }
    
    
    
    @Test
    public void shouldFind() {
        // Create a table
        Table table = new Table();

        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");

        // Insert some values
        table.add("Arnold", 420, false);	// 0
        table.add("Jane", 770, false);		// 1 *
        table.add("Erik", 600, false);		// 2
        table.add("Henry", 601, false);		// 3 *
        table.add("Bill", 564, true);		// 4
        table.add("Janet", 875, false);		// 5 *

        TableQuery query = table.where().greaterThan(1, 600);
        
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
    
    
    @Test
    public void queryTestForNoMatches() {
        Table t = new Table();
        t = TestHelper.getTableWithAllColumnTypes();
        
        t.add(new byte[]{1,2,3}, true, new Date(1384423149761l), 4.5d, 5.7f, 100, new Mixed("mixed"), "string", null);
        
        TableQuery q = t.where().greaterThan(5, 1000); // No matches
        
        assertEquals(-1, q.find());
        assertEquals(-1, q.find(1));
    }


    @Test
    public void queryWithWrongDataType() {

        Table table = TestHelper.getTableWithAllColumnTypes();

        // Query the table
        TableQuery query = table.where();

        // Compare strings in non string columns
        for (int i = 0; i <= 8; i++) {
            if (i != 7) {
                try { query.equalTo(i, "string");                 assert(false); } catch(IllegalArgumentException e) {}
                try { query.notEqualTo(i, "string");              assert(false); } catch(IllegalArgumentException e) {}
                try { query.beginsWith(i, "string");            assert(false); } catch(IllegalArgumentException e) {}
                try { query.endsWith(i, "string");              assert(false); } catch(IllegalArgumentException e) {}
                try { query.contains(i, "string");              assert(false); } catch(IllegalArgumentException e) {}
            }
        }

        // Compare integer in non integer columns
        for (int i = 0; i <= 8; i++) {
            if (i != 5) {
                try { query.equalTo(i, 123);                      assert(false); } catch(IllegalArgumentException e) {}
                try { query.notEqualTo(i, 123);                   assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThan(i, 123);                   assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThanOrEqual(i, 123);            assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThan(i, 123);                assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThanOrEqual(i, 123);         assert(false); } catch(IllegalArgumentException e) {}
                try { query.between(i, 123, 321);               assert(false); } catch(IllegalArgumentException e) {}
            }
        }

        // Compare float in non float columns
        for (int i = 0; i <= 8; i++) {
            if (i != 4) {
                try { query.equalTo(i, 123F);                     assert(false); } catch(IllegalArgumentException e) {}
                try { query.notEqualTo(i, 123F);                  assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThan(i, 123F);                  assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThanOrEqual(i, 123F);           assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThan(i, 123F);               assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThanOrEqual(i, 123F);        assert(false); } catch(IllegalArgumentException e) {}
                try { query.between(i, 123F, 321F);             assert(false); } catch(IllegalArgumentException e) {}
            }
        }

        // Compare double in non double columns
        for (int i = 0; i <= 8; i++) {
            if (i != 3) {
                try { query.equalTo(i, 123D);                     assert(false); } catch(IllegalArgumentException e) {}
                try { query.notEqualTo(i, 123D);                  assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThan(i, 123D);                  assert(false); } catch(IllegalArgumentException e) {}
                try { query.lessThanOrEqual(i, 123D);           assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThan(i, 123D);               assert(false); } catch(IllegalArgumentException e) {}
                try { query.greaterThanOrEqual(i, 123D);        assert(false); } catch(IllegalArgumentException e) {}
                try { query.between(i, 123D, 321D);             assert(false); } catch(IllegalArgumentException e) {}
            }
        }

        // Compare boolean in non boolean columns
        for (int i = 0; i <= 8; i++) {
            if (i != 1) {
              try { query.equalTo(i, true);                       assert(false); } catch(IllegalArgumentException e) {}
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

    @Test
    public void columnIndexOutOfBounds() {
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
        try { query.maximumDouble(1);         	assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumInt(2);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumFloat(2);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumDouble(2);           assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumInt(6);                 assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumFloat(6);            assert(false); } catch(IllegalArgumentException e) {}
        try { query.maximumDouble(6);         	assert(false); } catch(IllegalArgumentException e) {}
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
        try { query.equalTo(9, "string");                 assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(9, "string");              assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.beginsWith(9, "string");            assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.endsWith(9, "string");              assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.contains(9, "string");              assert(false); } catch(ArrayIndexOutOfBoundsException e) {}


        // Out of bounds for integer
        try { query.equalTo(9, 123);                      assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(9, 123);                   assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(9, 123);                   assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(9, 123);            assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(9, 123);                assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(9, 123);         assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.between(9, 123, 321);               assert(false); } catch(ArrayIndexOutOfBoundsException e) {}


        // Out of bounds for float
        try { query.equalTo(9, 123F);                     assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(9, 123F);                  assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(9, 123F);                  assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(9, 123F);           assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(9, 123F);               assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(9, 123F);        assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.between(9, 123F, 321F);             assert(false); } catch(ArrayIndexOutOfBoundsException e) {}


        // Out of bounds for double
        try { query.equalTo(9, 123D);                     assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.notEqualTo(9, 123D);                  assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.lessThan(9, 123D);                  assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.lessThanOrEqual(9, 123D);           assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThan(9, 123D);               assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.greaterThanOrEqual(9, 123D);        assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
        try { query.between(9, 123D, 321D);             assert(false); } catch(ArrayIndexOutOfBoundsException e) {}


        // Out of bounds for boolean
        try { query.equalTo(9, true);                       assert(false); } catch(ArrayIndexOutOfBoundsException e) {}
    }

    @Test
    public void queryOnView() {
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

        TableView view2 = view.where().equalTo(0, "John").findAll();

        assertEquals(2, view2.size());

        TableView view3 = view2.where().equalTo(1, "Anderson").findAll();

        assertEquals(1, view3.size());
    }

    @Test
    public void queryOnViewWithalreadyQueriedTable() {
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

        TableView view = table.where().equalTo(0, "John").findAll();

        TableView view2 = view.where().equalTo(1, "Anderson").findAll();

        assertEquals(1, view2.size());
    }

}
