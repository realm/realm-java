package com.tightdb.example;

import com.tightdb.ColumnType;
import com.tightdb.TableBase;
import com.tightdb.TableSpec;
import com.tightdb.TableViewBase;
import com.tightdb.internal.util;
import com.tightdb.lib.TightDB;

public class DynamicTableBaseEx {

	public static void main(String[] args) {
		System.out.println("starting...");
	//	util.waitForEnter();
	
		TightDB.loadLibrary();

		System.out.println("--Memusage: " + util.getNativeMemUsage());
		
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

		TableViewBase results = base.findAllLong(1, 24000);
		System.out.println("Results size: " + results.size());

		long rowIndex = base.findFirstString(0, "John");
		System.out.println("First result index: " + rowIndex);

		System.out.println("--Memusage: " + util.getNativeMemUsage());
		
		base.remove(0);
		base.clear();
	}

}
