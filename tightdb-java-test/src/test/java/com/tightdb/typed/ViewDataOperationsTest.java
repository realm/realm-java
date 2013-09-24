package com.tightdb.typed;

import java.util.Date;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tightdb.Mixed;
import com.tightdb.ReadTransaction;
import com.tightdb.SharedGroup;
import com.tightdb.WriteTransaction;
import com.tightdb.test.TestEmployeeQuery;
import com.tightdb.test.TestEmployeeRow;
import com.tightdb.test.TestEmployeeTable;
import com.tightdb.test.TestEmployeeView;
import com.tightdb.typed.AbstractTableOrView;

@Test
public class ViewDataOperationsTest extends AbstractDataOperationsTest {

    TestEmployeeView employees;

    @Override
    protected AbstractTableOrView<TestEmployeeRow, TestEmployeeView, TestEmployeeQuery> getEmployees() {
        return employees;
    }

    @BeforeMethod
    public void init() {
        TestEmployeeTable employeesTable = getEmployeeTable();
        employees = employeesTable.where().findAll();
    }

    @Test
    public void shouldPrint() {
        super.shouldPrintData("TestEmployeeView");
    }




    @Test
    public void setValuesOnViewInReadTransactionShouldFail() {

        SharedGroup group = new SharedGroup("viewTest.tightdb");

        // Create table if it does not exists
        WriteTransaction wt = group.beginWrite();
        try {
            TestEmployeeTable t = new TestEmployeeTable(wt);
            t.add("NoName", "Test Mixed Binary", 1, true, new byte[] { 1, 2, 3 }, new Date(), new byte[] { 3, 2, 1 },null);
            wt.commit();
        } catch (Throwable t){
            wt.rollback();
        }

        ReadTransaction rt = group.beginRead();

        try {
            TestEmployeeTable t = new TestEmployeeTable(rt);
            TestEmployeeView view = t.where().findAll();

            try {  view.get(0).firstName.set("new string");          assert(false); } catch (IllegalStateException e){ }
            try {  view.get(0).lastName.set("new last name");        assert(false); } catch (IllegalStateException e){ }
            try {  view.get(0).extra.set(new Mixed(true));           assert(false); } catch (IllegalStateException e){ }
            try {  view.get(0).birthdate.set(new Date());            assert(false); } catch (IllegalStateException e){ }
            try {  view.get(0).driver.set(false);                    assert(false); } catch (IllegalStateException e){ }
            try {  view.get(0).photo.set(null);                      assert(false); } catch (IllegalStateException e){ }

        } finally {
            rt.endRead();
        }
    }
}
