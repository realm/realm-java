package com.tightdb;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tightdb.test.TestEmployeeTable;

public class JNITransactions {

	@Table(row = "Employee")
	class employee {
		String firstName;
		String lastName;
		int salary;
		boolean driver;
	}

	protected SharedGroup db;

	protected String testFile = "transact.tightdb";

	protected void deleteFile(String filename) {
		File f = new File(filename);
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

	protected void checkRead(int rows) {
		// Read from table
		ReadTransaction t = db.beginRead();
		EmployeeTable employees = new EmployeeTable(t);
		assertEquals(true, employees.isValid());
		assertEquals(rows, employees.size());
		t.endRead();
	}

	@Test
	public void mustWriteCommit() {
		// Write to DB
		{
			WriteTransaction t = db.beginWrite();
			EmployeeTable employees = new EmployeeTable(t);
			employees.add("John", "Doe", 10000, true);
			assertEquals(1, employees.size());
			t.commit();
			// assertEquals(1, employees.size()); must set exception as
			// employees is invalid now.
		}
		// Read from DB
		checkRead(1);

	}

	// Test: exception at all mutable methods in TableBase, TableView,
	// TableQuery.... in ReadTransactions
	@Test
	public void mustFailOnWriteInReadTransactions() {
		ReadTransaction t = db.beginRead();
		TableBase table = t.getTable("Tbl");

		// all the methods that will be tested are specified here:
		Object[][] cases = {
				{ "insertLong", 0, 0, 123 },
				{ "insertBoolean", 0, 0, true },
				// etc.
		};

		Method[] methods = table.getClass().getMethods();
		for (Method method : methods) {
			String name = method.getName();
			for (Object[] c : cases) {
				if (c[0].equals(name)) {
					// we have a match, call the method
					Object[] args = Arrays.copyOfRange(c, 1, c.length);
					
					try {
						method.invoke(table, args);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		t.endRead();
	}

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
				// assertEquals(1, employees.size()); must set exception as
				// employees is invalid now.
			} catch (Throwable e) {
				wt.rollback();
				throw new RuntimeException(e);
			}

			// Read from DB
			ReadTransaction rt = db.beginRead();
			try {
				EmployeeTable employees = new EmployeeTable(rt);
				assertEquals(1, employees.size());
				rt.endRead();
			} catch (Throwable e) {
				rt.endRead();
				throw new RuntimeException(e);
			}

		} finally {
			db.close();
		}
	}

}
