package com.tightdb;

import static org.testng.AssertJUnit.*;

import java.io.File;

import org.testng.annotations.Test;

import com.tightdb.test.TestHelper;

// Tables get detached

public class JNICloseTest {

    @Test
    public void shouldCloseTable() throws Throwable {
        Table table = new Table();
        table.close();

        try { table.size();                            fail("Table is closed"); } catch (IllegalStateException e) { }
        try { table.getColumnCount();                  fail("Table is closed"); } catch (IllegalStateException e) { }
        try { table.addColumn(ColumnType.STRING, "");  fail("Table is closed"); } catch (IllegalStateException e) { }

        // TODO: Test all methods...
    }

    // TODO: Much more testing needed.
    // Verify that methods make exceptions when Tables are invalidated.
    // Verify subtables are invalidated when table is changed/updated in any way.
    // Check that Group.close works

    @Test
    public void shouldCloseGroup() { 

        Group group = new Group();
        group.close();

        try { group.getTable("t");    fail("Group is closed"); } catch (IllegalStateException e) { }
        try { group.size();                     fail("Group is closed"); } catch (IllegalStateException e) { }
    }

    /**
     * Make sure, that it's possible to use the query on a closed table
     */
    @Test()
    public void queryAccessibleAfterTableClose() throws Throwable{
        Table table = TestHelper.getTableWithAllColumnTypes();
        table.addEmptyRows(10);
        for (long i=0; i<table.size(); i++)
            table.setLong(5, i, i);
        TableQuery query = table.where();
        // Closes the table, it _should_ be allowed to access the query thereafter
        table.finalize();
        table = null;
        Table table2 = TestHelper.getTableWithAllColumnTypes();
        table2.addEmptyRows(10);
        for (int i=0; i<table2.size(); i++)
            table2.setLong(5, i, 117+i);

        TableView tv = query.findAll();
        assertEquals(10, tv.size());

        // TODO: add a lot of methods
    }

    @Test()
    public void accessingViewMethodsAfterTableClose() throws Throwable{
        Table table = TestHelper.getTableWithAllColumnTypes();
        table.addEmptyRows(10);
        TableQuery query = table.where();
        TableView view = query.findAll();
        //Closes the table, it should be allowed to access the view thereafter (table is ref-counted)
        table.finalize();
        table = null;

        // Accessing methods should be ok.
        view.size();
        view.getBinaryByteArray(0, 0);
        view.getBoolean(1, 0);
        view.getDate(2, 0);
        view.getDouble(3, 0);
        view.getFloat(4, 0);
        view.getLong(5, 0);
        view.getMixed(6, 0);
        view.getString(7, 0);

        // TODO - add all methods from view
    }


    public void shouldThrowWhenAccessingViewAfterTableIsDetached() {
        final String testFile = "closetest.tightdb";
        SharedGroup db;
        File f = new File(testFile);
        if (f.exists())
            f.delete();
        db = new SharedGroup(testFile);

        WriteTransaction trans = db.beginWrite();
        Table tbl = trans.getTable("EmployeeTable");
        tbl.addColumn(ColumnType.STRING, "name");
        tbl.addColumn(ColumnType.INTEGER, "number");
        TableView view = tbl.where().findAll();

        trans.commit();

        //methods below should throw exception, as table is invalid after commit
        try{ view.size();                       assert(false); } catch (IllegalStateException e){}
        try{ view.getBinaryByteArray(0, 0);     assert(false); } catch (IllegalStateException e){}
        try{ view.getBoolean(1, 0);             assert(false); } catch (IllegalStateException e){}
        try{ view.getDate(2, 0);                assert(false); } catch (IllegalStateException e){}
        try{ view.getDouble(3, 0);              assert(false); } catch (IllegalStateException e){}
        try{ view.getFloat(4, 0);               assert(false); } catch (IllegalStateException e){}
        try{ view.getLong(5, 0);                assert(false); } catch (IllegalStateException e){}
        try{ view.getMixed(6, 0);               assert(false); } catch (IllegalStateException e){}
        try{ view.getString(7, 0);              assert(false); } catch (IllegalStateException e){}
// TODO: Add more methods

        db.close();
        f.delete();
    }

}
