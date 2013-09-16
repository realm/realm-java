package com.tightdb;

import org.testng.annotations.Test;

import com.tightdb.test.TestHelper;

public class JNICloseTest {

    @Test (enabled=true, expectedExceptions = IllegalStateException.class)
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
    public void queryShouldThrowAfterTableClose(){
        
        Table table = TestHelper.getTableWithAllColumnTypes();
        TableQuery query = table.where();
        table.private_debug_close(); //Table is being closed
        
        query.findAll(); //Should throw exception, as table has been closed
        // TODO: add a lot of methods
    }  

    @Test()
    public void shouldThrowExceptionWhenAccessingViewAfterTableClose(){
        
        Table table = TestHelper.getTableWithAllColumnTypes();
        
        table.addEmptyRow();
        TableQuery query = table.where(); 
        TableView view = query.findAll();
        table.private_debug_close(); //Closes the table, it should not be allowed to access the view thereafter
        table = null;
        
        //methods below should throw exception, as table is invalid
        try{ view.size();  						assert(false); } catch (IllegalStateException e){}
        try{ view.getBinaryByteArray(0, 0);     assert(false); } catch (IllegalStateException e){}
        try{ view.getBoolean(1, 0);             assert(false); } catch (IllegalStateException e){}        
        try{ view.getDate(2, 0);                assert(false); } catch (IllegalStateException e){}
        try{ view.getDouble(3, 0);              assert(false); } catch (IllegalStateException e){}
        try{ view.getFloat(4, 0);               assert(false); } catch (IllegalStateException e){}
        try{ view.getLong(5, 0);                assert(false); } catch (IllegalStateException e){}
        try{ view.getMixed(7, 0);               assert(false); } catch (IllegalStateException e){}
        try{ view.getString(7, 0);              assert(false); } catch (IllegalStateException e){}
    }
}
