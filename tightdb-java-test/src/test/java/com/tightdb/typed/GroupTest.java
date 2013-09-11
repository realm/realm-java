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
import com.tightdb.Group.OpenMode;
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

    public void testHasTable() {
        Group group = new Group();
        assertEquals(group.hasTable(null), false);
        assertEquals(group.hasTable(""), false);
        assertEquals(group.hasTable("hi"), false);
        
        Table table = group.getTable("hi");
        assertEquals(table.isValid(), true);
        assertEquals(group.hasTable("hi"), true);
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
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldOpenReadOnly() throws IOException {
    	File file = new File(FILENAME);
    	file.delete();
    	file.createNewFile();
    	// Throw when opening non-TightDB file
    	Group group = new Group(FILENAME, OpenMode.READ_ONLY);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionOnGroupReadOnly() throws IOException {
    	new File(FILENAME).delete();
    	// Throw when opening non-existing file
    	Group group = new Group(FILENAME, OpenMode.READ_ONLY);    	
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionOnOpenWrongFileReadOnly() throws IOException {
    	File file = new File(FILENAME);
    	file.delete();
    	file.createNewFile();
    	// Throw when opening non-TightDB file
    	Group group = new Group(FILENAME, OpenMode.READ_ONLY);
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void ThrowExceptionOnGroupNoCreate() throws IOException {
    	new File(FILENAME).delete();      
    	Group group2 = new Group(FILENAME, OpenMode.READ_WRITE_NO_CREATE);
    }

    @Test
    public void groupCanWriteToFile() throws IOException {
    	new File(FILENAME).delete();
    	
    	Group group = new Group();
        group.writeToFile(FILENAME);
        group.close();
        // TODO: How can we verify that group is closed?
    }

    public void groupCanWriteToFile2() throws IOException {
    	new File(FILENAME).delete();
    	
    	Group group = new Group(FILENAME);
        group.writeToFile(FILENAME);
        group.close();
        // TODO: How can we verify that group is closed?
    }

    @Test(expectedExceptions = IOException.class)
    public void groupNoOverwrite1() throws IOException {
    	new File(FILENAME).delete();

    	Group group = new Group();
        group.writeToFile(FILENAME);
        // writing to the same file should throw exception
        group.writeToFile(FILENAME);
    }

    @Test
    public void shouldCommitToDisk() throws IOException {
    	new File(FILENAME).delete();

    	// Write a DB to file
    	Group group = new Group(FILENAME, OpenMode.READ_WRITE);
    	//group.commit();
  	
      	Table tbl = group.getTable("test");
    	tbl.addColumn(ColumnType.LONG, "number");
    	tbl.add(1);
    	//group.commit();
    	assertEquals(tbl.getLong(0, 0), 1);
    	
    	// Update, commit and close file.
    	tbl.set(0, 27);
    	//group.commit();
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
    @Test(enabled = true)
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

    @Test
    public void shouldCompareGroups() {
    	Group group1 = new Group();
      	Table tbl = group1.getTable("test");
    	tbl.addColumn(ColumnType.LONG, "number");
    	tbl.add(1);

    	Group group2 = new Group();
      	Table tbl2 = group2.getTable("test");
    	tbl2.addColumn(ColumnType.LONG, "number");
    	tbl2.add(1);
    	
    	assertEquals(true, group1.equals(group2));
    	
    	tbl2.add(2);
    	assertEquals(false, group1.equals(group2));    	
    }
    
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldFailWhenModifyingTablesOnClosedGroup() {
        Group group = new Group();
        Table tbl = group.getTable("test");
        tbl.addColumn(ColumnType.LONG, "number");
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
        tbl.addColumn(ColumnType.LONG, "number");
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
        tbl.addColumn(ColumnType.LONG, "number");
        tbl.add(1);
        
        //Close the group
        group.close();
        
        tbl.getLong(0, 0);
    }

}
