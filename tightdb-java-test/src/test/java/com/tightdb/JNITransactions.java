package com.tightdb;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.util.Date;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JNITransactions {

    protected SharedGroup db;

    protected String testFile = "transact.tightdb";

    protected void deleteFile(String filename) {
        File f = new File(filename);
        if (f.exists())
            f.delete();
        
        f = new File(filename + ".lock"); // Make sure lock file is also removed
        if (f.exists())
            f.delete();
    }

    @BeforeMethod
    public void init() {
        deleteFile(testFile);
        
        db = new SharedGroup(testFile);
    }

    @AfterMethod
    public void clear() {
        db.close();
        deleteFile(testFile);
    }

    protected void writeOneTransaction(long rows) {
        WriteTransaction trans = db.beginWrite();
        Table tbl = trans.getTable("EmployeeTable");
        TableSpec tableSpec = new TableSpec();
        tableSpec.addColumn(ColumnType.STRING, "name");
        tableSpec.addColumn(ColumnType.INTEGER, "number");
        tbl.updateFromSpec(tableSpec);


        for (long row=0; row < rows; row++)
            tbl.add("Hi", 1);
        assertEquals(rows, tbl.size());
        trans.commit();

        // must throw exception as table is invalid now.
        try {
            assertEquals(1, tbl.size());
            assert(false);
        } catch (IllegalStateException e) {
        }

    }

    protected void checkRead(int rows) {
        // Read transaction
        ReadTransaction trans = db.beginRead();
        Table tbl = trans.getTable("EmployeeTable");
        assertEquals(true, tbl.isValid());
        assertEquals(rows, tbl.size());
        trans.endRead();
    }

    @Test
    public void mustWriteAndReadEmpty() {

        writeOneTransaction(0);
        checkRead(0);
        clear();
    }

    @Test
    public void mustWriteCommit() {

        writeOneTransaction(10);
        checkRead(10);
        clear();
    }



    @Test(expectedExceptions=IllegalStateException.class)
    public void shouldThrowExceptionAfterClosedReadTransaction() {
        ReadTransaction rt = db.beginRead();

        try {
            Table tbl = rt.getTable("EmployeeTable");
            rt.endRead();
            tbl.getColumnCount(); //Should throw exception, the table is invalid when transaction has been closed
        } finally {
            rt.endRead();
            clear();
        }
    }


    @Test(expectedExceptions=IllegalStateException.class)
    public void shouldThrowExceptionAfterClosedReadTransactionWhenWriting() {
        ReadTransaction rt = db.beginRead();

        try {
            Table tbl = rt.getTable("EmployeeTable");
            rt.endRead();
            tbl.addColumn(ColumnType.STRING, "newString"); //Should throw exception, as adding a column is not allowed in read transaction
        } finally {
            rt.endRead();
            clear();
        }
    }


    @Test(expectedExceptions=IllegalStateException.class)
    public void shouldThrowExceptionWhenWritingInReadTrans() {

        ReadTransaction rt = db.beginRead();

        try {
            Table tbl = rt.getTable("newTable");  //Should throw exception, as this method creates a new table, if the table does not exists, thereby making it a mutable operation
            rt.endRead();
        } finally {
            rt.endRead();
            clear();
        }
    }


    @Test
    public void onlyOneCommit() { 
        WriteTransaction trans = db.beginWrite();

        try{

            Table tbl = trans.getTable("EmployeeTable");
            tbl.addColumn(ColumnType.STRING, "name");

            trans.commit();

            try {   trans.commit(); assert(false); } catch (IllegalStateException e){}

        } catch (Throwable t){
            trans.rollback();
        }
    }

    @Test
    public void mustRollback() {

        writeOneTransaction(1);

        WriteTransaction trans = db.beginWrite();
        Table tbl = trans.getTable("EmployeeTable");

        tbl.add("Hello", 1);
        assertEquals(2, tbl.size());
        trans.rollback();

        checkRead(1); // Only 1 row now.

        clear();
    }

    @Test()
    public void mustAllowDoubleCommitAndRollback() {
        {
            WriteTransaction trans = db.beginWrite();
            Table tbl = trans.getTable("EmployeeTable");
            tbl.addColumn(ColumnType.STRING, "name");
            tbl.addColumn(ColumnType.INTEGER, "number");

            // allow commit before any changes
            assertEquals(0, tbl.size());
            tbl.add("Hello", 1);
            trans.commit();
        }
        {
            WriteTransaction trans = db.beginWrite();
            Table tbl = trans.getTable("EmployeeTable");
            // allow double rollback
            tbl.add("Hello", 2);
            assertEquals(2, tbl.size());
            trans.rollback();
            trans.rollback();
            trans.rollback();
            trans.rollback();
        }
        {
            ReadTransaction trans = db.beginRead();
            Table tbl = trans.getTable("EmployeeTable");
            assertEquals(1, tbl.size());
            trans.endRead();
        }

        clear();
    }

    // Test: exception at all mutable methods in TableBase, TableView,
    // Test: above in custom Typed Tables
    // TableQuery.... in ReadTransactions

    @Test
    public void mustFailOnWriteInReadTransactions() {

        writeOneTransaction(1);

        ReadTransaction t = db.beginRead();
        Table table = t.getTable("EmployeeTable");

        try { table.addAt(0, 0, false);             assert(false);} catch (IllegalStateException e) {}
        try { table.add(0, false);                  assert(false);} catch (IllegalStateException e) {}
        try { table.addEmptyRow();                  assert(false);} catch (IllegalStateException e) {}
        try { table.addEmptyRows(1);                assert(false);} catch (IllegalStateException e) {}
        try { table.adjust(0,0);        assert(false);} catch (IllegalStateException e) {}
        try { table.clear();                        assert(false);} catch (IllegalStateException e) {}
        try { table.clearSubTable(0,0);             assert(false);} catch (IllegalStateException e) {}
        try { table.optimize();                     assert(false);} catch (IllegalStateException e) {}
        try { table.remove(0);                      assert(false);} catch (IllegalStateException e) {}
        try { table.removeLast();                   assert(false);} catch (IllegalStateException e) {}
        try { table.setBinaryByteArray(0,0,null);   assert(false);} catch (IllegalStateException e) {}
        try { table.setBoolean(0,0,false);          assert(false);} catch (IllegalStateException e) {}
        try { table.setDate(0,0,new Date(0));       assert(false);} catch (IllegalStateException e) {}
        try { table.setIndex(0);                    assert(false);} catch (IllegalStateException e) {}
        try { table.setLong(0,0,0);                 assert(false);} catch (IllegalStateException e) {}
        try { table.setMixed(0,0,null);             assert(false);} catch (IllegalStateException e) {}
        try { table.setString(0,0,"");              assert(false);} catch (IllegalStateException e) {}
        try { table.updateFromSpec(null);           assert(false);} catch (IllegalStateException e) {}

        TableQuery q = table.where();
        try { q.remove();                           assert(false);} catch (IllegalStateException e) {}
        try { q.remove(0,0);                        assert(false);} catch (IllegalStateException e) {}

        TableView v = q.findAll();
        try { v.adjust(0, 0);           assert(false);} catch (IllegalStateException e) {}
        try { v.clear();                            assert(false);} catch (IllegalStateException e) {}
        try { v.clearSubTable(0, 0);                assert(false);} catch (IllegalStateException e) {}
        try { v.remove(0);                          assert(false);} catch (IllegalStateException e) {}
        try { v.removeLast();                       assert(false);} catch (IllegalStateException e) {}
        try { v.setBinaryByteArray(0, 0, null);     assert(false);} catch (IllegalStateException e) {}
        try { v.setBoolean(0, 0, false);            assert(false);} catch (IllegalStateException e) {}
        try { v.setDate(0, 0, new Date());          assert(false);} catch (IllegalStateException e) {}
        try { v.setLong(0, 0, 0);                   assert(false);} catch (IllegalStateException e) {}
        try { v.setString(0,0,"");                  assert(false);} catch (IllegalStateException e) {}
        try { v.setMixed(0, 0, null);               assert(false);} catch (IllegalStateException e) {}

        t.endRead();
        clear();
    }


    /*  ARM Only works for Java 1.7 - NOT available in Android.

    @Test(enabled=true)
    public void mustReadARM() {
        writeOneTransaction(1);

        // Read from table
        // System.out.println("mustReadARM.");
        try (ReadTransaction t = new ReadTransaction(db)) {
            EmployeeTable employees = new EmployeeTable(t);
            assertEquals(true, employees.isValid());
            assertEquals(1, employees.size());
        }
        catch (Throwable e) {

        }
    }
     */
}
