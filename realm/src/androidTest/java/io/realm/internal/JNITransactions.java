package io.realm.internal;


import android.test.AndroidTestCase;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import io.realm.TestHelper;
import io.realm.exceptions.RealmException;

public class JNITransactions extends AndroidTestCase {
    
    String testFile;

    @Override
    protected void setUp() throws Exception {
        createDBFileName();
    }

    @Override
    public void tearDown() throws IOException {
        deleteFile(testFile);
    }

    protected void deleteFile(String filename) throws IOException {
        for (String filePath : Arrays.asList(filename, filename + ".lock")) {
            File f = new File(filePath);
            if (f.exists()) {
                boolean result = f.delete();
                if (!result) {
                    throw new java.io.IOException("Could not delete " + filePath);
                }
            }
        }
    }

    private Table getTableWithStringPrimaryKey() {
        SharedGroup db = new SharedGroup(testFile, SharedGroup.Durability.FULL, null);
        WriteTransaction trans = db.beginWrite();
        Table t = trans.getTable("TestTable");
        t.addColumn(ColumnType.STRING, "colName");
        t.setPrimaryKey("colName");
        return t;
    }

    private Table getTableWithIntegerPrimaryKey() {
        SharedGroup db = new SharedGroup(testFile, SharedGroup.Durability.FULL, null);
        WriteTransaction trans = db.beginWrite();
        Table t = trans.getTable("TestTable");
        t.addColumn(ColumnType.INTEGER, "colName");
        t.setPrimaryKey("colName");
        return t;
    }

    private void createDBFileName(){
        testFile = new File(
                this.getContext().getFilesDir(),
                System.currentTimeMillis() + "_transact.realm").toString();
    }

    protected void writeOneTransaction(SharedGroup db, long rows) {
        WriteTransaction trans = db.beginWrite();
        Table tbl = trans.getTable("EmployeeTable");
        tbl.addColumn(ColumnType.STRING, "name");
        tbl.addColumn(ColumnType.INTEGER, "number");


        for (long row=0; row < rows; row++)
            tbl.add("Hi", 1);
        assertEquals(rows, tbl.size());
        trans.commit();

        // must throw exception as table is invalid now.
        try {
            assertEquals(1, tbl.size());
            fail();
        } catch (IllegalStateException e) {
            assertNotNull(e);
        }

    }

    protected void checkRead(SharedGroup db, int rows) {
        // Read transaction
        ReadTransaction trans = db.beginRead();
        Table tbl = trans.getTable("EmployeeTable");
        assertEquals(true, tbl.isValid());
        assertEquals(rows, tbl.size());
        trans.endRead();
    }

    // TODO: tests should be done both for all Durability options

    public void testMustWriteAndReadEmpty() {
        SharedGroup db = new SharedGroup(testFile, SharedGroup.Durability.FULL, null);
        writeOneTransaction(db, 0);
        checkRead(db, 0);
    }

    public void testMustWriteCommit() {
        SharedGroup db = new SharedGroup(testFile, SharedGroup.Durability.FULL, null);
        writeOneTransaction(db, 10);
        checkRead(db, 10);
    }


    public void testShouldThrowExceptionAfterClosedReadTransaction() {
        SharedGroup db = new SharedGroup(testFile, SharedGroup.Durability.FULL, null);
        writeOneTransaction(db, 10);
        ReadTransaction rt = db.beginRead();

        try {
            Table tbl = rt.getTable("EmployeeTable");
            rt.endRead();
            try {
                tbl.getColumnCount(); //Should throw exception, the table is invalid when transaction has been closed
                fail();
            } catch (IllegalStateException e) {
                assert(false);
                //assertNotNull(e);
            }
        } finally {
            rt.endRead();
        }
    }


