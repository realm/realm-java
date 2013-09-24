package com.tightdb;
   
import static org.testng.AssertJUnit.assertEquals;

import java.io.File;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;


    
public class JNITableTest {
    
    
Table t = new Table();
    
    @BeforeMethod
    void init() {       
        t.addColumn(ColumnType.ColumnTypeBinary, "binary"); // 0
        t.addColumn(ColumnType.ColumnTypeBool, "boolean");  // 1
        t.addColumn(ColumnType.ColumnTypeDate, "date");     // 2
        t.addColumn(ColumnType.ColumnTypeDouble, "double"); // 3
        t.addColumn(ColumnType.ColumnTypeFloat, "float");   // 4
        t.addColumn(ColumnType.ColumnTypeInt, "long");      // 5
        t.addColumn(ColumnType.ColumnTypeMixed, "mixed");   // 6
        t.addColumn(ColumnType.ColumnTypeString, "string"); // 7
        t.addColumn(ColumnType.ColumnTypeTable, "table");   // 8
    }
	
    @Test
    public void tableToString() {
        Table t = new Table();
        
        t.addColumn(ColumnType.ColumnTypeString, "stringCol");
        t.addColumn(ColumnType.ColumnTypeInt, "intCol");
        t.addColumn(ColumnType.ColumnTypeBool, "boolCol");
        
        t.add("s1", 1, true);
        t.add("s2", 2, false);
        
        String expected =  
"    stringCol  intCol  boolCol\n" +
"0:  s1              1     true\n" + 
"1:  s2              2    false\n" ;
        
        assertEquals(expected, t.toString());
    }
        
    
    @Test
    public void getNonExistingColumn() {
        Table t = new Table();
        t.addColumn(ColumnType.ColumnTypeInt, "int");
        
        assertEquals(-1, t.getColumnIndex("non-existing column"));
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void setDataInNonExistingRow() {
        Table t = new Table();
        t.addColumn(ColumnType.ColumnTypeString, "colName");
        t.add("String val");
        
        t.set(7, "new string val"); // Exception expected. Row 7 does not exist
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void setDataWithWrongColumnAmountParameters() {
        Table t = new Table();
        t.addColumn(ColumnType.ColumnTypeString, "colName");
        t.add("String val");
        
        t.set(0, "new string val", "This column does not exist"); // Exception expected. Table only has 1 column
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void addNegativeEmptyRows() {
        Table t = new Table();
        t.addColumn(ColumnType.ColumnTypeString, "colName");
        
        t.addEmptyRows(-1); // Argument is negative, Throws exception
    }
    
    
    @Test(expectedExceptions = NullPointerException.class)
    public void addNullInMixedColumn() {
        Table t = new Table();
        t.addColumn(ColumnType.ColumnTypeMixed, "mixed");
        t.add(new Mixed(true));
        
        t.setMixed(0, 0, null); // Argument is null, Throws exception
    }
    
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void setDataWithWrongColumnTypes() {
        Table t = new Table();
        t.addColumn(ColumnType.ColumnTypeString, "colName");
        t.add("String val");
        
        t.set(0, 100); // Exception expected. Table has string column, and here an integer is inserted
    }
    
    
    
    @Test
    public void immutableInsertNotAllowed() {
        
        String FILENAME = "only-test-file.tightdb";
        String TABLENAME = "tableName";
        
        new File(FILENAME).delete();

        SharedGroup group = new SharedGroup(FILENAME);

        // Write transaction must be run so where are sure a db exists with the correct table
        WriteTransaction wt = group.beginWrite();
        try{
            Table table = wt.getTable(TABLENAME);
            table.addColumn(ColumnType.ColumnTypeString, "col0");
            table.add("value0");
            table.add("value1");  
            table.add("value2");

            wt.commit();
        } catch (Throwable t) {
            wt.rollback();
        }

        ReadTransaction rt = group.beginRead();
        try{
            Table table = rt.getTable(TABLENAME);
            
            try {  table.insert(1, "NewValue"); fail("Exception excpeted when inserting in read transaction"); } catch (IllegalStateException e) { }
            
        } finally {
            rt.endRead();
        }
    }
    
    @Test
    public void shouldThrowWhenSetIndexOnWrongColumnType() {
        for (long colIndex = 0; colIndex < t.getColumnCount(); colIndex++) {
            
            // Check all other column types than String throws exception when using setIndex()/hasIndex()
            boolean exceptionExpected = (t.getColumnType(colIndex) != ColumnType.ColumnTypeString);

            // Try to setIndex()
            try {
                t.setIndex(colIndex);
                if (exceptionExpected)
                    fail("expected exception for colIndex " + colIndex);
            } catch (IllegalArgumentException e) {
            }

            // Try to hasIndex() for all columnTypes
            t.hasIndex(colIndex);
        }
    }
    
    
    /**
     * Returns a table with a few columns and values
     * @return
     */
    private Table getTableWithSimpleData(){
        Table table =  new Table();
        table.addColumn(ColumnType.ColumnTypeString, "col");
        table.addColumn(ColumnType.ColumnTypeInt, "int");
        table.add("val1", 100);
        table.add("val2", 200);
        table.add("val3", 300);

        return table;
    }
    
    @Test
    public void tableEqualsTest() {
        
        Table table1 = getTableWithSimpleData();
        Table table2 = getTableWithSimpleData();
        
        assertEquals(true, table1.equals(table2));
    }
    

    @Test
    public void tableNumbers() {
        Table t = new Table();
        t.addColumn(ColumnType.ColumnTypeInt, "intCol");
        t.addColumn(ColumnType.ColumnTypeDouble, "doubleCol");
        t.addColumn(ColumnType.ColumnTypeFloat, "floatCol");
        t.addColumn(ColumnType.ColumnTypeString, "StringCol");
        
        // Add 3 rows of data with same values in each column
        t.add(1, 2.0d, 3.0f, "s1");
        t.add(1, 2.0d, 3.0f, "s1");
        t.add(1, 2.0d, 3.0f, "s1");

        // Add other values
        t.add(10, 20.0d, 30.0f, "s10");
        t.add(100, 200.0d, 300.0f, "s100");
        t.add(1000, 2000.0d, 3000.0f, "s1000");

        // Count instances of values added in the first 3 rows
        assertEquals(3, t.count(0, 1));
        assertEquals(3, t.count(1, 2.0d));
        assertEquals(3, t.count(2, 3.0f));
        assertEquals(3, t.count(3, "s1"));
        
        
        assertEquals(3, t.findAllDouble(1, 2.0d).size());
        assertEquals(3, t.findAllFloat(2, 3.0f).size());
        
        
        assertEquals(3, t.findFirstDouble(1, 20.0d)); // Find rows index for first double value of 20.0 in column 1
        assertEquals(4, t.findFirstFloat(2, 300.0f)); // Find rows index for first float value of 300.0 in column 2
        
        
        // Set double and float
        t.setDouble(1, 2, -2.0d);
        t.setFloat(2, 2, -3.0f);
        
        // Get double tests
        assertEquals(-2.0d, t.getDouble(1, 2));
        assertEquals(20.0d, t.getDouble(1, 3));
        assertEquals(200.0d, t.getDouble(1, 4));
        assertEquals(2000.0d, t.getDouble(1, 5));
        
        // Get float test
        assertEquals(-3.0f, t.getFloat(2, 2));
        assertEquals(30.0f, t.getFloat(2, 3));
        assertEquals(300.0f, t.getFloat(2, 4));
        assertEquals(3000.0f, t.getFloat(2, 5));
    }
}
