package com.tightdb;

public class TableTest {

	public static void main(String[] args) {
		TableBase base = new TableBase();

		base.registerColumn(ColumnType.ColumnTypeString, "name");
		base.registerColumn(ColumnType.ColumnTypeInt, "salary");

		// base.addRow() - not in the JNI TableBase?
		base.insertString(0, 1, "John");
		base.insertLong(0, 1, 24000);
		base.insertDone();

		System.out.println(base.getColumnName(0));
		System.out.println(base.getColumnName(1));

		System.out.println(base.getCount());
		System.out.println(base.getString(0, 0));
		System.out.println(base.getString(1, 0));
		
		base.removeRow(0);
	}

}
