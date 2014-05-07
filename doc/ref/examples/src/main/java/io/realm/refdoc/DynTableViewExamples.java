package io.realm.refdoc;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import io.realm.*;

public class DynTableViewExamples {

    public static void main(String[] args) throws FileNotFoundException  {

        // View methods:
        sizeExample();
        isEmptyExample();
        clearExample();

        // Columns methods:
        getColumnCountExample();
        getColumnNameExample();
        getColumnIndexExample();
        getColumnTypeExample();
        adjustExample();

        // Rows methods:
        removeExample();
        removeLastExample();
        getSourceRowIndexExample();

        // Cells methods:
        getExamples();
        setExamples();
        //TODO getSubtableSize();
        //TODO clearSubtable

        // Searching methods:
        findFirstExamples();
        findAllExample();
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
    // View methods
    // ******************************************


    public static void sizeExample() {
        // @@Example: ex_java_dyn_view_size @@
        // @@Show@@
        // Creates table with 2 columns
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "ID");
        table.addColumn(ColumnType.STRING, "City");

        // Add data to the table
        table.add(100, "Washington");
        table.add(200, "Los Angeles");

        // Get a view of the complete unfiltered table
        TableView view = table.where().findAll();

        Assert(view.size() == 2);
        // @@EndShow@@
        // @@EndExample@@
    }


    public static void isEmptyExample() {
        // @@Example: ex_java_dyn_view_is_empty @@
        // @@Show@@
        // Creates table with 2 columns
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "ID");
        table.addColumn(ColumnType.STRING, "City");

        // Get a view of the complete unfiltered table
        TableView view = table.where().findAll();

        // No data has been added to the table
        // View is empty
        Assert(view.isEmpty());

        // Table.size is 0
        Assert(view.size() == 0);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void clearExample() {
        // @@Example: ex_java_dyn_view_clear @@
        // @@Show@@
        // Create table with 2 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "ID");
        table.addColumn(ColumnType.STRING, "City");

        // Add data to table
        table.add(100, "Washington");
        table.add(200, "Los Angeles");

        // Get a view of the complete unfiltered table
        TableView view = table.where().findAll();

        // Remove all rows in a view,
        // and therefore also the table.
        view.clear();

        // Both view and table is empty
        Assert(view.isEmpty());
        Assert(table.isEmpty());
        // @@EndShow@@
        // @@EndExample@@
    }

    // ******************************************
    // Columns methods
    // ******************************************

    public static void getColumnCountExample() {
        // @@Example: ex_java_dyn_view_get_column_count @@
        // @@Show@@
        // Create new table and add 3 columns
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "id");
        table.addColumn(ColumnType.STRING, "name");
        table.addColumn(ColumnType.STRING, "extra");

        // Get a view of the complete unfiltered table
        TableView view = table.where().findAll();

        // Column count should be 3
        Assert(view.getColumnCount() == 3);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void getColumnNameExample() {
        // @@Example: ex_java_dyn_view_get_column_name @@
        // @@Show@@
        // Create new table and add 3 columns
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "id");
        table.addColumn(ColumnType.STRING, "name");
        table.addColumn(ColumnType.STRING, "extra");

        // Get a view of the complete unfiltered table
        TableView view = table.where().findAll();

        // Column name in column 2 should be 'extra'
        Assert(view.getColumnName(2).equals("extra"));
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void getColumnIndexExample() {
        // @@Example: ex_java_dyn_view_get_column_index @@
        // @@Show@@
        // Create new table and add 3 columns
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "id");
        table.addColumn(ColumnType.STRING, "name");
        table.addColumn(ColumnType.STRING, "extra");

        // Get a view of the complete unfiltered table
        TableView view = table.where().findAll();

        // Column index of column 'name' is 1
        Assert(view.getColumnIndex("name") == 1);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void getColumnTypeExample() {
        // @@Example: ex_java_dyn_view_get_column_type @@
        // @@Show@@
        // Create new table and add 3 columns
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "id");
        table.addColumn(ColumnType.STRING, "name");
        table.addColumn(ColumnType.BOOLEAN, "hidden");

        // Get a view of the complete unfiltered table
        TableView view = table.where().findAll();

