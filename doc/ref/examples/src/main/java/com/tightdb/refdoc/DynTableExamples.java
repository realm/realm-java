package com.tightdb.refdoc;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import com.tightdb.*;
import com.tightdb.TableView.Order;

public class DynTableExamples {

    public static void main(String[] args) throws FileNotFoundException  {
        
        
        // Constructor
        constrcutorExample();


        //Table schema methods
        addColumnExample();
        removeColumnExample();
        renameColumnExample();
        getColumnCountExample();
        getColumnNameExample();
        getColumnIndexExample();
        getColumnTypeExample();

        // Table methods:
        isValidExample();
        sizeExample();
        isEmptyExample();
        clearExample();
        getSortedViewExample();
        //TODO optimizeExample();
        setIndexExample();
        hasIndexExample();


        // Columns methods: 
        adjustExample();


        // Rows methods:
        addAtExample();
        addAtExample();
        setRowExample();
        removeExample();
        removeLastExample();
        addEmptyRowExample();
        addEmptyRowsExample();


        // Cells methods:
        getExamples();
        setExamples();
        //TODO getSubtableSize();
        //TODO clearSubtable

        
        // Searching methods:
        lookupExample();
        findFirstExamples();
        findAllExample();
        getDistinctViewExample();
        whereExample();

        // Aggregates methods:
        sumExample();
        maximumExample();
        minimumExample();
        averageExample();

        // Dump methods:
        toJsonExample();
    }
    
    
    // ******************************************
    // Constructor methods
    // ******************************************
    
    
    public static void constrcutorExample(){
        // @@Example: ex_java_dyn_table_constructor_plain @@
        // Create new table in memory
        Table table = new Table();
        // @@EndExample@@
        
        
        Group group = new Group();
        // @@Example: ex_java_dyn_table_constructor_group @@
        // Get table from group. If table does not exist in the group, it is created
        Table table2 = group.getTable("tableName");
        // @@EndExample@@
    }

    
    // ******************************************
    // Table schema methods
    // ******************************************

    public static void addColumnExample(){
        // @@Example: ex_java_dyn_table_add_column_1 @@
        Table table1 = new Table();

        // Add a String column called 'name' and an int column called 'age'
        table1.addColumn(ColumnType.STRING, "name");
        table1.addColumn(ColumnType.INTEGER, "age");
        // @@EndExample@@

        // @@Example: ex_java_dyn_table_add_column_2 @@
        Table table2 = new Table();

        table2.addColumn(ColumnType.STRING, "colName0");
        table2.addColumn(ColumnType.INTEGER, "colName1"); // Converts to a long in java
        table2.addColumn(ColumnType.FLOAT, "colName2");
        table2.addColumn(ColumnType.DOUBLE, "colName3");
        table2.addColumn(ColumnType.BOOLEAN, "colName4");
        table2.addColumn(ColumnType.DATE, "colName5");
        table2.addColumn(ColumnType.BINARY, "colName6");
        table2.addColumn(ColumnType.TABLE, "colName7"); // Subtables
        table2.addColumn(ColumnType.MIXED, "colName8"); // Any of the above values
        // @@EndExample@@

        // @@Example: ex_java_dyn_table_add_column_3 @@
        Table table3 = new Table();

        table3.addColumn(ColumnType.INTEGER, "id");
        long SUBTABLE_COL_INDEX = table3.addColumn(ColumnType.TABLE, "events");

        // To add columns to the subtable use TableSchema
        TableSchema subtableSchema = table3.getSubTableSchema(SUBTABLE_COL_INDEX);

        // Now simply add columns using addColumn on the TableSchema object
        subtableSchema.addColumn(ColumnType.STRING, "desc");
        subtableSchema.addColumn(ColumnType.DATE, "date");
        // @@EndExample@@

        // @@Example: ex_java_dyn_table_add_column_4 @@
        Table table4 = new Table();

        for (long i=0; i<10000;i++){
            table4.addColumn(ColumnType.INTEGER, "col" + i);
        }
        // @@EndExample@@

        // @@Example: ex_java_dyn_table_add_column_5 @@
        Table table5 = new Table();
        
        // Although it's possible to have empty names or the same names multiple times, 
        // looking up columns based on the name, will only give you the first column 
        // with that name. 
        // But if you don't care about looking up columns by name, 
        // you don't have to set a name.
        table5.addColumn(ColumnType.STRING, "val");
        table5.addColumn(ColumnType.INTEGER, "val");
        table5.addColumn(ColumnType.DOUBLE, "val");
        table5.addColumn(ColumnType.STRING, ""); // Empty string are also allowed
        table5.addColumn(ColumnType.STRING, ""); 
        
        try { 
            table5.addColumn(ColumnType.STRING, null); // Null are not allowed as column name
        } 
        catch(NullPointerException e) { 
            table5.addColumn(ColumnType.STRING, "notNullName"); 
        } 
        // @@EndExample@@
        
        // @@Example: ex_java_dyn_table_add_column_6 @@
        Table table6 = new Table();
        table6.addColumn(ColumnType.STRING, "StringCol");
        
        // Adding 1.000.000 rows with string values to the table
        for (long l=0;l<1000000;l++){
            table6.add("s" + l);
        }
        
        // Adding a new int column. Default values in all 1.000.000 rows for that column is 0
        table6.addColumn(ColumnType.INTEGER, "IntCol");
        
        Assert(table6.getLong(1, 5000) == 0);
        // @@EndExample@@
    }

