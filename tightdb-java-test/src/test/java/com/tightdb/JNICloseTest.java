package com.tightdb;

import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tightdb.test.TestHelper;

public class JNICloseTest {

    @Test (expectedExceptions = IllegalStateException.class)
    public void shouldCloseTable() {
        // util.setDebugLevel(1);
        Table table = new Table();
        table.close();

        @SuppressWarnings("unused")
        long s = table.size();
        // TODO: a more specific Exception must be thrown from JNI..
    }

    // TODO: Much more testing needed.
    // Verify that methods make exceptions when Tables are invalidated.
    // Verify subtables are invalidated when table is changed/updated in any way.
    // Check that Group.close works

    @Test (enabled=false)
    public void shouldCloseGroup() {
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
        
        table.close(); //Table is being closed
        
        query.findAll(); //Should throw exception, as table has been closed
    }
    
    
    @Test()
    public void closeTableShouldThrowExceptionWhenAccessingView(){
        
        Table table = TestHelper.getTableWithAllColumnTypes();
        
        TableQuery query = table.where();
        
        TableView view = query.findAll();
        
        table.close(); //Closes the table, should not be allowed to access the view
        
        try{ view.size();                               assert(false); } catch (IllegalStateException e){} //size() should throw exception, as table is invalid
        
        try{ view.getBinaryByteArray(0, 0);             assert(false); } catch (IllegalStateException e){} //size() should throw exception, as table is invalid
        try{ view.getBoolean(1, 0);                     assert(false); } catch (IllegalStateException e){} //size() should throw exception, as table is invalid
        try{ view.getDate(2, 0);                        assert(false); } catch (IllegalStateException e){} //size() should throw exception, as table is invalid
        try{ view.getDouble(3, 0);                      assert(false); } catch (IllegalStateException e){} //size() should throw exception, as table is invalid
        try{ view.getFloat(4, 0);                       assert(false); } catch (IllegalStateException e){} //size() should throw exception, as table is invalid
        try{ view.getLong(5, 0);                        assert(false); } catch (IllegalStateException e){} //size() should throw exception, as table is invalid
        try{ view.getMixed(7, 0);                       assert(false); } catch (IllegalStateException e){} //size() should throw exception, as table is invalid
        try{ view.getString(7, 0);                      assert(false); } catch (IllegalStateException e){} //size() should throw exception, as table is invalid
    }
}
