package com.tightdb;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.util.Date;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tightdb.test.TestEmployeeTable;

public class JNITransactions {

    @Table(row="Employee")
    class employee {
        String firstName;
        String lastName;
        int salary;
        boolean driver;
    }

    protected SharedGroup db;

    protected String testFile = "transact.tightdb";
    
    protected void deleteFile(String filename)
    {
    	File f = new File(filename);
    	if (f.exists())
    		f.delete();
    }
    
	@BeforeMethod
	public void init() {
		deleteFile(testFile);
		db = new SharedGroup(testFile);
	}

	//@AfterMethod
	public void clear() {
		db.close();
		deleteFile(testFile);
	}

	protected void checkRead(int rows)
	{
		// Read from table
		ReadTransaction t = db.beginRead();
    	EmployeeTable employees = new EmployeeTable(t);
    	assertEquals(true, employees.isValid());
    	assertEquals(rows, employees.size());
    	t.endRead();
	}

	protected void writeOneTransaction()
	{
		{	
	        WriteTransaction t = db.beginWrite();
	        EmployeeTable employees = new EmployeeTable(t);
	        employees.add("John", "Doe", 10000, true);
	        System.out.println("Table name:" + employees.getName() );
			assertEquals(1, employees.size());
	        t.commit();
			// assertEquals(1, employees.size()); must set exception as employees is invalid now.
		}
	}
	
	@Test
	public void mustWriteCommit() {
		writeOneTransaction();
		
		checkRead(1);
		
		clear();
	}

	// Test: exception at all mutable methods in TableBase, TableView,
	// Test: above in custom Typed Tables
	// TableQuery.... in ReadTransactions
	
	@Test(enabled=true)
	public void mustFailOnWriteInReadTransactions() {
		writeOneTransaction();

 		ReadTransaction t = db.beginRead(); 
 		long cnt = t.getTableCount();
 		for (int i=0; i< cnt; ++i)
 			System.out.println(i  + ":" + t.getTableName(i));
 		TableBase table = t.getTable("com.tightdb.EmployeeTable");

 		ByteBuffer buf = ByteBuffer.allocate(1);
		try { table.insertBoolean(0, 0, false); assert(false);} catch (IllegalStateException e) {}		
		try { table.addEmptyRow(); assert(false);} catch (IllegalStateException e) {}
		try { table.addEmptyRows(1); assert(false);} catch (IllegalStateException e) {}
		try { table.addLong(0,0);	assert(false);} catch (IllegalStateException e) {}
		try { table.clear();	assert(false);} catch (IllegalStateException e) {}
		try { table.clearSubTable(0,0);	assert(false);} catch (IllegalStateException e) {}
		try { table.insertBinary(0,0,new byte[0]);	assert(false);} catch (IllegalStateException e) {}
		try { table.insertBinary(0,0,buf);	assert(false);} catch (IllegalStateException e) {}
		try { table.insertBoolean(0,0,true);	assert(false);} catch (IllegalStateException e) {}
		try { table.insertDate(0,0,new Date(0));	assert(false);} catch (IllegalStateException e) {}
		try { table.insertDone();	assert(false);} catch (IllegalStateException e) {}
		try { table.insertLong(0,0,0);	assert(false);} catch (IllegalStateException e) {}
		try { table.insertMixed(0,0,null);	assert(false);} catch (IllegalStateException e) {}
		try { table.insertString(0,0,"");	assert(false);} catch (IllegalStateException e) {}
		try { table.insertSubTable(0,0);	assert(false);} catch (IllegalStateException e) {}
		try { table.optimize();	assert(false);} catch (IllegalStateException e) {}
		try { table.remove(0);	assert(false);} catch (IllegalStateException e) {}
		try { table.removeLast();	assert(false);} catch (IllegalStateException e) {}
		try { table.setBinaryByteArray(0,0,new byte[0]);	assert(false);} catch (IllegalStateException e) {}
		try { table.setBinaryByteBuffer(0,0,buf);	assert(false);} catch (IllegalStateException e) {}
		try { table.setBoolean(0,0,false);	assert(false);} catch (IllegalStateException e) {}
		try { table.setDate(0,0,new Date(0));	assert(false);} catch (IllegalStateException e) {}
		try { table.setIndex(0);	assert(false);} catch (IllegalStateException e) {}
		try { table.setLong(0,0,0);	assert(false);} catch (IllegalStateException e) {}
		try { table.setMixed(0,0,null);	assert(false);} catch (IllegalStateException e) {}
		try { table.setString(0,0,"");	assert(false);} catch (IllegalStateException e) {}
		try { table.updateFromSpec(null);	assert(false);} catch (IllegalStateException e) {}
//		try { table.();	assert(false);} catch (IllegalStateException e) {}
		
		t.endRead();
		clear();
	}


/* 
	@Test(enabled=true)
	public void mustReadARM() {
		// Write to DB
		{	
	        WriteTransaction t = db.beginWrite();
	        EmployeeTable employees = new EmployeeTable(t);
	        employees.add("John", "Doe", 10000, true);
			assertEquals(1, employees.size());
	        t.commit();
			// assertEquals(1, employees.size()); must set exception as employees is invalid now.
		}
	
		// Read from table
		System.out.println("mustReadARM.");
		try (ReadTransaction t = new ReadTransaction(db)) {
	    	EmployeeTable employees = new EmployeeTable(t);
	    	assertEquals(true, employees.isValid());
	    	assertEquals(1, employees.size());
		} 
		catch (Throwable e) {
		
		}
	}
*/
	
	// Test: Read fails if nothing has been written!!!
	// Test: exception at all mutable methods in TableBase, TableView, TableQuery.... in ReadTransactions
	// Test: rollback()
	// Test: error-handling, exceptions

	
	@Test
	public void mustWriteCommit2() {
	    try {
	    	// Write to DB
	        WriteTransaction wt = db.beginWrite();
	        try {
	            EmployeeTable employees = new EmployeeTable(wt);
	            employees.clear();
	            employees.add("John", "Doe", 10000, true);
	    		assertEquals(1, employees.size());
	            wt.commit();
	    		// assertEquals(1, employees.size()); must set exception as employees is invalid now.
	        }
	        catch (Throwable e) {
	            wt.rollback();
	            throw new RuntimeException(e);
	        }

	        // Read from DB
	        ReadTransaction rt = db.beginRead();
		    try {
		    	EmployeeTable employees = new EmployeeTable(rt);
		    	assertEquals(1, employees.size());
		    	rt.endRead();
		    }
		    catch (Throwable e) {
	            rt.endRead();
	            throw new RuntimeException(e);
	        }

	    }
	    finally {
			clear();
	    }	    
	}
}