        // Column type of column 2 is boolean
        Assert(view.getColumnType(2).equals(ColumnType.BOOLEAN));
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void adjustExample() {
        // @@Example: ex_java_dyn_view_adjust @@
        // @@Show@@
        // Create table with 2 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.add("user1", 420);
        table.add("user2", 770);

        // Get a view of the complete unfiltered table
        TableView view = table.where().findAll();

        // Reward all users 100 extra points using the adjust method
        view.adjust(1, 100);

        // Check that all scores are increased by 100
        Assert(view.getLong(1, 0) == 520);
        Assert(view.getLong(1, 1) == 870);
        // @@EndShow@@
        // @@EndExample@@
    }

    // ******************************************
    // Rows methods
    // ******************************************

    public static void removeExample() {
        // @@Example: ex_java_dyn_view_remove @@
        // @@Show@@
        // Create table with 2 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "ID");
        table.addColumn(ColumnType.STRING, "City");
        table.add(100, "Washington");
        table.add(200, "Los Angeles");

        // Get a view of the complete unfiltered table
        TableView view = table.where().findAll();

        // Removes the the first row
        view.remove(0);

        // Table.size is now 1
        Assert(table.size() == 1);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void removeLastExample() {
        // @@Example: ex_java_dyn_view_remove_last_row @@
        // @@Show@@
        // Create table with 2 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "ID");
        table.addColumn(ColumnType.STRING, "City");
        table.add(100, "Washington");
        table.add(200, "Los Angeles");
        table.add(300, "New York");
        table.add(400, "Miami");

        // Get a view of the complete unfiltered table
        TableView view = table.where().findAll();

        // Removes the the last row
        view.removeLast();

        // Table.size is now 3
        Assert(table.size() == 3);
        // @@EndShow@@
        // @@EndExample@@
    }


    public static void getSourceRowIndexExample() {
        // @@Example: ex_java_dyn_view_get_source_row_index @@
        // @@Show@@
        // Create table with 3 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.STRING, "name");
        table.addColumn(ColumnType.INTEGER, "age");
        table.addColumn(ColumnType.BOOLEAN, "hired");
        table.add("John", 51, false);
        table.add("Peter", 35, false);
        table.add("Susan", 29, true);
        table.add("Greg", 33, true);

        // Create and execute query
        TableView v = table.where().equalTo(2, true).findAll();

        // Translate the view row indexes to the source table row indexes
        Assert(v.getSourceRowIndex(0) == 2);
        Assert(v.getSourceRowIndex(1) == 3);
        // @@EndShow@@
        // @@EndExample@@
    }


    // ******************************************
    // Cells methods
    // ******************************************

    public static void getExamples() {
        // @@Example: ex_java_dyn_view_get @@
        // @@Show@@
        // Create table with 2 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.add("user1", 420);
        table.add("user2", 770);

        // Get a view of the complete unfiltered table
        TableView view = table.where().findAll();

        // Get String value of cell at position 0,0
        String user1 = view.getString(0, 0);

        // Get long value value of cell at position 1,1
        // NB! Column type in realm is INTEGER
        // In java it is accessed by getLong(col, row);
        long score2 = view.getLong(1, 1);

        // Check values
        Assert(user1.equals("user1"));
        Assert(score2 == 770);

        // Throws exception if column or row index is out of range
        try {
            long score = view.getLong(1, 3);
        } catch (ArrayIndexOutOfBoundsException e){
            // ...
        }

        // Throws exception if accessor method and column type do not match
        try {
            boolean bool = view.getBoolean(0, 0);
        } catch (IllegalArgumentException e){
            // ...
        }
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void setExamples() {
        // @@Example: ex_java_dyn_view_set @@
        // @@Show@@
        // Create table with 2 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.add("user1", 420);
        table.add("user2", 770);

        // Get a view of the complete unfiltered table
        TableView view = table.where().findAll();

        // Set a new username in cell at position 0,0
        view.setString(0, 0, "Liquid Droid");

        // Reset the users score to 0 in cell 1,1
        // NB. Column type in realm is INTEGER
        // In java it is set by setLong(col, row, value);
        view.setLong(1, 1, 0);

        // Check values
        Assert(view.getString(0, 0).equals("Liquid Droid"));
        Assert(view.getLong(1,1) == 0);

        // Throws exception if column or row index is out of range
        try {
            view.setLong(1, 50, 200);
        } catch (ArrayIndexOutOfBoundsException e){
            // ...
        }

        // Throws exception if mutator method and column type do not match
        try {
            view.setBoolean(0, 0, true);
        } catch (IllegalArgumentException e){
            // ...
        }
        // @@EndShow@@
        // @@EndExample@@
    }


    /* TODO: public static void getSubtableSizeExample(){ }*/

    /* TODO: public static void clearSubtableExample(){ }*/


    // ******************************************
    // Searching methods
    // ******************************************

    public static void findFirstExamples() {
        // @@Example: ex_java_dyn_view_find_first @@
        // @@Show@@
        // Create table with 2 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");
        table.add("user1", 420, false);
        table.add("user2", 770, true);
        table.add("user3", 327, true);
        table.add("user4", 770, false);

        // Get a view of the complete unfiltered table
        TableView view = table.where().findAll();

        // Find first row index where column 2 is true
        long rowIndex = view.findFirstBoolean(2, true);

        Assert(view.getString(0, rowIndex).equals("user2"));
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void findAllExample() {
        // @@Example: ex_java_dyn_view_find_all @@
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

        // Get a view of the complete unfiltered table
        TableView view = table.where().findAll();

        // Find all rows  where column 2 is true. Return a view
        TableView viewResult = view.findAllBoolean(2, true);

        // Check that resulting view has 3 rows
        Assert(viewResult.size() == 3);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void whereExample(){
        // @@Example: ex_java_dyn_view_where @@
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

        // Query the table to get a view of all users with a score above 500
        TableView view = table.where().greaterThan(1, 500).findAll();

        // Use the query object to query the view and get a new view with the results
        TableView results = view.where().equalTo(2, false).findAll();

        // Check that resulting view has 5 rows
        Assert(results.size() == 3);
        // @@EndShow@@
        // @@EndExample@@
    }


    // ******************************************
    // Aggregates methods
    // ******************************************

    public static void sumExample() {
        // @@Example: ex_java_dyn_view_sum @@
        // @@Show@@
        // Create table with 3 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");
        table.add("user1", 420, false);
        table.add("user2", 770, false);
        table.add("user3", 327, false);

        // Get a view of the complete unfiltered table
        TableView view = table.where().findAll();

        // The sum of all values in column 1
        long totalScore = view.sumLong(1);

        Assert(totalScore == 1517);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void maximumExample() {
        // @@Example: ex_java_dyn_view_maximum @@
        // @@Show@@
        // Create table with 3 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");
        table.add("user1", 420, false);
        table.add("user2", 770, false);
        table.add("user3", 327, false);

        // Get a view of the complete unfiltered table
        TableView view = table.where().findAll();

        // The maximum score in column 1
        long maxScore = view.maximumLong(1);

        Assert(maxScore == 770);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void minimumExample() {
        // @@Example: ex_java_dyn_view_minimum @@
        // @@Show@@
        // Create table with 3 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");
        table.add("user1", 420, false);
        table.add("user2", 770, false);
        table.add("user3", 327, false);

        // Get a view of the complete unfiltered table
        TableView view = table.where().findAll();

        // The minimum score in column 1
        long minScore = view.minimumLong(1);

        Assert(minScore == 327);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void averageExample() {
        // @@Example: ex_java_dyn_view_average @@
        // @@Show@@
        // Create table with 3 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");
        table.add("user1", 420, false);
        table.add("user2", 770, false);
        table.add("user3", 325, false);

        // Get a view of the complete unfiltered table
        TableView view = table.where().findAll();

        // The average score in column 1
        double avgScore = view.averageLong(1); // Returns a double

        Assert(avgScore == 505);
        // @@EndShow@@
        // @@EndExample@@
    }

    // ******************************************
    // Dump methods
    // ******************************************

    public static void toJsonExample() throws FileNotFoundException {
        // @@Example: ex_java_dyn_view_to_json @@
        // @@Show@@
        // Creates table with 2 columns and add data
        Table table = new Table();
        table.addColumn(ColumnType.INTEGER, "ID");
        table.addColumn(ColumnType.STRING, "City");
        table.add(100, "Washington");
        table.add(200, "Los Angeles");

        // Get a view of the complete unfiltered table
        TableView view = table.where().findAll();

        String json = view.toJson();

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
