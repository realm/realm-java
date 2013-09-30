package com.tightdb.typed;

import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.io.File;

import com.tightdb.ColumnType;
import com.tightdb.Group;
import com.tightdb.Table;
import com.tightdb.test.TestEmployeeTable;

@SuppressWarnings("unused")
public class GroupTest {

    protected static final String NAME0 = "John";
    protected static final String NAME1 = "Nikolche";
    protected static final String NAME2 = "Johny";

    protected static final String FILENAME = "test_no_overwrite.tightdb";

    @Test
    public void shouldCreateTablesInGroup() {
        //util.setDebugLevel(2);
        Group group = new Group();

        TestEmployeeTable employees = new TestEmployeeTable(group);
        employees.add(NAME0, "Doe", 10000, true, new byte[] { 1, 2, 3 },
                new Date(), "extra", null);
        employees.add(NAME2, "B. Good", 20000, true, new byte[] { 1, 2, 3 },
                new Date(), true, null);
        employees.insert(1, NAME1, "Hansen", 30000, false, new byte[] { 4,
                5 }, new Date(), 1234, null);

        byte[] data = group.writeToMem();

        // check table info retrieval
        assertEquals(1, group.size());
        assertEquals(TestEmployeeTable.class.getSimpleName(), group.getTableName(0));
        assertTrue(group.hasTable(TestEmployeeTable.class.getSimpleName()));
        assertFalse(group.hasTable("xxxxxx"));

        // check table retrieval
        assertEquals(employees.size(),
                group.getTable(TestEmployeeTable.class.getSimpleName()).size());
        employees.clear();
        group.close();

        // Make new group based on same data.
        Group group2 = new Group(data);
        TestEmployeeTable employees2 = new TestEmployeeTable(group2);
        assertEquals(3, employees2.size());
        assertEquals(NAME0, employees2.get(0).getFirstName());
        assertEquals(NAME1, employees2.get(1).getFirstName());
        assertEquals(NAME2, employees2.get(2).getFirstName());
        employees2.clear();
        group2.close();

        // Make new empty group
        Group group3 = new Group();
        TestEmployeeTable employees3 = new TestEmployeeTable(group3);
        assertEquals(0, employees3.size());
        employees3.clear();
        group3.close();

    }

    @Test 
    public void testHasTable() {
        Group group = new Group();
        assertEquals(group.hasTable(null), false);
        assertEquals(group.hasTable(""), false);
        assertEquals(group.hasTable("hi"), false);

        Table table = group.getTable("hi");
        assertEquals(table.isValid(), true);
        assertEquals(group.hasTable("hi"), true);
    }

    /*
     * Helper method. Returns a group populated with a single table with a single column
     */
    private Group getGroupWithTable(){
        Group group = new Group();
        Table table1 =  group.getTable("table");
        table1.addColumn(ColumnType.ColumnTypeString, "col");
        table1.add("StringValue");
        return group;
    }


    @Test 
    public void testGroupGetWrongTableIndex() {
        Group group = getGroupWithTable();
        try { 
            group.getTableName(-1); 
            fail("Should have thrown"); 
        } catch (IndexOutOfBoundsException e ) { }
        
        try { 
            group.getTableName(1000); 
            fail("Should have thrown"); 
        } 
        catch (IndexOutOfBoundsException e ) { }
    }