    public void testShouldThrowExceptionAfterClosedReadTransactionWhenWriting() {
        SharedGroup db = new SharedGroup(testFile, SharedGroup.Durability.FULL, null);
        writeOneTransaction(db, 10);
        ReadTransaction rt = db.beginRead();

        try {
            Table tbl = rt.getTable("EmployeeTable");
            rt.endRead();
            try {
                tbl.addColumn(ColumnType.STRING, "newString"); //Should throw exception, as adding a column is not allowed in read transaction
                fail();
            } catch (IllegalStateException e) {
                //assertNotNull(e);
            }
        } finally {
            rt.endRead();
        }
    }


    public void testShouldThrowExceptionWhenWritingInReadTrans() {
        SharedGroup db = new SharedGroup(testFile, SharedGroup.Durability.FULL, null);
        ReadTransaction rt = db.beginRead();

        try {
            try {
                rt.getTable("newTable");  //Should throw exception, as this method creates a new table, if the table does not exists, thereby making it a mutable operation
                fail();
            } catch (IllegalStateException e) {
                assertNotNull(e);
            }
        } finally {
            rt.endRead();
        }
    }


    public void testOnlyOneCommit() {
        SharedGroup db = new SharedGroup(testFile, SharedGroup.Durability.FULL, null);
        WriteTransaction trans = db.beginWrite();

        try {
            Table tbl = trans.getTable("EmployeeTable");
            tbl.addColumn(ColumnType.STRING, "name");
            trans.commit();
            try {
                trans.commit(); // should throw
                fail();
            } catch (IllegalStateException e){
                assertNotNull(e);
            }

        } catch (Throwable t){
            trans.rollback();
        }
    }

    public void testMustRollback() {
        SharedGroup db = new SharedGroup(testFile, SharedGroup.Durability.FULL, null);
        writeOneTransaction(db, 1);
        WriteTransaction trans = db.beginWrite();
        Table tbl = trans.getTable("EmployeeTable");

        tbl.add("Hello", 1);
        assertEquals(2, tbl.size());
        trans.rollback();

        checkRead(db, 1); // Only 1 row now.
    }

    public void testMustAllowDoubleCommitAndRollback() {
        SharedGroup db = new SharedGroup(testFile, SharedGroup.Durability.FULL, null);
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
    }

    // TODO:
    // Test: exception at all mutable methods in TableBase, TableView,
    // Test: above in custom Typed Tables
    // TableQuery.... in ReadTransactions

