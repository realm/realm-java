// @@Example: ex_java_dyn_table_intro @@
package com.tightdb.refdoc;
import com.tightdb.*;

public class DynTableIntro {
    public static void main(String[] args) {
        // @@Show@@
        // Create a basic dynamic table with 3 columns: long, String, Mixed
        Table tbl = new Table();
        tbl.addColumn(ColumnType.LONG, "myInt");
        tbl.addColumn(ColumnType.STRING, "myStr");
        tbl.addColumn(ColumnType.MIXED, "myMixed");

        //
        // Add, delete and set whole Rows
        //
        // Add some data
        tbl.add(12, "hello", 2);
        tbl.add(-15, "World", "I can be different types...");
        tbl.addAt(0, 53, "I'm now first", true);     // data in order of columns
        tbl.addEmptyRow();                            // append row at end of table - default values
        tbl.set(3, 198, "TightDB", 12.345);           // set values in row 3
        tbl.remove(0);                                // remove row 0
        tbl.removeLast();                             // remove last row

        // Get and set cell values
        Assert(tbl.getLong(0,1) == -15);              // get value at column 0, row 2
        tbl.setMixed(2,  0, new Mixed("changed Long value to String"));
        // Inspect the type of Mixed value that was just added:
        Assert(tbl.getMixedType(2, 0) == ColumnType.STRING);

        // Inspect table
        Assert(tbl.size() == 2);
        Assert(tbl.isEmpty() == false);

        // Update columns
        tbl.renameColumn(0,  "myLong");               // Rename the first column
        tbl.removeColumn(1);                          // Remove the string column
        tbl.add(42, "this is the mixed column");      // We now got two columns left
        tbl.addColumn(ColumnType.DOUBLE, "myDouble");
        tbl.add(-15, "still mixed", 123.45);

        // Column introspection
        Assert(tbl.getColumnCount() == 3);
        Assert(tbl.getColumnName(0).equals("myLong"));
        Assert(tbl.getColumnIndex("myMixed") == 1);
        Assert(tbl.getColumnType(2) == ColumnType.DOUBLE);

        // Do some simple aggregations
        Assert(tbl.maximumDouble(2) == 123.45);
        Assert(tbl.sum(0) == 24);
        Assert(tbl.average(0) == 6.0);

        // Simple match search
        Assert(tbl.findFirstLong(0, -15) == 1);       // Search for -15 in column 0. returns rowIndex
        TableView view = tbl.findAllLong(0, -15);     // Find all -15 in column 0
        Assert(view.size() == 2);                     // expect 2 matches

        // For more advanced search, checkout the TableQuery Object
        TableQuery q = tbl.where();                   // Create a query on the table
        Assert(q.between(0, 0, 100).count() == 2);    // Column 0 values in range 0-100

        // Set index and get distinct values (currently only works on Strings)
        Table tbl2 = new Table();
        long strColumn = tbl2.addColumn(ColumnType.STRING, "new Strings");
        tbl2.setIndex(strColumn);
        tbl2.add("MyString");
        tbl2.add("MyString2");
        tbl2.add("MyString");
        TableView view2 = tbl2.distinct(strColumn);   // Get distinct values
        Assert(view2.size() == 2);

        // Dump table content to json format
        String json = tbl.toJson();
        System.out.println("JSON: " + json);

        //-------------------------------------------------------------------------
        // Working with sub tables
        //-------------------------------------------------------------------------

        // Note: the .addColumn() method currently doesn't support subtables
        // You have the use the older (and likely soon deprecated) TableSpec like this:

        // Define schema with 1 String column and one Subtable column with 2 columns
        TableSpec tableSpec = new TableSpec();
        tableSpec.addColumn(ColumnType.STRING,"name");
        TableSpec subSpec = tableSpec.addSubtableColumn("subtable");    // Subtable:
        subSpec.addColumn(ColumnType.STRING, "key");
        subSpec.addColumn(ColumnType.MIXED,  "value");
        // Now apply the schema to the table
        Table tbl3 = new Table();
        tbl3.updateFromSpec(tableSpec);

        // Add two rows - the first with two rows in its' subtable cell
        Object[][] sub = new Object[][] { {"firstkey", 12},
                                          {"secondkey", "hi - I'm mixed" } };
        tbl3.add("first", sub);
        tbl3.add("second", null);
        Assert(tbl3.getSubTableSize(1, 0) == 2);

        // Add some rows to the empty subtable in the second row
        Table subTbl = tbl3.getSubTable(1,1);     // Get subtable
        // Now you can work with the subtable as any other table
        subTbl.add("key1", 23);
        Assert(subTbl.getString(0, 0).equals("key1"));

        // @@EndShow@@

        System.out.println("Everything worked :-)");
    }

    static void Assert(boolean check) {
        if (!check) {
            throw new RuntimeException();
        }
    }
}
//@@EndExample@@