    @Test 
    public void testGroupEquals() {
        Group group1 = getGroupWithTable();
        Group group2 = getGroupWithTable();
        assertEquals(true, group1.equals(group2));
        Table t = group1.getTable("table");
        t.add("hej");
        assertEquals(false, group1.equals(group2));
        
        assertEquals(true, group1.equals(group1)); // Compare to itself
        assertEquals(false, group1.equals(null)); // Compare to null
        assertEquals(false, group1.equals("String")); // Compare to other object
        
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void getNullTableShouldThrowIllegalArgument() {
        Group group = new Group();
        group.getTable(null);
        // Expect to throw exception
    }
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void getEmptyTableShouldThrowIllegalArgument() {
        Group group = new Group();
        group.getTable("");
        // Expect to throw exception
    }

    @Test 
    public void shouldOpenExistingGroupFile() throws IOException {
        new File(FILENAME).delete();
        Group group = new Group();
        group.writeToFile(FILENAME);
        group.close();

        Group group2 = new Group(FILENAME);
        group2.close();
    }

    @Test
    public void groupCanWriteToFile() throws IOException {
        new File(FILENAME).delete();

        Group group = new Group();
        group.writeToFile(FILENAME);
        group.close();
        // TODO: How can we verify that group is closed?
    }


    @Test(expectedExceptions = com.tightdb.IOException.class)
    public void groupWriteToEmptyStringPath() throws IOException {

        Group group = new Group();
        group.writeToFile(""); // Empty string - exception
    }


    @Test(expectedExceptions = IllegalArgumentException.class)
    public void groupWriteToNullStringPath() throws IOException {

        Group group = new Group();
        String path = null;
        group.writeToFile(path); // String is null - exception
    }


    @Test(expectedExceptions = com.tightdb.IOException.class)
    public void groupCanWriteToFile2() throws IOException {
        new File(FILENAME).delete();
        Group group = new Group(FILENAME); // File is deleted, should not be able to open group on non-existing file using a string path
    }

    @Test(expectedExceptions = com.tightdb.IOException.class)
    public void groupCanWriteToFile3() throws IOException {
        File file = new File(FILENAME);
        file.delete();

        Group group = new Group(file); // File is deleted, should not be able to open group on non-existing file using a file object
    }

    @Test(expectedExceptions = com.tightdb.IOException.class)
    public void groupNoOverwrite1() throws IOException {
        new File(FILENAME).delete();

        Group group = new Group();
        group.writeToFile(FILENAME);
        // writing to the same file should throw exception
        group.writeToFile(FILENAME);
    }

    /* TODO: Enable when implemented "free" method for the data
    @Test(enabled = true)
    public void groupByteBufferCanClose() {
        Group group = new Group();
        ByteBuffer data = group.writeToByteBuffer();
        group.close();

        Group group2 = new Group(data);
        group2.close();
    }
     */
    @Test
    public void groupMemCanClose() {
        Group group = new Group();
        byte[] data = group.writeToMem();
        group.close();

        Group group2 = new Group(data);
        group2.close();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void groupByteBufferChecksForNull() {
        ByteBuffer data = null;
        Group group = new Group(data);
        // Expect to throw exception
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void groupByteBufferChecksForDatabaseFormat() {
        ByteBuffer data = ByteBuffer.allocateDirect(5);
        Group group = new Group(data);
        // Expect to throw exception
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void groupByteArrayChecksForNull() {
        byte[] data = null;
        Group group = new Group(data);
        // Expect to throw exception
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void groupByteArrayChecksForDatabaseFormat() {
        byte[] data = {1,2,3,4,5};
        Group group = new Group(data);
        // Expect to throw exception
    }


    @Test
    public void shouldThrowExceptionOnMethodCallToClosedGroup() throws IOException {
        boolean failed = false;

        new File(FILENAME).delete();
        Group group = new Group();
        group.close();

        try { group.size(); failed = true; } catch (IllegalStateException e) {}
        try { group.hasTable("hi"); failed = true; } catch (IllegalStateException e) {}
        try { group.getTableName(0); failed = true; } catch (IllegalStateException e) {}
        try { group.getTable("hi"); failed = true; } catch (IllegalStateException e) {}
        try { group.writeToFile(""); failed = true; } catch (IllegalStateException e) {}
        try { group.writeToFile(new File("hi")); failed = true; } catch (IllegalStateException e) {}
        try { group.writeToMem(); failed = true; } catch (IllegalStateException e) {}

        if (failed)
            fail("Didn't throw exception");
    }    

    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldFailWhenModifyingTablesOnClosedGroup() {
        Group group = new Group();
        Table tbl = group.getTable("test");
        tbl.addColumn(ColumnType.ColumnTypeInt, "number");
        tbl.add(1);

        //Close the group
        group.close();

        //Try to add data to table in group
        tbl.add(2);
    }


    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldFailWhenAddingTablesToClosedGroup() {
        Group group = new Group();
        Table tbl = group.getTable("test");
        tbl.addColumn(ColumnType.ColumnTypeInt, "number");
        tbl.add(1);

        //Close the group
        group.close();

        //Try to add data to table in group
        Table newTable = group.getTable("test2");
    }


    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldFailWhenGettingValuesFromTablesInClosedGroup() {
        Group group = new Group();
        Table tbl = group.getTable("test");
        tbl.addColumn(ColumnType.ColumnTypeInt, "number");
        tbl.add(1);

        //Close the group
        group.close();

        tbl.getLong(0, 0);
    }

}
