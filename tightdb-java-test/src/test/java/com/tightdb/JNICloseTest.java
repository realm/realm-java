package com.tightdb;

import java.util.Date;

import org.testng.Assert;
import static org.testng.AssertJUnit.*;

import java.io.File;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tightdb.test.TestHelper;

// Tables get detached
public class JNICloseTest {

    @Test (expectedExceptions = IllegalStateException.class)
    public void shouldCloseTable() {
        // util.setDebugLevel(1);
        Table table = new Table();
        table.private_debug_close();

        @SuppressWarnings("unused")
        long s = table.size();
        
        // TODO: Test all methods...
    }

    // TODO: Much more testing needed.
    // Verify that methods make exceptions when Tables are invalidated.
    // Verify subtables are invalidated when table is changed/updated in any way.
    // Check that Group.close works

    @Test (enabled=false)
    public void shouldCloseGroup() { // TODO!
        
    	//Group group = new Group();

        //  EmployeeTable employees = new EmployeeTable(group);
    }
    
    /**
     * Make sure, that an illegalStateException is thrown when trying to do queries on a closed table
     */
    @Test(expectedExceptions = IllegalStateException.class)
    public void closeTableShouldThrowExceptionWhenQuery(){
        
        Table table = TestHelper.getTableWithAllColumnTypes();
        
        TableQuery query = table.where();
        
        table.private_debug_close(); //Table is being closed
        
        query.findAll(); //Should throw exception, as table has been closed
    }
    
    /**
     * Get methods should not be allowed when table has been closed using private_debug_close() method
     */
    @Test()
    public void tableClosedGetMethodsTest(){
        
        Table table = TestHelper.getTableWithAllColumnTypes();
        table.addEmptyRows(10);
        
        table.private_debug_close(); //Table is being closed
        
        try{ table.size();                       assert(false); } catch (IllegalStateException e){}
        try{ table.getBinaryByteArray(0, 0);     assert(false); } catch (IllegalStateException e){}
        try{ table.getBoolean(1, 0);             assert(false); } catch (IllegalStateException e){}        
        try{ table.getDate(2, 0);                assert(false); } catch (IllegalStateException e){}
        try{ table.getDouble(3, 0);              assert(false); } catch (IllegalStateException e){}
        try{ table.getFloat(4, 0);               assert(false); } catch (IllegalStateException e){}
        try{ table.getLong(5, 0);                assert(false); } catch (IllegalStateException e){}
        try{ table.getMixed(6, 0);               assert(false); } catch (IllegalStateException e){}
        try{ table.getString(7, 0);              assert(false); } catch (IllegalStateException e){}
    }
    
    
    
    
    /**
     * Make sure, that an illegalStateException is thrown when trying to do queries on a closed table
     */
    @Test()
    public void queryShouldThrowAfterTableClose(){
        Table table = TestHelper.getTableWithAllColumnTypes();
        table.addEmptyRows(10);
        for (long i=0; i<table.size(); i++)
        	table.setLong(5, i, i);
        TableQuery query = table.where(); 
        // Closes the table, it should not be allowed to access the view thereafter
        table.private_debug_close();
        table = null;
        Table table2 = TestHelper.getTableWithAllColumnTypes();
        table2.addEmptyRows(10);
        for (int i=0; i<table2.size(); i++)
        	table2.setLong(5, i, 117+i);

        TableView tv = query.findAll(); //Should throw exception, as table has been closed
        assertEquals(10, tv.size());

        // TODO: add a lot of methods
    }  

    @Test()
    public void accessingViewMethodsAfterTableClose(){
        Table table = TestHelper.getTableWithAllColumnTypes();
        table.addEmptyRows(10);
        TableQuery query = table.where(); 
        TableView view = query.findAll();
        //Closes the table, it should not be allowed to access the view thereafter
        table.private_debug_close();
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
    

    public void shouldThrowWhenAccessingViewAfterTableIsDetached()
    {
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
        try{ view.size();  						assert(false); } catch (IllegalStateException e){}
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