    public static void removeColumnExample(){
        // @@Example: ex_java_dyn_table_remove_column_1 @@
        // Create new table and add 3 columns
        Table table1 = new Table();
        table1.addColumn(ColumnType.INTEGER, "id");
        table1.addColumn(ColumnType.STRING, "name");
        table1.addColumn(ColumnType.STRING, "extra");

        // Remove column 'name'
        table1.removeColumn(1);

        // The column index of 'extra' has been changed
        Assert(table1.getColumnIndex("extra") == 1);
        // @@EndExample@@
        
        // @@Example: ex_java_dyn_table_remove_column_2" @@
        // Create table, with subtable and add columns
        Table table2 = new Table();
        table2.addColumn(ColumnType.INTEGER, "id");
        long subtableColIndex = table2.addColumn(ColumnType.TABLE, "events");
        
        TableSchema subtableSchema = table2.getSubTableSchema(subtableColIndex);
        subtableSchema.addColumn(ColumnType.STRING, "desc");
        long subDateColIndex = subtableSchema.addColumn(ColumnType.DATE, "date");
        
        // Remove column 'extra'
        subtableSchema.removeColumn(subDateColIndex);
        // @@EndExample@@
    }

    public static void renameColumnExample(){
        // @@Example: ex_java_dyn_table_rename_column @@
        // @@Show@@
        // Create new table and add 3 columns
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "id");
        table.addColumn(ColumnType.STRING, "name");
        table.addColumn(ColumnType.STRING, "extra");

        // Rename column 2
        table.renameColumn(2, "newName");

        // Column name in column 2 should be 'newName'
        Assert(table.getColumnName(2).equals("newName"));
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void getColumnCountExample(){
        // @@Example: ex_java_dyn_table_get_column_count @@
        // @@Show@@
        // Create new table and add 3 columns
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "id");
        table.addColumn(ColumnType.STRING, "name");
        table.addColumn(ColumnType.STRING, "extra");

        // Column count should be 3
        Assert(table.getColumnCount() == 3);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void getColumnNameExample(){
        // @@Example: ex_java_dyn_table_get_column_name @@
        // @@Show@@
        // Create new table and add 3 columns
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "id");
        table.addColumn(ColumnType.STRING, "name");
        table.addColumn(ColumnType.STRING, "extra");

        // Column name in column 2 should be 'extra'
        Assert(table.getColumnName(2).equals("extra"));
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void getColumnIndexExample(){
        // @@Example: ex_java_dyn_table_get_column_index @@
        // @@Show@@
        // Create new table and add 3 columns
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "id");
        table.addColumn(ColumnType.STRING, "name");
        table.addColumn(ColumnType.STRING, "extra");

        // Column index of column 'name' is 1
        Assert(table.getColumnIndex("name") == 1);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void getColumnTypeExample(){
        // @@Example: ex_java_dyn_table_get_column_type @@
        // @@Show@@
        // Create new table and add 3 columns
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "id");
        table.addColumn(ColumnType.STRING, "name");
        table.addColumn(ColumnType.BOOLEAN, "hidden");

