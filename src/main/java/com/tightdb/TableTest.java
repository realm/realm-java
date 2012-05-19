package com.tightdb;

public class TableTest {

	public static void main(String[] args) {
		System.out.println("starting...");

		System.loadLibrary("tightdb");

		TableBase base = new TableBase();

		System.out.println("created table");

		TableSpec tableSpec = new TableSpec();
		tableSpec.addColumn(ColumnType.ColumnTypeString, "name");
		tableSpec.addColumn(ColumnType.ColumnTypeInt, "salary");
		base.updateFromSpec(tableSpec);

		System.out.println("specified structure");

		base.insertString(0, 0, "John");
		base.insertLong(1, 0, 24000);
		base.insertDone();

		System.out.println("inserted data");

		System.out.println(base.getColumnName(0));
		System.out.println(base.getColumnName(1));

		System.out.println(base.size());
		System.out.println(base.getString(0, 0));
		System.out.println(base.getLong(1, 0));

		base.removeRow(0);
	}

}
