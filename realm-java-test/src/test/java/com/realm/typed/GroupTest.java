package com.realm.typed;

import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.io.File;

import com.realm.ColumnType;
import com.realm.Group;
import com.realm.Group.OpenMode;
import com.realm.Table;
import com.realm.test.TestEmployeeTable;

@SuppressWarnings("unused")
public class GroupTest {

    protected static final String NAME0 = "John";
    protected static final String NAME1 = "Nikolche";
    protected static final String NAME2 = "Johny";

    protected static final String FILENAME = "test_no_overwrite.realm";

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
        table1.addColumn(ColumnType.STRING, "col");
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

    @Test
    public void getEmptyTableShouldThrowIllegalArgument() {
        Group group = new Group();
        try { group.getTable(null); fail("null String name"); } catch (IllegalArgumentException e) { }
        try { group.getTable(""); fail("Empty String name"); } catch (IllegalArgumentException e) { }

    }

    //
    // Open Group with file
    //

    @Test
    public void shouldOpenExistingGroupFile() throws IOException {
        new File(FILENAME).delete();

        Group group = new Group();
        group.writeToFile(FILENAME);
        group.close();

        Group group2 = new Group(FILENAME);
        group2.close();

        Group group3 = new Group(FILENAME, OpenMode.READ_ONLY);
        group3.close();

        Group group4 = new Group(FILENAME, OpenMode.READ_WRITE);
        group4.close();

        Group group5 = new Group(FILENAME, OpenMode.READ_WRITE_NO_CREATE);
        group5.close();
    }


    @Test
    public void shouldThrowExceptionOnGroupReadOnly() throws IOException {
        new File(FILENAME).delete();
        // Throw when opening non-existing file
        try { Group group = new Group(FILENAME, OpenMode.READ_ONLY); fail("Group is read only"); } catch (com.realm.IOException e) { }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionOnOpenWrongFileReadOnly() throws IOException {
        File file = new File(FILENAME);
        file.delete();
        file.createNewFile();
        // Throw when opening non-TightDB file
        Group group = new Group(FILENAME, OpenMode.READ_ONLY);
    }

    @Test
    public void ThrowExceptionOnGroupNoCreate() throws IOException {
        new File(FILENAME).delete();
        try { Group group2 = new Group(FILENAME, OpenMode.READ_WRITE_NO_CREATE); fail("Exception excpected"); } catch (com.realm.IOException e) { }
    }

    @Test
    public void groupCanWriteToFile() throws IOException {
        new File(FILENAME).delete();

        Group group = new Group();
        group.writeToFile(FILENAME);
        group.close();
        // TODO: How can we verify that group is closed?
    }
    
    @Test
    public void testReadOnlyGroup() throws IOException {
        String fileName = "db-name.realm";
        new File(fileName).delete();
        Group g1 = new Group();
        g1.getTable("table1");
        g1.writeToFile(fileName);
        
        Group g2 = new Group(fileName, OpenMode.READ_ONLY);
        try { g2.getTable("newTable"); fail("Group read-only"); } catch (IllegalStateException e ) { }
        assertEquals(1, g2.size()); // Only the 1 table from g1 should be there
    }

    @Test(expectedExceptions = com.realm.IOException.class)
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


    @Test(expectedExceptions = com.realm.IOException.class)
    public void groupCanWriteToFile2() throws IOException {
        new File(FILENAME).delete();
        Group group = new Group(FILENAME); // File is deleted, should not be able to open group on non-existing file using a string path
    }

    @Test(expectedExceptions = com.realm.IOException.class)
    public void groupCanWriteToFile3() throws IOException {
        File file = new File(FILENAME);
        file.delete();

        Group group = new Group(file); // File is deleted, should not be able to open group on non-existing file using a file object
    }

    @Test
    public void groupNoOverwrite1() throws IOException {
        new File(FILENAME).delete();

        Group group = new Group();
        group.writeToFile(FILENAME);
        // writing to the same file should throw exception
        try { group.writeToFile(FILENAME); fail("writing to same file"); } catch (com.realm.IOException e) { }
    }

    @Test
    public void shouldCommitToDisk() throws IOException {
        new File(FILENAME).delete();

        // Write a DB to file
        Group group = new Group(FILENAME, OpenMode.READ_WRITE);
        group.commit();

        Table tbl = group.getTable("test");
        tbl.addColumn(ColumnType.INTEGER, "number");
        tbl.add(1);
        group.commit();
        assertEquals(tbl.getLong(0, 0), 1);

        // Update, commit and close file.
        tbl.set(0, 27);
        group.commit();
        group.close();

        // Open file again and verify content
        Group readGrp = new Group(FILENAME);
        Table tbl2 = readGrp.getTable("test");
        assertEquals(tbl2.getLong(0, 0), 27);
        readGrp.close();
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



    @Test
    public void groupByteArrayChecksForDatabaseFormat() {

        ByteBuffer nullBugger = null;
        try { Group group = new Group(nullBugger); fail("null buffer"); } catch (IllegalArgumentException e) { }

        ByteBuffer wrongBuffer = ByteBuffer.allocateDirect(5);
        try { Group group = new Group(wrongBuffer); fail("wrong buffer format"); } catch (IllegalArgumentException e) { }

        byte[] nullByte = null;
        try { Group group = new Group(nullByte); fail("null byte array"); } catch (IllegalArgumentException e) { }

        byte[] wrongByteArray = new byte[] {1,2,3,4,5};
        try { Group group = new Group(wrongByteArray); fail("wrong byte array format"); } catch (IllegalArgumentException e) { }
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

    @Test
    public void shouldCompareGroups() {
        Group group1 = new Group();
        Table tbl = group1.getTable("test");
        tbl.addColumn(ColumnType.INTEGER, "number");
        tbl.add(1);

        Group group2 = new Group();
        Table tbl2 = group2.getTable("test");
        tbl2.addColumn(ColumnType.INTEGER, "number");
        tbl2.add(1);

        assertEquals(true, group1.equals(group2));

        tbl2.add(2);
        assertEquals(false, group1.equals(group2));
    }


    @Test
    public void shouldFailWhenModifyingTablesOnClosedGroup() {
        Group group = new Group();
        Table tbl = group.getTable("test");
        tbl.addColumn(ColumnType.INTEGER, "number");
        tbl.add(1);

        //Close the group
        group.close();

        //Try to add data to table in group
        try{ tbl.add(2); fail("Group is closed"); } catch (IllegalStateException e) { }
    }


    @Test
    public void shouldFailWhenAddingTablesToClosedGroup() {
        Group group = new Group();
        Table tbl = group.getTable("test");
        tbl.addColumn(ColumnType.INTEGER, "number");
        tbl.add(1);

        //Close the group
        group.close();

        //Try to add data to table in group
        try { Table newTable = group.getTable("test2"); fail("Group closed"); } catch (IllegalStateException e) { }
    }


    @Test
    public void shouldFailWhenGettingValuesFromTablesInClosedGroup() {
        Group group = new Group();
        Table tbl = group.getTable("test");
        tbl.addColumn(ColumnType.INTEGER, "number");
        tbl.add(1);

        //Close the group
        group.close();

        try { tbl.getLong(0, 0); fail("group closed"); } catch (IllegalStateException e) { }
    }
}
