package io.realm.internal;

import android.test.AndroidTestCase;
import android.test.MoreAsserts;

import java.io.File;
import java.util.Date;

import io.realm.internal.test.TestHelper;

public class JNITableTest extends AndroidTestCase {

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

    @Override
    public void setUp() {
        t = createTestTable();
    }

    public void testTableToString() {
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

    public void testRowOperationsOnZeroRow(){

        Table t = new Table();
        // Remove rows without columns
        try { t.remove(0);  fail("No rows in table"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { t.remove(10); fail("No rows in table"); } catch (ArrayIndexOutOfBoundsException e) {}

        // Column added, remove rows again
        t.addColumn(ColumnType.STRING, "");
        try { t.remove(0);  fail("No rows in table"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { t.remove(10); fail("No rows in table"); } catch (ArrayIndexOutOfBoundsException e) {}

    }

    public void testZeroColOperations() {
        Table tableZeroCols = new Table();

        // Add rows
        try { tableZeroCols.add("val");         fail("No columns in table"); } catch (IllegalArgumentException e) {}
        try { tableZeroCols.addEmptyRow();      fail("No columns in table"); } catch (IndexOutOfBoundsException e) {}
        try { tableZeroCols.addEmptyRows(10);   fail("No columns in table"); } catch (IndexOutOfBoundsException e) {}


        // Col operations
        try { tableZeroCols.removeColumn(0);                fail("No columns in table"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { tableZeroCols.renameColumn(0, "newName");     fail("No columns in table"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { tableZeroCols.removeColumn(10);               fail("No columns in table"); } catch (ArrayIndexOutOfBoundsException e) {}
        try { tableZeroCols.renameColumn(10, "newName");    fail("No columns in table"); } catch (ArrayIndexOutOfBoundsException e) {}
    }

    public void testTableBinaryTest() {
        Table t = new Table();
        t.addColumn(ColumnType.BINARY, "binary");

        byte[] row0 = new byte[]{1, 2, 3};
        byte[] row1 = new byte[]{10, 20, 30};

        t.getInternalMethods().insertBinary(0, 0, row0);
        t.getInternalMethods().insertDone();
        t.getInternalMethods().insertBinary(0, 1, row1);
        t.getInternalMethods().insertDone();

        byte[] nullByte = null;


        MoreAsserts.assertEquals(new byte[]{1, 2, 3}, t.getBinaryByteArray(0, 0));
        assertEquals(false, t.getBinaryByteArray(0, 0) == new byte[]{1, 2, 3});

        byte[] newRow0 = new byte[]{7, 77, 77};
        t.setBinaryByteArray(0, 0, newRow0);

        MoreAsserts.assertEquals(new byte[]{7, 77, 77}, t.getBinaryByteArray(0, 0));
        assertEquals(false, t.getBinaryByteArray(0, 0) == new byte[]{1, 2, 3});
    }

    public void testTableNotNullableBinaryTest() {
        Table t = new Table();
        t.addColumn(ColumnType.BINARY, "binary", false);

        byte[] nullByte = null;

        try {
            t.getInternalMethods().insertBinary(0, 2, nullByte);
            fail("Inserting null array");
        } catch (IllegalArgumentException e) {
        }

        t.addEmptyRow();
        try { t.setBinaryByteArray(0, 0, nullByte); fail("Inserting null array"); } catch(IllegalArgumentException e) { }
    }


    public void testFindFirstNonExisting() {
        Table t = TestHelper.getTableWithAllColumnTypes();
        t.add(new byte[]{1,2,3}, true, new Date(1384423149761l), 4.5d, 5.7f, 100, new Mixed("mixed"), "string", null);

        assertEquals(-1, t.findFirstBoolean(1, false));
        assertEquals(-1, t.findFirstDate(2, new Date(138442314986l)));
        assertEquals(-1, t.findFirstDouble(3, 1.0d));
        assertEquals(-1, t.findFirstFloat(4, 1.0f));
        assertEquals(-1, t.findFirstLong(5, 50));
        assertEquals(-1, t.findFirstString(7, "other string"));
    }


    public void testGetValuesFromNonExistingColumn() {
        Table t = TestHelper.getTableWithAllColumnTypes();
        t.addEmptyRows(10);

        try { t.getBinaryByteArray(-1, 0);          fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getBinaryByteArray(-10, 0);         fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getBinaryByteArray(9, 0);           fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }

        try { t.getBoolean(-1, 0);                  fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getBoolean(-10, 0);                 fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getBoolean(9, 0);                   fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }

        try { t.getDate(-1, 0);                     fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getDate(-10, 0);                    fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getDate(9, 0);                      fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }

        try { t.getDouble(-1, 0);                   fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getDouble(-10, 0);                  fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getDouble(9, 0);                    fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }

        try { t.getFloat(-1, 0);                    fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getFloat(-10, 0);                   fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getFloat(9, 0);                     fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }

        try { t.getLong(-1, 0);                     fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getLong(-10, 0);                    fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getLong(9, 0);                      fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }

        try { t.getMixed(-1, 0);                    fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getMixed(-10, 0);                   fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getMixed(9, 0);                     fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }

        try { t.getString(-1, 0);                   fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getString(-10, 0);                  fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getString(9, 0);                    fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }

        try { t.getSubtable(-1, 0);                 fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getSubtable(-10, 0);                fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getSubtable(9, 0);                  fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }

    }

    public void testGetNonExistingColumn() {
        Table t = new Table();
        t.addColumn(ColumnType.INTEGER, "int");

        assertEquals(-1, t.getColumnIndex("non-existing column"));
        try { t.getColumnIndex(null); fail("column name null"); } catch (IllegalArgumentException e) { }
    }


    public void testGetSortedView() {
        Table t = new Table();
        t.addColumn(ColumnType.INTEGER, "");
        t.addColumn(ColumnType.STRING, "");
        t.addColumn(ColumnType.DOUBLE, "");

        t.add(1, "s", 1000d);
        t.add(3,"sss", 10d);
        t.add(2, "ss", 100d);


        // Check the order is as it is added
        assertEquals(1, t.getLong(0, 0));
        assertEquals(3, t.getLong(0, 1));
        assertEquals(2, t.getLong(0, 2));
        assertEquals("s", t.getString(1, 0));
        assertEquals("sss", t.getString(1, 1));
        assertEquals("ss", t.getString(1, 2));
        assertEquals(1000d, t.getDouble(2, 0));
        assertEquals(10d, t.getDouble(2, 1));
        assertEquals(100d, t.getDouble(2, 2));

        // Get the sorted view on first column
        TableView v = t.getSortedView(0);

        // Check the new order
        assertEquals(1, v.getLong(0, 0));
        assertEquals(2, v.getLong(0, 1));
        assertEquals(3, v.getLong(0, 2));
        assertEquals("s", v.getString(1, 0));
        assertEquals("ss", v.getString(1, 1));
        assertEquals("sss", v.getString(1, 2));
        assertEquals(1000d, v.getDouble(2, 0));
        assertEquals(100d, v.getDouble(2, 1));
        assertEquals(10d, v.getDouble(2, 2));

        // Get the sorted view on first column descending
        v = t.getSortedView(0, TableView.Order.descending);

        // Check the new order
        assertEquals(3, v.getLong(0, 0));
        assertEquals(2, v.getLong(0, 1));
        assertEquals(1, v.getLong(0, 2));
        assertEquals("sss", v.getString(1, 0));
        assertEquals("ss", v.getString(1, 1));
        assertEquals("s", v.getString(1, 2));
        assertEquals(10d, v.getDouble(2, 0));
        assertEquals(100d, v.getDouble(2, 1));
        assertEquals(1000d, v.getDouble(2, 2));

        // Some out of bounds test cases
        try { t.getSortedView(-1, TableView.Order.descending);    fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getSortedView(-100, TableView.Order.descending);  fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { t.getSortedView(100, TableView.Order.descending);   fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }

    }

    public void testSetDataInNonExistingRow() {
        Table t = new Table();
        t.addColumn(ColumnType.STRING, "colName");
        t.add("String val");

        try { t.set(1, "new string val"); fail("Row 1 does not exist"); } catch (IllegalArgumentException e) { }
    }


    public void testSetNulls() {
        Table t = new Table();
        t.addColumn(ColumnType.STRING, "");
        t.addColumn(ColumnType.DATE, "");
        t.addColumn(ColumnType.MIXED, "");
        t.addColumn(ColumnType.BINARY, "");
        t.add("String val", new Date(), new Mixed(""), new byte[] { 1,2,3} );

        try { t.setString(0, 0, null);  fail("null string not allowed"); } catch (IllegalArgumentException e) { }
        try { t.setDate(1, 0, null);    fail("null Date not allowed"); } catch (IllegalArgumentException e) { }
    }

    public void testSetDataWithWrongColumnAmountParameters() {
        Table t = new Table();
        t.addColumn(ColumnType.STRING, "colName");
        t.add("String val");

        try { t.set(0, "new string val", "This column does not exist"); fail("Table only has 1 column"); } catch (IllegalArgumentException e) { }
    }

    public void testAddNegativeEmptyRows() {
        Table t = new Table();
        t.addColumn(ColumnType.STRING, "colName");

        try { t.addEmptyRows(-1); fail("Argument is negative"); } catch (IllegalArgumentException e ) { }
    }

    public void testAddNullInMixedColumn() {
        Table t = new Table();
        t.addColumn(ColumnType.MIXED, "mixed");
        t.add(new Mixed(true));

        try { t.setMixed(0, 0, null); fail("Argument is null"); } catch (IllegalArgumentException e) { }
    }

    public void testSetDataWithWrongColumnTypes() {
        Table t = new Table();
        t.addColumn(ColumnType.STRING, "colName");
        t.add("String val");

        try { t.set(0, 100); fail("Table has string column, and here an integer is inserted"); } catch (IllegalArgumentException e) { }
    }

    public void testImmutableInsertNotAllowed() {

        String FILENAME = new File(this.getContext().getFilesDir(), "only-test-file.realm").toString();
        String TABLENAME = "tableName";

        new File(FILENAME).delete();
        new File(FILENAME+".lock").delete();
        SharedGroup group = new SharedGroup(FILENAME);

        // Write transaction must be run so we are sure a db exists with the correct table
        WriteTransaction wt = group.beginWrite();
        try {
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
        try {
            Table table = rt.getTable(TABLENAME);

            try {  table.addAt(1, "NewValue"); fail("Exception expected when inserting in read transaction"); } catch (IllegalStateException e) { }

        } finally {
            rt.endRead();
        }
    }

    public void testGetName() {
        String FILENAME = new File(this.getContext().getFilesDir(), "only-test-file.realm").toString();
        String TABLENAME = "tableName";

        new File(FILENAME).delete();
        new File(FILENAME+".lock").delete();
        SharedGroup group = new SharedGroup(FILENAME);

        // Write transaction must be run so we are sure a db exists with the correct table
        WriteTransaction wt = group.beginWrite();
        try {
            Table table = wt.getTable(TABLENAME);
            wt.commit();
        } catch (Throwable t) {
            wt.rollback();
        }

        ReadTransaction rt = group.beginRead();
        Table table = rt.getTable(TABLENAME);
        assertEquals(TABLENAME, table.getName());
    }

    public void testShouldThrowWhenSetIndexOnWrongColumnType() {
        for (long colIndex = 0; colIndex < t.getColumnCount(); colIndex++) {

            // Check all other column types than String throws exception when using addSearchIndex()/hasSearchIndex()
            boolean exceptionExpected = (t.getColumnType(colIndex) != ColumnType.STRING);

            // Try to addSearchIndex()
            try {
                t.addSearchIndex(colIndex);
                if (exceptionExpected)
                    fail("expected exception for colIndex " + colIndex);
            } catch (IllegalArgumentException e) {
            }

            // Try to hasSearchIndex() for all columnTypes
            t.hasSearchIndex(colIndex);
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

    public void testColumnName() {
        Table t = new Table();
        try { t.addColumn(ColumnType.STRING, "I am 64 chracters..............................................."); fail("Only 63 chracters supported"); } catch (IllegalArgumentException e) { }
        t.addColumn(ColumnType.STRING, "I am 63 chracters..............................................");
    }

    public void testTableNumbers() {
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

    public void testMaximumDate() {

        Table table = new Table();
        table.addColumn(ColumnType.DATE, "date");

        table.add(new Date(0));
        table.add(new Date(10000));
        table.add(new Date(1000));

        assertEquals(new Date(10000), table.maximumDate(0));

    }

    public void testMinimumDate() {

        Table table = new Table();
        table.addColumn(ColumnType.DATE, "date");

        table.add(new Date(10000));
        table.add(new Date(0));
        table.add(new Date(1000));

        assertEquals(new Date(0), table.minimumDate(0));

    }

}
