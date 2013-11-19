package com.tightdb;
   
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.Date;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tightdb.test.TestHelper;

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
    public void rowOperationsOnZeroRow(){
        
        Table t = new Table();
        // Remove rows without columns
        try { t.remove(0); fail("No rows in table"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { t.remove(10); fail("No rows in table"); } catch (ArrayIndexOutOfBoundsException e) {}
        
        // Column added, remove rows again
        t.addColumn(ColumnType.STRING, "");
        try { t.remove(0); fail("No rows in table"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { t.remove(10); fail("No rows in table"); } catch (ArrayIndexOutOfBoundsException e) {}

    }
    
    @Test 
    public void testZeroColOperations() {
        Table tableZeroCols = new Table();
        
        // Add rows
        try { tableZeroCols.add("val"); fail("No columns in table"); } catch (IllegalArgumentException e) {}
        try { tableZeroCols.addEmptyRow(); fail("No columns in table"); } catch (IndexOutOfBoundsException e) {}
        try { tableZeroCols.addEmptyRows(10); fail("No columns in table"); } catch (IndexOutOfBoundsException e) {}
        
        
        // Col operations
        try { tableZeroCols.removeColumn(0); fail("No columns in table"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { tableZeroCols.renameColumn(0, "newName"); fail("No columns in table"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { tableZeroCols.removeColumn(10); fail("No columns in table"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { tableZeroCols.renameColumn(10, "newName"); fail("No columns in table"); } catch (ArrayIndexOutOfBoundsException e) {}
    }
    
    @Test
    public void tableBinaryTest() {
        Table t = new Table();
        t.addColumn(ColumnType.BINARY, "binary");
        
        byte[] row0 = new byte[] { 1, 2, 3 };
        byte[] row1 = new byte[] { 10, 20, 30 };
        
        t.getInternalMethods().insertBinary(0, 0, row0);
        t.getInternalMethods().insertDone();
        t.getInternalMethods().insertBinary(0, 1, row1);
        t.getInternalMethods().insertDone();
        
        byte[] nullByte = null;
        
        try { t.getInternalMethods().insertBinary(0, 2, nullByte); fail("Inserting null array"); } catch(NullPointerException e) { }
        
        
        assertEquals(new byte[] { 1, 2, 3 }, t.getBinaryByteArray(0, 0));
        assertEquals(false, t.getBinaryByteArray(0, 0) == new byte[]{1, 2, 3});
        
        byte[] newRow0 = new byte[] { 7, 77, 77 };
        t.setBinaryByteArray(0, 0, newRow0);
        
        assertEquals(new byte[] { 7, 77, 77 }, t.getBinaryByteArray(0, 0));
        assertEquals(false, t.getBinaryByteArray(0, 0) == new byte[] { 1, 2, 3 });
        
        try { t.setBinaryByteArray(0, 2, nullByte); fail("Inserting null array"); } catch(NullPointerException e) { }
    }
    
    
    @Test
    public void lookupTableTest() {
        Table t = new Table();
      
        long STRING_COL_INDEX   = t.addColumn(ColumnType.STRING, "col0");
        long INT_COL_INDEX      = t.addColumn(ColumnType.INTEGER, "col1");
        
        t.add("s", 1);
        t.add("s", 2);
        t.add("ss",1);
        t.add("ss", 2);
        t.add("", 2);
        
        assertEquals(0, t.lookup("s"));
        assertEquals(2, t.lookup("ss"));
        assertEquals(4, t.lookup(""));
        
        assertEquals(false, t.hasIndex(STRING_COL_INDEX));
        
        //try setting an index
        t.setIndex(0);
        assertEquals(0, t.lookup("s"));
        assertEquals(2, t.lookup("ss"));
        assertEquals(4, t.lookup(""));
        
        // null lookup value
        try {  t.lookup(null); fail("lookup value is null"); } catch (NullPointerException r) { };
        
        assertEquals(-1, t.lookup("I dont exist"));

        
        // Try with non string column
        Table t2 = new Table();
        t2.addColumn(ColumnType.INTEGER, "col0");
        t2.addColumn(ColumnType.INTEGER, "col1");
        t2.add(1, 2);
        t2.add(3, 4);
        
        try {  t2.lookup("ss"); fail("Column not String"); } catch (UnsupportedOperationException r) { };
    }
    
    
    @Test
    public void findFirstNonExisting() {
        Table t = TestHelper.getTableWithAllColumnTypes();
        t.add(new byte[]{1,2,3}, true, new Date(1384423149761l), 4.5d, 5.7f, 100, new Mixed("mixed"), "string", null);
        
        assertEquals(-1, t.findFirstBoolean(1, false));
        assertEquals(-1, t.findFirstDate(2, new Date(138442314986l)));
        assertEquals(-1, t.findFirstDouble(3, 1.0d));
        assertEquals(-1, t.findFirstFloat(4, 1.0f));
        assertEquals(-1, t.findFirstLong(5, 50));
        assertEquals(-1, t.findFirstString(7, "other string"));
    }
    
    
    @Test
    public void getValuesFromNonExistingColumn() {
        Table t = TestHelper.getTableWithAllColumnTypes();
        t.addEmptyRows(10);
        
        try { t.getBinaryByteArray(-1, 0); fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getBinaryByteArray(-10, 0); fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getBinaryByteArray(100, 0); fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }
        
        try { t.getBoolean(-1, 0); fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getBoolean(-10, 0); fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getBoolean(100, 0); fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }
        
        try { t.getDate(-1, 0); fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getDate(-10, 0); fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getDate(100, 0); fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }
        
        try { t.getDouble(-1, 0); fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getDouble(-10, 0); fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getDouble(100, 0); fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }
        
        try { t.getFloat(-1, 0); fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getFloat(-10, 0); fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getFloat(100, 0); fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }
        
        try { t.getLong(-1, 0); fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getLong(-10, 0); fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getLong(100, 0); fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }
        
        try { t.getMixed(-1, 0); fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getMixed(-10, 0); fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getMixed(100, 0); fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }
        
        try { t.getString(-1, 0); fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getString(-10, 0); fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getString(100, 0); fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }
        
        try { t.getSubTable(-1, 0); fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getSubTable(-10, 0); fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getSubTable(100, 0); fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }
        
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

      //  new File(FILENAME).delete();
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
            
            try {  table.addAt(1, "NewValue"); fail("Exception expected when inserting in read transaction"); } catch (IllegalStateException e) { }

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
    public void columnNameTest() {
        Table t = new Table();
        try { t.addColumn(ColumnType.STRING, "I am 64 chracters..............................................."); fail("Only 63 chracters supported"); } catch (IllegalArgumentException e) { }
        t.addColumn(ColumnType.STRING, "I am 63 chracters.............................................."); 
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
