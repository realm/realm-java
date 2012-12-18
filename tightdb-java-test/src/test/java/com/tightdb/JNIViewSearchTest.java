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

		table.add("Foo");
		table.add("Bar");

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

		table.add("A1");
		table.add("B");
		table.add("A2");
		table.add("B");
		table.add("A3");
		table.add("B");
		table.add("A3");

		TableQuery query = table.where();
		TableViewBase view = query.beginsWith(0, "A").findAll(0, table.size(), TableBase.INFINITE);
		assertEquals(4, view.size());

		TableQuery query2 = table.where();
		TableViewBase view2 = query2.tableview(view).contains(0, "3").findAll();
		assertEquals(2, view2.size());
	}

}
