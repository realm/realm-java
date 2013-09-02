package com.tightdb;
   
import org.testng.annotations.BeforeMethod;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;
import java.util.Date;
import com.tightdb.ColumnType;
import com.tightdb.Table;
import com.tightdb.TableView;

    
public class JNIViewSortTest {
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
	    t.addColumn(ColumnType.LONG,    "Age");
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
	}


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

    public void shouldThrowExceptionForUnsupportedColumns() {

	    TableView view = t.where().findAll();

		long colIndex;
		for (colIndex = 4; colIndex <= 8; colIndex++) {
			try {
				view.sort(colIndex); // Must throw for invalid column types		
				assertTrue(false);
			} catch (IllegalArgumentException e) {		
			}
		}

    }

	
}