package com.tightdb;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;

import com.tightdb.ColumnType;
import com.tightdb.Table;
import com.tightdb.TableView;


@SuppressWarnings("deprecation")
public class JNIViewTest {
    Table t;
    Date date1 = new Date(2010, 01, 05);
    Date date2 = new Date(1999, 12, 01);
    Date date3 = new Date(1990, 12, 24);
    Date date4 = new Date(2010, 01, 04);

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
	    
	    System.err.println("Setting out test date to");
	    System.err.println(date1.getTime()/1000);
	    t.setDate(3,0, date1);
	    assertEquals(date1, t.getDate(3, 0));
	    assertEquals(date2, t.getDate(3, 1));
	    assertEquals(date3, t.getDate(3, 2));
	    assertEquals(date4, t.getDate(3, 3));
	}
    
    @Test
    public void unimplementedMethodsShouldFail() {    
        //Get a view containing all rows in table since you can only sort views currently.
        TableView view = t.where().findAll();

        //Sort without specifying the order, should default to ascending.
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

        TableSchema addresses = persons.getSubTableSchema(2);
        addresses.addColumn(ColumnType.STRING, "street");
        addresses.addColumn(ColumnType.INTEGER, "zipcode");
        addresses.addColumn(ColumnType.TABLE, "phone_numbers");

        TableSchema phone_numbers = addresses.getSubTableSchema(2);
        phone_numbers.addColumn(ColumnType.INTEGER, "number");

        // Inserting data
        persons.add(new Object[] {"Mr X", "xx@xxxx.com", new Object[][] {
                                                                            { "X Street", 1234, new Object[][] {{ 12345678 }} },
                                                                            { "Y Street", 1234, new Object[][] {{ 12345678 }} }
                                                                                                                                    } });
        
        TableView personsView = persons.where().findAll();
        
        assertEquals(2, personsView.getSubTableSize(2, 0));
        
        Table address = personsView.getSubTable(2, 0);
        assertEquals(2, address.size());
        assertEquals(3, address.getColumnCount());
        
        personsView.clearSubTable(2, 0);
        assertEquals(0, personsView.getSubTableSize(2, 0));


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

        TableSpec tableSpec = new TableSpec();
        tableSpec.addColumn(ColumnType.STRING, "name");
        table.updateFromSpec(tableSpec);

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

        TableSpec tableSpec = new TableSpec();
        tableSpec.addColumn(ColumnType.STRING, "name");
        table.updateFromSpec(tableSpec);

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
        try { view.getColumnIndex(null); fail("Getting null column"); } catch(NullPointerException e) { }
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
}
