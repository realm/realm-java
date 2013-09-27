package com.tightdb;
   
import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.nio.ByteBuffer;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

public class JNITableTest {
    
    Table t = new Table();

    Table createTestTable() {
        Table t = new Table();
        t.addColumn(ColumnType.BINARY, "binary"); // 0
        t.addColumn(ColumnType.BOOLEAN, "boolean");  // 1
        t.addColumn(ColumnType.DATE, "date");     // 2
        t.addColumn(ColumnType.DOUBLE, "double"); // 3
        t.addColumn(ColumnType.FLOAT, "float");   // 4
        t.addColumn(ColumnType.INTEGER, "long");      // 5
        t.addColumn(ColumnType.MIXED, "mixed");   // 6
        t.addColumn(ColumnType.STRING, "string"); // 7
        t.addColumn(ColumnType.TABLE, "table");   // 8
        return t;
    }
    
    @BeforeMethod
    void init() {       
        t = createTestTable(); 
    }
	
    @Test
    public void tableToString() {
        Table t = new Table();
        
        t.addColumn(ColumnType.STRING, "stringCol");
        t.addColumn(ColumnType.INTEGER, "intCol");
        t.addColumn(ColumnType.BOOLEAN, "boolCol");
        
        t.add("s1", 1, true);
        t.add("s2", 2, false);
        
        String expected =  
"    stringCol  intCol  boolCol\n" +
"0:  s1              1     true\n" + 
"1:  s2              2    false\n" ;
        
        assertEquals(expected, t.toString());
    }

    @Test 
    public void testGroupEquals() {
        Table t2 = createTestTable();
        assertEquals(true, t.equals(t2));
        t.addEmptyRow();
        assertEquals(false, t.equals(t2));
    }
    
    
    @Test
    public void tableBinaryTest() {
        Table t = new Table();
        t.addColumn(ColumnType.BINARY, "binary");
        
        byte[] row0 = new byte[] { 1, 2, 3 };
        byte[] row1 = new byte[] { 10, 20, 30 };
        
        t.getInternalMethods().insertBinary(0, 0, row0);
        t.getInternalMethods().insertBinary(0, 1, row1);
        t.getInternalMethods().insertDone();
        
        byte[] nullByte = null;
        
        try { t.getInternalMethods().insertBinary(0, 2, nullByte); fail("Inserting null array"); } catch(NullPointerException e) { }
        
        
        assertEquals(new byte[] { 1, 2, 3 }, t.getBinaryByteArray(0, 0));
        assertEquals(false, t.getBinaryByteArray(0, 0) == new byte[] { 1, 2, 3 });
        
        byte[] newRow0 = new byte[] { 7, 77, 77 };
        t.setBinaryByteArray(0, 0, newRow0);
        
        assertEquals(new byte[] { 7, 77, 77 }, t.getBinaryByteArray(0, 0));
        assertEquals(false, t.getBinaryByteArray(0, 0) == new byte[] { 1, 2, 3 });
        
        try { t.setBinaryByteArray(0, 2, nullByte); fail("Inserting null array"); } catch(NullPointerException e) { }
    }
    

    @Test
    public void getNonExistingColumn() {
        Table t = new Table();
        t.addColumn(ColumnType.INTEGER, "int");
        
        assertEquals(-1, t.getColumnIndex("non-existing column"));
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void setDataInNonExistingRow() {
        Table t = new Table();
        t.addColumn(ColumnType.STRING, "colName");
        t.add("String val");
        
        t.set(7, "new string val"); // Exception expected. Row 7 does not exist
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void setDataWithWrongColumnAmountParameters() {
        Table t = new Table();
        t.addColumn(ColumnType.STRING, "colName");
        t.add("String val");
        
        t.set(0, "new string val", "This column does not exist"); // Exception expected. Table only has 1 column
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void addNegativeEmptyRows() {
        Table t = new Table();
        t.addColumn(ColumnType.STRING, "colName");
        
        t.addEmptyRows(-1); // Argument is negative, Throws exception
    }
    
    @Test(expectedExceptions = NullPointerException.class)
    public void addNullInMixedColumn() {
        Table t = new Table();
        t.addColumn(ColumnType.MIXED, "mixed");
        t.add(new Mixed(true));
        
        t.setMixed(0, 0, null); // Argument is null, Throws exception
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void setDataWithWrongColumnTypes() {
        Table t = new Table();
        t.addColumn(ColumnType.STRING, "colName");
        t.add("String val");
        
        t.set(0, 100); // Exception expected. Table has string column, and here an integer is inserted
    }
    
    @Test
    public void immutableInsertNotAllowed() {
        
        String FILENAME = "only-test-file.tightdb";
        String TABLENAME = "tableName";
        
        new File(FILENAME).delete();
        new File(FILENAME + ".lock").delete();

        SharedGroup group = new SharedGroup(FILENAME);

        // Write transaction must be run so where are sure a db exists with the correct table
        WriteTransaction wt = group.beginWrite();
        try{
            Table table = wt.getTable(TABLENAME);
            table.addColumn(ColumnType.STRING, "col0");
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
            
            try {  table.addAt(1, "NewValue"); fail("Exception excpeted when inserting in read transaction"); } catch (IllegalStateException e) { }
            
        } finally {
            rt.endRead();
        }
    }
    
    @Test
    public void shouldThrowWhenSetIndexOnWrongColumnType() {
        for (long colIndex = 0; colIndex < t.getColumnCount(); colIndex++) {
            
            // Check all other column types than String throws exception when using setIndex()/hasIndex()
            boolean exceptionExpected = (t.getColumnType(colIndex) != ColumnType.STRING);

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
        table.addColumn(ColumnType.STRING, "col");
        table.addColumn(ColumnType.INTEGER, "int");
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
        assertEquals(true, table1.equals(table1)); // Same table
        assertEquals(false, table1.equals(null)); // Null object
        assertEquals(false, table1.equals("String")); // Other object
    }
    

    @Test
    public void tableNumbers() {
        Table t = new Table();
        t.addColumn(ColumnType.INTEGER, "intCol");
        t.addColumn(ColumnType.DOUBLE, "doubleCol");
        t.addColumn(ColumnType.FLOAT, "floatCol");
        t.addColumn(ColumnType.STRING, "StringCol");
        
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