    public void testMustFailOnWriteInReadTransactions() {
        SharedGroup db = new SharedGroup(testFile, SharedGroup.Durability.FULL, null);

        writeOneTransaction(db, 1);

        ReadTransaction t = db.beginRead();
        Table table = t.getTable("EmployeeTable");

        try { table.addAt(0, 0, false);             fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { table.add(0, false);                  fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { table.addEmptyRow();                  fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { table.addEmptyRows(1);                fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { table.adjust(0,0);                    fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { table.clear();                        fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { table.clearSubtable(0,0);             fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { table.optimize();                     fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { table.remove(0);                      fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { table.removeLast();                   fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { table.setBinaryByteArray(0,0,null);   fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { table.setBoolean(0,0,false);          fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { table.setDate(0,0,new Date(0));       fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { table.setIndex(0);                    fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { table.setLong(0,0,0);                 fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { table.setMixed(0,0,null);             fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { table.setString(0,0,"");              fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { table.updateFromSpec(null);           fail();} catch (IllegalStateException e) {assertNotNull(e);}

        TableQuery q = table.where();
        try { q.remove();                           fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { q.remove(0,0);                        fail();} catch (IllegalStateException e) {assertNotNull(e);}

        TableView v = q.findAll();
        try { v.adjust(0, 0);                       fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { v.clear();                            fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { v.clearSubtable(0, 0);                fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { v.remove(0);                          fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { v.removeLast();                       fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { v.setBinaryByteArray(0, 0, null);     fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { v.setBoolean(0, 0, false);            fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { v.setDate(0, 0, new Date());          fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { v.setLong(0, 0, 0);                   fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { v.setString(0,0,"");                  fail();} catch (IllegalStateException e) {assertNotNull(e);}
        try { v.setMixed(0, 0, null);               fail();} catch (IllegalStateException e) {assertNotNull(e);}

        t.endRead();
    }

    // Test that primary key constraints are actually removed
    public void testRemovingPrimaryKeyRemovesConstraint() {
        SharedGroup db = new SharedGroup(testFile, SharedGroup.Durability.FULL, null);

        WriteTransaction trans = db.beginWrite();
        Table tbl = trans.getTable("EmployeeTable");
        tbl.addColumn(ColumnType.STRING, "name");
        tbl.addColumn(ColumnType.INTEGER, "number");
        tbl.setPrimaryKey("name");

        tbl.add("Foo", 42);
        try {
            tbl.add("Foo", 41);
        } catch (RealmException e1) {
            // Primary key check worked, now remove it and try again.
            tbl.setPrimaryKey("");
            try {
                tbl.add("Foo", 41);
                return;
            } catch (RealmException e2) {
                fail("Primary key not removed");
            }
        }

        fail("Primary key not enforced.");
    }

    // Test that primary key constraints are actually removed
    public void testRemovingPrimaryKeyRemovesConstraint_typeSetters() {
        SharedGroup db = new SharedGroup(testFile, SharedGroup.Durability.FULL, null);

        WriteTransaction trans = db.beginWrite();
        Table tbl = trans.getTable("EmployeeTable");
        tbl.addColumn(ColumnType.STRING, "name");
        tbl.setPrimaryKey("name");

        // Create first entry with name "foo"
        tbl.setString(0, tbl.addEmptyRow(), "Foo");

        long rowIndex = tbl.addEmptyRow();
        try {
            tbl.setString(0, rowIndex, "Foo"); // Try to create 2nd entry with name Foo
        } catch (RealmException e1) {
            tbl.setPrimaryKey(""); // Primary key check worked, now remove it and try again.
            try {
                tbl.setString(0, rowIndex, "Foo");
                return;
            } catch (RealmException e2) {
                fail("Primary key not removed");
            }
        }

        fail("Primary key not enforced.");
    }

    public void testAddEmptyRowWithPrimaryKeyWrongTypeStringThrows() {
        Table t = getTableWithStringPrimaryKey();
        try {
            t.addEmptyRowWithPrimaryKey(42);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testAddEmptyRowWithPrimaryKeyNullStringThrows() {
        Table t = getTableWithStringPrimaryKey();
        try {
            t.addEmptyRowWithPrimaryKey(null);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testAddEmptyRowWithPrimaryKeyWrongTypeIntegerThrows() {
        Table t = getTableWithIntegerPrimaryKey();
        try {
            t.addEmptyRowWithPrimaryKey("Foo");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testAddEmptyRowWithPrimaryKeyString() {
        Table t = getTableWithStringPrimaryKey();
        long rowIndex = t.addEmptyRowWithPrimaryKey("Foo");
        assertEquals(1, t.size());
        assertEquals("Foo", t.getRow(rowIndex).getString(0));
    }

    public void testAddEmptyRowWithPrimaryKeyLong() {
        Table t = getTableWithIntegerPrimaryKey();
        long rowIndex = t.addEmptyRowWithPrimaryKey(42);
        assertEquals(1, t.size());
        assertEquals(42, t.getRow(rowIndex).getLong(0));
    }

    public void testPrimaryKeyTableMigration() throws IOException {
        TestHelper.copyRealmFromAssets(getContext(), "wrong_primarykey_table.realm", "default.realm");
        String realmPath = new File(getContext().getFilesDir(), "default.realm").getAbsolutePath();
        SharedGroup db = new SharedGroup(realmPath, SharedGroup.Durability.FULL, null);
        ImplicitTransaction transaction = db.beginImplicitTransaction();
        Table table = transaction.getTable("pk");
        assertEquals(2, table.getColumnCount());
        assertEquals(ColumnType.STRING, table.getColumnType(0));
        assertEquals(ColumnType.STRING, table.getColumnType(1));
    }
}
