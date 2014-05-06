package com.realm.example;

import com.realm.*;
// import com.realm.internal.util;

public class DynamicTableBaseEx {

    public static void main(String[] args) {
        // System.out.println("--Memusage: " + util.getNativeMemUsage());

        Table base = new Table();
        System.out.println("created table");

        base.addColumn(ColumnType.STRING, "name");
        base.addColumn(ColumnType.INTEGER, "salary");
        base.addColumn(ColumnType.MIXED, "Whatever");
        System.out.println("specified structure");

        base.add("John", 24000, new Mixed(1));
        System.out.println("inserted data");

        System.out.println(base.getColumnName(0));
        System.out.println(base.getColumnName(1));

        System.out.println(base.size());
        System.out.println(base.getString(0, 0));
        System.out.println(base.getLong(1, 0));

        TableView results = base.findAllLong(1, 24000);
        System.out.println("Results size: " + results.size());

        long rowIndex = base.findFirstString(0, "John");
        System.out.println("First result index: " + rowIndex);

        //System.out.println("--Memusage: " + util.getNativeMemUsage());

        base.remove(0);
        base.clear();
    }

}
