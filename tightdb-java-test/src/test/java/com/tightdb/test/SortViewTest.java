package com.tightdb.test;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.Test;




import com.tightdb.ColumnType;
import com.tightdb.Table;
import com.tightdb.TableView;

public class SortViewTest {
    @Test
    public void shouldSortView() {

        //Specify table
        Table t = new Table();
        t.addColumn(ColumnType.ColumnTypeString, "Name");
        t.addColumn(ColumnType.ColumnTypeDouble, "GPA");
        t.addColumn(ColumnType.ColumnTypeInt, "Age");
        t.addColumn(ColumnType.ColumnTypeString, "Nickname");
        
        //Add data
        t.add("cc", 2.5, 24, "Pete");
        t.add("dd", 4.5, 35, "Su");
        t.add("bb", 3.6, 22, "Bri");
        t.add("aa", 3.6, 22, "Chris");
        
        //Get a view containing all rows in table
        TableView view = t.where().findAll();

        //Sort without specifying the order, should default to ascending. First row in colmun 0 should be aa
        view.sort(0);
        
        System.out.println(view.toJson());
        assertEquals("aa", view.getString(0, 0));
        
        //Sort ascending. First row in column 0 should be aa
        view.sort(0, TableView.Order.ascending);
        assertEquals("aa", view.getString(0, 0));
        
        //Sort descending. First row in column 0 should be dd
        view.sort(0, TableView.Order.descending);
        assertEquals("dd", view.getString(0, 0));
    }
}