        // Column type of column 2 is boolean
        Assert(table.getColumnType(2).equals(ColumnType.BOOLEAN));
        // @@EndShow@@
        // @@EndExample@@
    }


    // ******************************************
    // Table methods
    // ******************************************

    public static void isValidExample(){
        // @@Example: ex_java_dyn_table_is_valid @@
        // @@Show@@
        // Open a group from file
        Group fromFile = new Group( /* filepath.tightdb */);

        // Get table from group
        Table table = fromFile.getTable("peopleTable");

        // Group is closed
        fromFile.close();

        if( table.isValid()) {
            long size = table.size();
        } else {
            System.out.println("Group has been closed, table is no longer valid");
        }
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void sizeExample(){
        // @@Example: ex_java_dyn_table_size @@
        // @@Show@@
        // Creates table with 2 columns
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "ID");
        table.addColumn(ColumnType.STRING, "City");

        // Add data to the table
        table.add(100, "Washington");
        table.add(200, "Los Angeles");

        // 2 rows have been added
        Assert(table.size() == 2);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void isEmptyExample(){
        // @@Example: ex_java_dyn_table_is_empty @@
        // @@Show@@
        // Creates table with 2 columns
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "ID");
        table.addColumn(ColumnType.STRING, "City");

        // No data has been added to the table
        // Table is empty
        Assert(table.isEmpty());

        // Table.size is 0
        Assert(table.size() == 0);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void clearExample(){
        // @@Example: ex_java_dyn_table_clear @@
        // @@Show@@
        // Create table with 2 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "ID");
        table.addColumn(ColumnType.STRING, "City");
        table.add(100, "Washington DC");
        table.add(200, "Los Angeles");

        // Remove all rows in table
        table.clear();

        // Table.size is now 0
        Assert(table.size() == 0);
        // Table is empty
        Assert(table.isEmpty());
        // @@EndShow@@
        // @@EndExample@@
    }
    
    
    public static void getSortedViewExample(){
        // @@Example: ex_java_dyn_table_get_sorted_view @@
        // @@Show@@
        Table table = new Table();
        long USERNAME_COLUMN_INDEX = table.addColumn(ColumnType.STRING, "username");
        long SCOEW_COLUMN_INDEX    = table.addColumn(ColumnType.INTEGER, "score");
        table.add("tarzan", 420);
        table.add("ultra88", 80);
        table.add("mongo-beat", 130);
        table.add("fatbrain", 390);

        // Sorting is only supported on Integer, Boolean and Date columns!
        
        // Get a sorted table view sorted by score. Ascending by default
        TableView view = table.getSortedView(SCOEW_COLUMN_INDEX);
        Assert(view.getString(USERNAME_COLUMN_INDEX, 0).equals("ultra88"));
        
        // Sort descending
        view = table.getSortedView(SCOEW_COLUMN_INDEX, Order.descending);
        Assert(view.getString(USERNAME_COLUMN_INDEX, 0).equals("tarzan"));
    }

    public static void setIndexExample(){
        // @@Example: ex_java_dyn_table_set_index @@
        // @@Show@@
        Table table = new Table();
        long ID_COLUMN_INDEX    = table.addColumn(ColumnType.INTEGER, "ID");
        long CITY_COLUMN_INDEX  = table.addColumn(ColumnType.STRING, "City");
        long STATE_COLUMN_INDEX = table.addColumn(ColumnType.STRING, "State");
        
        // Index can be set before data is added to table.
        // Insert operations will be a little slower from this point
        table.setIndex(CITY_COLUMN_INDEX);
        
        // Add data
        table.add(100, "Washington DC", "District of Columbia");
        table.add(200, "Los Angeles", "California");

        // Index can also be set after data has been added. 
        // This might take a while if the dataset is very large
        table.setIndex(STATE_COLUMN_INDEX);

        Assert(table.hasIndex(CITY_COLUMN_INDEX));
        Assert(table.hasIndex(STATE_COLUMN_INDEX));
        // @@EndShow@@
        // @@EndExample@@
    }
    
    
    public static void hasIndexExample(){
        // @@Example: ex_java_dyn_table_has_index @@
        // @@Show@@
        Table table = new Table();
        long ID_COLUMN_INDEX    = table.addColumn(ColumnType.INTEGER, "ID");
        long CITY_COLUMN_INDEX  = table.addColumn(ColumnType.STRING, "City");
        long STATE_COLUMN_INDEX = table.addColumn(ColumnType.STRING, "State");
        
        // As default no columns in table are indexed
        Assert(table.hasIndex(CITY_COLUMN_INDEX) == false);
        
        // An index can easily be set on string columns
        table.setIndex(CITY_COLUMN_INDEX);
        
        // hasIndex returns true if an index has been set
        Assert(table.hasIndex(CITY_COLUMN_INDEX) == true);
        // @@EndShow@@
        // @@EndExample@@
    }

    // ******************************************
    // Columns methods
    // ******************************************


    public static void adjustExample(){
        // @@Example: ex_java_dyn_table_adjust @@
        // @@Show@@
        // Create table with 2 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.add("user1", 420);
        table.add("user2", 770);

        // Reward all users 100 extra points using the adjust method
        table.adjust(1, 100);

        // Check that all scores are increased by 100
        Assert(table.getLong(1, 0) == 520);
        Assert(table.getLong(1, 1) == 870);
        // @@EndShow@@
        // @@EndExample@@
    }
    

    // ******************************************
    // Rows methods
    // ******************************************

    public static void addExample(){
        // @@Example: ex_java_dyn_table_add @@
        // @@Show@@
        // Create table with 2 columns
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "ID");
        table.addColumn(ColumnType.STRING, "City");

        // Add data to the table
        table.add(100, "Washington");
        table.add(200, "Los Angeles");
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void addEmptyRowExample(){
        // @@Example: ex_java_dyn_table_add_empty_row @@
        // @@Show@@
        // Create table with 2 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "ID");
        table.addColumn(ColumnType.STRING, "City");
        table.add(100, "Washington");
        table.add(200, "Los Angeles");

        // Add an empty row with default values
        table.addEmptyRow();

        // Table.size is now 3
        Assert(table.size() == 3);

        //Default values check
        Assert(table.getLong(0, 2) == 0);
        Assert(table.getString(1, 2).equals(""));
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void addEmptyRowsExample(){
        // @@Example: ex_java_dyn_table_add_empty_rows @@
        // @@Show@@
        // Create table with 2 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "ID");
        table.addColumn(ColumnType.STRING, "City");
        table.add(100, "Washington");
        table.add(200, "Los Angeles");

        // Add 10 empty rows with default values
        table.addEmptyRows(10);

        // Table.size is now 12
        Assert(table.size() == 12);

        //Default values check
        Assert(table.getLong(0, 11) == 0);
        Assert(table.getString(1, 11).equals(""));
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void addAtExample(){
        // @@Example: ex_java_dyn_table_add_at @@
        // @@Show@@
        // Create table with 2 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "ID");
        table.addColumn(ColumnType.STRING, "City");
        table.add(100, "Washington");
        table.add(200, "Los Angeles");
        table.add(300, "New York");

        // Add a row at row index 2 and shift the subsequent rows one down
        table.addAt(2, 250, "Texas");
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void setRowExample(){
        // @@Example: ex_java_dyn_table_set_row @@
        // @@Show@@
        // Create table with 2 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "ID");
        table.addColumn(ColumnType.STRING, "City");
        table.add(100, "Washington");
        table.add(200, "Los Angeles");

        // Replaces the the first row
        table.set(0, 100, "Washington DC");

        Assert(table.getString(1, 0).equals("Washington DC"));
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void removeExample(){
        // @@Example: ex_java_dyn_table_remove @@
        // @@Show@@
        // Create table with 2 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "ID");
        table.addColumn(ColumnType.STRING, "City");
        table.add(100, "Washington");
        table.add(200, "Los Angeles");

        // Removes the the first row
        table.remove(0);

        // Table.size is now 1
        Assert(table.size() == 1);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void removeLastExample(){
        // @@Example: ex_java_dyn_table_remove_last_row @@
        // @@Show@@
        // Create table with 2 columns and add ata
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "ID");
        table.addColumn(ColumnType.STRING, "City");
        table.add(100, "Washington");
        table.add(200, "Los Angeles");
        table.add(300, "New York");
        table.add(400, "Miami");

        // Removes the the last row
        table.removeLast();

        // Table.size is now 3
        Assert(table.size() == 3);
        // @@EndShow@@
        // @@EndExample@@
    }


    // ******************************************
    // Cells methods
    // ******************************************


    public static void getExamples(){
        // @@Example: ex_java_dyn_table_get @@
        // @@Show@@
        // Create table with 2 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.add("user1", 420);
        table.add("user2", 770);

        // Get String value of cell at position 0,0
        String user1 = table.getString(0, 0);

        // Get long value value of cell at position 1,1
        // NB! Column type in tightdb is INTEGER
        // In java it is accessed by getLong(col, row);
        long score2 = table.getLong(1, 1); 

        // Check values
        Assert(user1.equals("user1"));
        Assert(score2 == 770);


        // Throws exception if column or row index is out of range
        try {
            long score = table.getLong(1, 3);
        } catch (ArrayIndexOutOfBoundsException e){
            // ...
        }

        // Throws exception if accessor method and column type do not match
        try {
            boolean bool = table.getBoolean(0, 0);
        } catch (IllegalArgumentException e){
            // ...
        }
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void setExamples(){
        // @@Example: ex_java_dyn_table_set @@
        // @@Show@@
        // Create table with 2 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.add("user1", 420);
        table.add("user2", 770);

        // Set a new username in cell at position 0,0
        table.setString(0, 0, "Liquid Droid");

        // Reset the users score to 0 in cell 1,1
        // NB. Column type in tightdb is INTEGER
        // In java it is set by setLong(col, row, value);
        table.setLong(1, 1, 0);

        // Check values
        Assert(table.getString(0, 0).equals("Liquid Droid"));
        Assert(table.getLong(1,1) == 0);


        // Throws exception if column or row index is out of range
        try {
            table.setLong(1, 50, 200);
        } catch (ArrayIndexOutOfBoundsException e){
            // ...
        }

        // Throws exception if mutator method and column type do not match
        try {
            table.setBoolean(0, 0, true);
        } catch (IllegalArgumentException e){
            // ...
        }
        // @@EndShow@@
        // @@EndExample@@
    }

    
    /*public static void getSubtableSizeExample(){ }*/

    /*public static void clearSubtableExample(){ }*/
    
    // ******************************************
    // Searching methods
    // ******************************************
    
    
    
    
    public static void lookupExample(){
        // @@Example: ex_java_dyn_table_lookup @@
        // @@Show@@
        // Create a table with 2 column, the first being a String column containing map keys
        Table tableMap = new Table();
        long KEY_COLUMN_INDEX   = tableMap.addColumn(ColumnType.STRING, "key");
        long VALUE_COLUMN_INDEX = tableMap.addColumn(ColumnType.INTEGER, "value");
        
        // Put some values into the table map
        for (long i=0; i<1000;i++){
            String key = "key" + i;
            long value = i*1000;
            tableMap.add(key, value);
        }
        
        // Lookup returns the row index of the matched key
        long keyIndex = tableMap.lookup("key49");
        
        // Use the row index to retrieve the value from other columns
        Assert(tableMap.getLong(VALUE_COLUMN_INDEX, keyIndex) == 49000);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void findFirstExamples(){
        // @@Example: ex_java_dyn_table_find_first @@
        // @@Show@@
        // Create table with 2 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");
        table.add("user1", 420, false);
        table.add("user2", 770, false);
        table.add("user3", 327, false);
        table.add("user4", 770, false);
        table.add("user5", 564, true);
        table.add("user6", 875, false);
        table.add("user7", 420, true);
        table.add("user8", 770, true);

        // Find first row index where column 2 is true
        long rowIndex = table.findFirstBoolean(2, true);

        Assert(table.getString(0, rowIndex).equals("user5"));
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void findAllExample(){
        // @@Example: ex_java_dyn_table_find_all @@
        // @@Show@@
        // Create table with 3 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");
        table.add("user1", 420, false);
        table.add("user2", 770, false);
        table.add("user3", 327, false);
        table.add("user4", 770, false);
        table.add("user5", 564, true);
        table.add("user6", 875, false);
        table.add("user7", 420, true);
        table.add("user8", 770, true);

        // Find all rows  where column 2 is true. Return a view
        TableView view = table.findAllBoolean(2, true);

        // Check that resulting view has 3 rows
        Assert(view.size() == 3);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void getDistinctViewExample(){
        // @@Example: ex_java_dyn_table_get_distinct_view @@
        // @@Show@@
        // Create table with 1 column and add data
        Table table = new Table();
        table.addColumn(ColumnType.STRING, "country");
        table.add("UK");
        table.add("UK");
        table.add("US");
        table.add("US");
        table.add("US");
        table.add("US");
        table.add("China");
        table.add("China");
        table.add("US");
        table.add("US");
        table.add("US");
        table.add("China");

        // Set index before using distinct
        table.setIndex(0);

        // Call distinct on column 0. Method return a table view
        TableView view = table.getDistinctView(0);

        // Check that resulting view has 3 rows; China, UK and US
        Assert(view.size() == 3);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void whereExample(){
        // @@Example: ex_java_dyn_table_where @@
        // @@Show@@
        // Create table with 3 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed"); // column 2
        table.add("user1", 420, false);
        table.add("user2", 770, true);
        table.add("user3", 327, false);
    
        // where() returns a Query
        TableQuery query = table.where();
        
        // You can continue to build the query
        // Here you want all the rows where
        // column 2 contains false, and column 1 > 400
        query = query.equalTo(2, false);
        query = query.greaterThan(1, 400);
        // Now execute the query, and get the results in a TableView
        TableView view = query.findAll();

        // Expect 1 match
        Assert(view.size() == 1);
        Assert(view.getString(0,0).equals("user1"));
        // @@EndShow@@
        // @@EndExample@@
    }
    
    // ******************************************
    // Aggregates methods
    // ******************************************

    public static void sumExample(){
        // @@Example: ex_java_dyn_table_sum @@
        // @@Show@@
        // Create table with 3 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");
        table.add("user1", 420, false);
        table.add("user2", 770, false);
        table.add("user3", 327, false);

        // The sum of all values in column 1
        long totalScore = table.sumInt(1);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void maximumExample(){
        // @@Example: ex_java_dyn_table_maximum @@
        // @@Show@@
        // Create table with 3 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");
        table.add("user1", 420, false);
        table.add("user2", 770, false);
        table.add("user3", 327, false);

        // The maximum score in column 1
        long maxScore = table.maximumInt(1);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void minimumExample(){
        // @@Example: ex_java_dyn_table_minimum @@
        // @@Show@@
        // Create table with 3 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");
        table.add("user1", 420, false);
        table.add("user2", 770, false);
        table.add("user3", 327, false);

        // The minimum score in column 1
        long minScore = table.minimumInt(1);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void averageExample(){
        // @@Example: ex_java_dyn_table_average @@
        // @@Show@@
        // Create table with 3 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");
        table.add("user1", 420, false);
        table.add("user2", 770, false);
        table.add("user3", 327, false);

        // The average score in column 1
        double avgScore = table.averageInt(1); // Returns a double
        // @@EndShow@@
        // @@EndExample@@
    }

    // ******************************************
    // Dump methods
    // ******************************************


    public static void toJsonExample() throws FileNotFoundException{
        // @@Example: ex_java_dyn_table_to_json @@
        // @@Show@@
        // Creates table with 2 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "ID");
        table.addColumn(ColumnType.STRING, "City");
        table.add(100, "Washington");
        table.add(200, "Los Angeles");

        String json = table.toJson();

        // Print json e.g. using a printwriter
        PrintWriter out = new PrintWriter("fromServlet");
        out.print(json);
        out.close();

        Assert(json.equals("[{\"ID\":100,\"City\":\"Washington\"},{\"ID\":200,\"City\":\"Los Angeles\"}]"));
        // @@EndShow@@
        // @@EndExample@@
    }

    static void Assert(boolean check) {
        if (!check) {
            throw new RuntimeException();
        }
    }
} 
