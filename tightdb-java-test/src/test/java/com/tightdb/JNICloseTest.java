package com.tightdb;

import org.testng.annotations.Test;

import com.tightdb.test.TestHelper;

public class JNICloseTest {

    @Test (enabled=true, expectedExceptions = IllegalStateException.class)
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
     * Make sure, that an illegavlStateException is thrown when trying to do queries on a closed table
     */
    @Test(expectedExceptions = IllegalStateException.class)
    public void closeTableShouldThrowExceptionWhenQuery(){
        
        Table table = TestHelper.getTableWithAllColumnTypes();
        
        TableQuery query = table.where();
        
        table.close(); //Table is being closed
        
        query.findAll(); //Should throw exception, as table has been closed
    }



}
