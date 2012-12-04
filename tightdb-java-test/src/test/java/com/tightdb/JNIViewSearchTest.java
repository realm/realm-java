package com.tightdb;

import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

public class JNIViewSearchTest {

	@Test(enabled = true)
	public void shouldSearchByColumnValue() {
		TableBase table = new TableBase();

		TableSpec tableSpec = new TableSpec();
		tableSpec.addColumn(ColumnType.ColumnTypeString, "name");
		table.updateFromSpec(tableSpec);

		table.insertString(0, 0, "Foo");
		table.insertDone();

		table.insertString(0, 1, "Bar");
		table.insertDone();

		TableQuery query = table.where();
		TableViewBase view = query.findAll(0, table.size(), Integer.MAX_VALUE);
		assertEquals(2, view.size());

		view.findAllString(0, "Foo");
	}
	
	@Test()
	public void shouldQueryInView() {
		TableBase table = new TableBase();

		TableSpec tableSpec = new TableSpec();
		tableSpec.addColumn(ColumnType.ColumnTypeString, "name");
		table.updateFromSpec(tableSpec);

		long i = 0;
		table.insertString(0, i++, "A1"); table.insertDone();
		table.insertString(0, i++, "B"); table.insertDone();
		table.insertString(0, i++, "A2"); table.insertDone();
		table.insertString(0, i++, "B"); table.insertDone();
		table.insertString(0, i++, "A3"); table.insertDone();
		table.insertString(0, i++, "B"); table.insertDone();
		table.insertString(0, i++, "A3"); table.insertDone();

		TableQuery query = table.where();
		TableViewBase view = query.beginsWith(0, "A").findAll(0, table.size(), TableBase.INFINITE);
		assertEquals(4, view.size());

		TableQuery query2 = table.where();
		TableViewBase view2 = query2.tableview(view).contains(0, "3").findAll();
		assertEquals(2, view2.size());
	}

}
