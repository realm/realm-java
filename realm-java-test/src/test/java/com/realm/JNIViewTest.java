package com.realm;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;

import com.realm.test.TestHelper;


@SuppressWarnings("deprecation")
public class JNIViewTest {
    Table t;
    Date date1 = new Date(2010-1900, 01, 05);
    Date date2 = new Date(1999-1900, 12, 01);
    Date date3 = new Date(1990-1900, 12, 24);
    Date date4 = new Date(2010-1900, 01, 04);

    @BeforeMethod
    void init() {
        //Specify table
        t = new Table();
        t.addColumn(ColumnType.STRING, "Name");
        t.addColumn(ColumnType.BOOLEAN,   "Study");
        t.addColumn(ColumnType.INTEGER,    "Age");
        t.addColumn(ColumnType.DATE,   "Birthday");

        // Add unsupported column types
        t.addColumn(ColumnType.STRING, "Unsupported0");
        t.addColumn(ColumnType.FLOAT,  "Unsupported1");
        t.addColumn(ColumnType.DOUBLE, "Unsupported2");
        t.addColumn(ColumnType.MIXED,  "Unsupported3");
        t.addColumn(ColumnType.TABLE,  "Unsupported4");

        //Add data
        t.add("cc", true,  24, date1, "", 0.0f, 0.0, 0, null);
        t.add("dd", false, 35, date2, "", 0.0f, 0.0, 0, null);
        t.add("bb", true,  22, date3, "", 0.0f, 0.0, 0, null);
        t.add("aa", false, 22, date4, "", 0.0f, 0.0, 0, null);

        assertEquals(date1, t.getDate(3, 0));
        assertEquals(date2, t.getDate(3, 1));
        assertEquals(date3, t.getDate(3, 2));
        assertEquals(date4, t.getDate(3, 3));
    }

    @Test
    public void unimplementedMethodsShouldFail() {
        //Get a view containing all rows in table since you can only sort views currently.
        TableView view = t.where().findAll();

        try { view.upperBoundLong(0, 0); fail("Not implemented yet"); } catch (RuntimeException e ) { }
        try { view.lowerBoundLong(0, 0); fail("Not implemented yet"); } catch (RuntimeException e ) { }
      //  try { view.lookup("Some String"); fail("Not implemented yet"); } catch (RuntimeException e ) { }
        try { view.count(0, "Some String"); fail("Not implemented yet"); } catch (RuntimeException e ) { }
    }


    @Test
    public void shouldSortViewDate() {
        //Get a view containing all rows in table since you can only sort views currently.
        TableView view = t.where().findAll();

        //Sort without specifying the order, should default to ascending.
        view.sort(3);
        assertEquals(date3, view.getDate(3, 0));
        assertEquals(date2, view.getDate(3, 1));
        assertEquals(date4, view.getDate(3, 2));
        assertEquals(date1, view.getDate(3, 3));
        assertEquals("cc", view.getString(0, 3));
    }


    @Test
    public void shouldSortViewIntegers() {
        //Get a view containing all rows in table since you can only sort views currently.
        TableView view = t.where().findAll();

        //Sort without specifying the order, should default to ascending.
        view.sort(2);
        assertEquals(22, view.getLong(2, 0));
        assertEquals(22, view.getLong(2, 1));
        assertEquals(24, view.getLong(2, 2));
        assertEquals(35, view.getLong(2, 3));
        assertEquals("dd", view.getString(0, 3));

        //Sort descending - creating a new view
        view.sort(2, TableView.Order.descending);
        assertEquals(35, view.getLong(2, 0));
        assertEquals(24, view.getLong(2, 1));
        assertEquals(22, view.getLong(2, 2));
        assertEquals(22, view.getLong(2, 3));
        assertEquals("dd", view.getString(0, 0));

        //Sort ascending.
        TableView view2 = t.where().findAll();
        view2.sort(2, TableView.Order.ascending);
        assertEquals(22, view2.getLong(2, 0));
        assertEquals(22, view2.getLong(2, 1));
        assertEquals(24, view2.getLong(2, 2));
        assertEquals(35, view2.getLong(2, 3));
        assertEquals("dd", view2.getString(0, 3));

        // Check that old view is still the same
        assertEquals(35, view.getLong(2, 0));
        assertEquals(24, view.getLong(2, 1));
        assertEquals(22, view.getLong(2, 2));
        assertEquals(22, view.getLong(2, 3));
        assertEquals("dd", view.getString(0, 0));
    }


