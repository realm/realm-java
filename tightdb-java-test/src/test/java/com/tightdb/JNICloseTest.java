package com.tightdb;

import org.testng.annotations.Test;

public class JNICloseTest {
	
	@Test (enabled=true, expectedExceptions = IllegalArgumentException.class)
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

		//	EmployeeTable employees = new EmployeeTable(group);
		
		
	}
	
	
	
}
