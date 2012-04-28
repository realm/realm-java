package com.tightdb;


public class TableTest {

	public static void main(String[] args) {
		System.out.println("starting...");

		TableBase base = new TableBase();
		base.registerColumn(ColumnType.ColumnTypeString, "name");
		base.registerColumn(ColumnType.ColumnTypeInt, "salary");

		base.insertString(0, 0, "John");
		base.insertLong(1, 0, 24000);
		base.insertDone();

		System.out.println(base.getColumnName(0));
		System.out.println(base.getColumnName(1));

		System.out.println(base.getCount());
		System.out.println(base.getString(0, 0));
		System.out.println(base.getLong(1, 0));

		base.removeRow(0);
	}

}