    @Test
    public void setBinaryTest() {

        Table table = new Table();
        table.addColumn(ColumnType.BINARY, "binary");

        byte[] arr1 = new byte[] {1,2,3};
        table.add(arr1);
        assertEquals(arr1, table.getBinaryByteArray(0, 0));

        TableView view = table.where().findAll();

        byte[] arr2 = new byte[] {1,2,3, 4, 5};

        view.setBinaryByteArray(0, 0, arr2);

        assertEquals(arr2, view.getBinaryByteArray(0, 0));
    }


    @Test
    public void subtableTest() {
        Table persons = new Table();
        persons.addColumn(ColumnType.STRING, "name");
        persons.addColumn(ColumnType.STRING, "email");
        persons.addColumn(ColumnType.TABLE, "addresses");

        TableSchema addresses = persons.getSubtableSchema(2);
        addresses.addColumn(ColumnType.STRING, "street");
        addresses.addColumn(ColumnType.INTEGER, "zipcode");
        addresses.addColumn(ColumnType.TABLE, "phone_numbers");

        TableSchema phone_numbers = addresses.getSubtableSchema(2);
        phone_numbers.addColumn(ColumnType.INTEGER, "number");

        // Inserting data
        persons.add(new Object[] {"Mr X", "xx@xxxx.com", 
                                  new Object[][] { { "X Street", 1234, new Object[][] {{ 12345678 }} },
                                                   { "Y Street", 1234, new Object[][] {{ 12345678 }} }
                                                 } 
                                 });

        TableView personsView = persons.where().findAll();

        assertEquals(2, personsView.getSubtableSize(2, 0));

        Table address = personsView.getSubtable(2, 0);
        assertEquals(2, address.size());
        assertEquals(3, address.getColumnCount());

        personsView.clearSubtable(2, 0);
        assertEquals(0, personsView.getSubtableSize(2, 0));
    }

    @Test
    public void sortOnNonexistingColumn() {
        TableView view = t.where().findAll();

        try { view.sort(-1); fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.sort(-100); fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.sort(100); fail("Column is 100, column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }
    }


    @Test
    public void findFirstNonExisting() {
        Table tt = TestHelper.getTableWithAllColumnTypes();
        tt.add(new byte[]{1,2,3}, true, new Date(1384423149761l), 4.5d, 5.7f, 100, new Mixed("mixed"), "string", null);
        TableView v = tt.where().findAll();

        assertEquals(-1, v.findFirstBoolean(1, false));
        assertEquals(-1, v.findFirstDate(2, new Date(138442314986l)));
        assertEquals(-1, v.findFirstDouble(3, 1.0d));
        assertEquals(-1, v.findFirstFloat(4, 1.0f));
        assertEquals(-1, v.findFirstLong(5, 50));
        assertEquals(-1, v.findFirstString(7, "other string"));
    }


    @Test
    public void getValuesFromNonExistingColumn() {
        Table table = TestHelper.getTableWithAllColumnTypes();
        TableView view = table.where().findAll();
        
        try { view.getBinaryByteArray(-1, 0);   fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getBinaryByteArray(-10, 0);  fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getBinaryByteArray(100, 0);  fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }
        
        try { view.getBoolean(-1, 0);           fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getBoolean(-10, 0);          fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getBoolean(100, 0);          fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }
        
        try { view.getDate(-1, 0);              fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getDate(-10, 0);             fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getDate(100, 0);             fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }
        
        try { view.getDouble(-1, 0);            fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getDouble(-10, 0);           fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getDouble(100, 0);           fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }
        
        try { view.getFloat(-1, 0);             fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getFloat(-10, 0);            fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getFloat(100, 0);            fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }
        
        try { view.getLong(-1, 0);              fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getLong(-10, 0);             fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getLong(100, 0);             fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }
        
        try { view.getMixed(-1, 0);             fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getMixed(-10, 0);            fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getMixed(100, 0);            fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }
        
        try { view.getString(-1, 0);            fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getString(-10, 0);           fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getString(100, 0);           fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }
        
        try { view.getSubtable(-1, 0);          fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getSubtable(-10, 0);         fail("Column is less than 0"); } catch (ArrayIndexOutOfBoundsException e) { }
        try { view.getSubtable(100, 0);         fail("Column does not exist"); } catch (ArrayIndexOutOfBoundsException e) { }
    }


    @Test
    public void testGetSourceRow() {
        Table t = new Table();
        t.addColumn(ColumnType.STRING, "");
        t.addColumn(ColumnType.INTEGER, "");
        t.addColumn(ColumnType.BOOLEAN, "");

        t.add("1", 1, true);
        t.add("2", 2, true);
        t.add("3", 3, false);
        t.add("4", 5, false);

        TableView v = t.where().equalTo(2, false).findAll();

        assertEquals(2, v.getSourceRowIndex(0));
        assertEquals(3, v.getSourceRowIndex(1));

        // Out of bound
        try { assertEquals(0, v.getSourceRowIndex(2));      fail("index ot of bounds"); } catch (IndexOutOfBoundsException e) { }
        try { assertEquals(0, v.getSourceRowIndex(100));    fail("index ot of bounds"); } catch (IndexOutOfBoundsException e) { }
        try { assertEquals(0, v.getSourceRowIndex(-1));     fail("index ot of bounds"); } catch (IndexOutOfBoundsException e) { }
        try { assertEquals(0, v.getSourceRowIndex(-100));   fail("index ot of bounds"); } catch (IndexOutOfBoundsException e) { }
    }


    @Test
    public void testGetSourceRowNoRows() {
        Table t = new Table();
        t.addColumn(ColumnType.STRING, "");
        t.addColumn(ColumnType.INTEGER, "");
        t.addColumn(ColumnType.BOOLEAN, "");
        // No data is added
        TableView v = t.where().findAll();

        // Out of bound
        try { assertEquals(0, v.getSourceRowIndex(0));      fail("index ot of bounds"); } catch (IndexOutOfBoundsException e) { }
        try { assertEquals(0, v.getSourceRowIndex(1));      fail("index ot of bounds"); } catch (IndexOutOfBoundsException e) { }
    }


    @Test
    public void testGetSourceRowEmptyTable() {
        Table t = new Table();
        // No columns
        TableView v = t.where().findAll();

        // Out of bound
        try { assertEquals(0, v.getSourceRowIndex(0));      fail("index ot of bounds"); } catch (IndexOutOfBoundsException e) { }
        try { assertEquals(0, v.getSourceRowIndex(1));      fail("index ot of bounds"); } catch (IndexOutOfBoundsException e) { }
    }


    @Test
    public void shouldSortViewBool() {
        //Get a view containing all rows in table since you can only sort views currently.
        TableView view = t.where().findAll();

        //Sort without specifying the order, should default to ascending.
        view.sort(1);
        assertEquals(false, view.getBoolean(1, 0));
        assertEquals(false, view.getBoolean(1, 1));
        assertEquals(true, view.getBoolean(1, 2));
        assertEquals(true, view.getBoolean(1, 3));
        assertEquals("bb", view.getString(0, 3));
    }



    @Test
    public void shouldThrowExceptionForUnsupportedColumns() {
        TableView view = t.where().findAll();
        long colIndex;
        for (colIndex = 4; colIndex <= 8; colIndex++) {
            try {
                view.sort(colIndex); // Must throw for invalid column types
                fail("expected exception.");
            } catch (IllegalArgumentException e) {
            }
        }
    }

    @Test
    public void shouldSearchByColumnValue() {
        Table table = new Table();
        table.addColumn(ColumnType.STRING, "name");

        table.add("Foo");
        table.add("Bar");

        TableQuery query = table.where();
        TableView view = query.findAll(0, table.size(), Integer.MAX_VALUE);
        assertEquals(2, view.size());

        view.findAllString(0, "Foo");
    }

    @Test
    public void shouldQueryInView() {
        Table table = new Table();
        table.addColumn(ColumnType.STRING, "name");

        table.add("A1");
        table.add("B");
        table.add("A2");
        table.add("B");
        table.add("A3");
        table.add("B");
        table.add("A3");

        TableQuery query = table.where();
        TableView view = query.beginsWith(0, "A").findAll(0, table.size(), Table.INFINITE);
        assertEquals(4, view.size());

        TableQuery query2 = table.where();
        TableView view2 = query2.tableview(view).contains(0, "3").findAll();
        assertEquals(2, view2.size());
    }

    @Test
    public void getNonExistingColumn() {
        Table t = new Table();
        t.addColumn(ColumnType.INTEGER, "int");
        TableView view = t.where().findAll();
        assertEquals(-1, view.getColumnIndex("non-existing column"));
    }

    @Test
    public void getNullColumn() {
        Table t = new Table();
        t.addColumn(ColumnType.INTEGER, "");
        TableView view = t.where().findAll();
        try { view.getColumnIndex(null); fail("Getting null column"); } catch(IllegalArgumentException e) { }
    }


    @Test
    public void viewToString() {
        Table t = new Table();
        t.addColumn(ColumnType.STRING, "stringCol");
        t.addColumn(ColumnType.INTEGER, "intCol");
        t.addColumn(ColumnType.BOOLEAN, "boolCol");

        t.add("s1", 1, true);
        t.add("s2", 2, false);

        TableView view = t.where().findAll();

        String expected =
                "    stringCol  intCol  boolCol\n" +
                        "0:  s1              1     true\n" +
                        "1:  s2              2    false\n" ;

        assertEquals(expected, view.toString());
    }

    void accessingViewOk(TableView view)
    {
        view.size();
        view.isEmpty();
        view.getLong(0, 0);
        view.getColumnCount();
        view.getColumnName(0);
        view.getColumnIndex("");
        view.getColumnType(0);
        view.averageLong(0);
        view.maximumLong(0);
        view.minimumLong(0);
        view.sumLong(0);
        view.findAllLong(0, 2);
        view.findFirstLong(0, 2);
        view.where();
        view.toJson();
        view.toString();
    }

    void accessingViewMustThrow(TableView view)
    {
        try { view.size();              assert(false); } catch (IllegalStateException e) {}
        try { view.isEmpty();           assert(false); } catch (IllegalStateException e) {}
        try { view.getLong(0,0);        assert(false); } catch (IllegalStateException e) {}
        try { view.getColumnCount();    assert(false); } catch (IllegalStateException e) {}
        try { view.getColumnName(0);    assert(false); } catch (IllegalStateException e) {}
        try { view.getColumnIndex("");  assert(false); } catch (IllegalStateException e) {}
        try { view.getColumnType(0);    assert(false); } catch (IllegalStateException e) {}
        try { view.averageLong(0);      assert(false); } catch (IllegalStateException e) {}
        try { view.maximumLong(0);      assert(false); } catch (IllegalStateException e) {}
        try { view.minimumLong(0);      assert(false); } catch (IllegalStateException e) {}
        try { view.sumLong(0);          assert(false); } catch (IllegalStateException e) {}
        try { view.findAllLong(0, 2);   assert(false); } catch (IllegalStateException e) {}
        try { view.findFirstLong(0, 2); assert(false); } catch (IllegalStateException e) {}
        try { view.where();             assert(false); } catch (IllegalStateException e) {}
        try { view.toJson();            assert(false); } catch (IllegalStateException e) {}
        try { view.toString();          assert(false); } catch (IllegalStateException e) {}
    }

    @Test
    public void viewShouldInvalidate() {
        Table t = new Table();
        t.addColumn(ColumnType.INTEGER, "intCol");
        t.add(1);
        t.add(2);
        t.add(3);

        TableView view = t.where().equalTo(0, 2).findAll();
        // access view is ok.
        assertEquals(1, view.size());

        // access view after change in value is ok
        t.setLong(0, 0, 3);
        accessingViewOk(view);

        // access view after additions to table must fail
        t.add(4);
        accessingViewMustThrow(view);

        // recreate view to access again
        view = t.where().equalTo(0, 2).findAll();
        accessingViewOk(view);

        // Removing any row in Table should invalidate view 
        t.remove(3);
        accessingViewMustThrow(view);
    }

    @Test
    public void maximumDate() {

        Table table = new Table();
        table.addColumn(ColumnType.DATE, "date");

        table.add(new Date(0));
        table.add(new Date(10000));
        table.add(new Date(1000));

        TableView view = table.where().findAll();

        assertEquals(new Date(10000), view.maximumDate(0));

    }

    @Test
    public void minimumDate() {

        Table table = new Table();
        table.addColumn(ColumnType.DATE, "date");

        table.add(new Date(10000));
        table.add(new Date(0));
        table.add(new Date(1000));

        TableView view = table.where().findAll();

        assertEquals(new Date(0), view.minimumDate(0));

    }
}
