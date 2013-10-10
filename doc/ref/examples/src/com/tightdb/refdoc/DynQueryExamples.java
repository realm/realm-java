package com.tightdb.refdoc;

import com.tightdb.*;


public class DynQueryExamples {


    public static void main(String[] args) {

        DynQueryExamples examples = new DynQueryExamples();

        examples.equal();
        examples.notEqual();
        examples.greaterThan();
        examples.greaterThanOrEqual();
        examples.lessThan();
        examples.lessThanOrEqual();
        examples.between();
        examples.beginsWith();
        examples.endsWith();
        examples.contains();

        examples.group();
        //examples.subtable();

        examples.findAll();
        examples.findNext();

        examples.count();
        examples.sum();
        examples.maximum();
        examples.minimum();
        examples.average();

        examples.remove();

    }

    // ******************************************
    // Table methods
    // ******************************************



    public void equal() {
        // @@Example: ex_java_dyn_query_equals @@
        // @@Show@@
        // Create a table
        Table table = new Table();

        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");

        // Insert some values
        table.add("user1", 420, false);
        table.add("user2", 770, false);
        table.add("user3", 327, false);
        table.add("user4", 770, false);
        table.add("user5", 564, true);
        table.add("user6", 875, false);
        table.add("user7", 420, true);
        table.add("user8", 770, true);

        // Query the table
        TableView view = table.where().equalTo(1, 770).findAll();

        // @@EndShow@@
        // @@EndExample@@
    }

    public void notEqual() {
        // @@Example: ex_java_dyn_query_notEquals @@
        // @@Show@@
        // Create a table
        Table table = new Table();

        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");

        // Insert some values
        table.add("user1", 420, false);
        table.add("user2", 770, false);
        table.add("user3", 327, false);
        table.add("user4", 770, false);
        table.add("user5", 564, true);
        table.add("user6", 875, false);
        table.add("user7", 420, true);
        table.add("user8", 770, true);

        // Query the table
        TableView view = table.where().notEqualTo(1, 770).findAll();

        // @@EndShow@@
        // @@EndExample@@
    }

    public void greaterThan() {
        // @@Example: ex_java_dyn_query_greaterThan @@
        // @@Show@@
        // Create a table
        Table table = new Table();

        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");

        // Insert some values
        table.add("user1", 420, false);
        table.add("user2", 770, false);
        table.add("user3", 327, false);
        table.add("user4", 770, false);
        table.add("user5", 564, true);
        table.add("user6", 875, false);
        table.add("user7", 420, true);
        table.add("user8", 770, true);

        // Query the table
        TableView view = table.where().greaterThan(1, 564).findAll();

        // @@EndShow@@
        // @@EndExample@@
    }

    public void greaterThanOrEqual() {
        // @@Example: ex_java_dyn_query_greaterThanOrEqual @@
        // @@Show@@
        // Create a table
        Table table = new Table();

        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");

        // Insert some values
        table.add("user1", 420, false);
        table.add("user2", 770, false);
        table.add("user3", 327, false);
        table.add("user4", 770, false);
        table.add("user5", 564, true);
        table.add("user6", 875, false);
        table.add("user7", 420, true);
        table.add("user8", 770, true);

        // Query the table
        TableView view = table.where().greaterThanOrEqual(1, 564).findAll();

        // @@EndShow@@
        // @@EndExample@@
    }

    public void lessThan() {
        // @@Example: ex_java_dyn_query_lessThan @@
        // @@Show@@
        // Create a table
        Table table = new Table();

        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");

        // Insert some values
        table.add("user1", 420, false);
        table.add("user2", 770, false);
        table.add("user3", 327, false);
        table.add("user4", 770, false);
        table.add("user5", 564, true);
        table.add("user6", 875, false);
        table.add("user7", 420, true);
        table.add("user8", 770, true);

        // Query the table
        TableView view = table.where().lessThan(1, 564).findAll();

        // @@EndShow@@
        // @@EndExample@@
    }

    public void lessThanOrEqual() {
        // @@Example: ex_java_dyn_query_lessThanOrEqual @@
        // @@Show@@
        // Create a table
        Table table = new Table();

        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");

        // Insert some values
        table.add("user1", 420, false);
        table.add("user2", 770, false);
        table.add("user3", 327, false);
        table.add("user4", 770, false);
        table.add("user5", 564, true);
        table.add("user6", 875, false);
        table.add("user7", 420, true);
        table.add("user8", 770, true);

        // Query the table
        TableView view = table.where().lessThanOrEqual(1, 564).findAll();

        // @@EndShow@@
        // @@EndExample@@
    }

    public void between() {
        // @@Example: ex_java_dyn_query_between @@
        // @@Show@@
        // Create a table
        Table table = new Table();

        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");

        // Insert some values
        table.add("user1", 420, false);
        table.add("user2", 770, false);
        table.add("user3", 327, false);
        table.add("user4", 770, false);
        table.add("user5", 564, true);
        table.add("user6", 875, false);
        table.add("user7", 420, true);
        table.add("user8", 770, true);

        // Query the table
        TableView view = table.where().between(1, 420, 875).findAll();

        // @@EndShow@@
        // @@EndExample@@
    }

    public void beginsWith() {
        // @@Example: ex_java_dyn_query_beginsWith @@
        // @@Show@@
        // Create a table
        Table table = new Table();

        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");

        // Insert some values
        table.add("Arnold", 420, false);
        table.add("Jane", 770, false);
        table.add("Erik", 327, false);
        table.add("Henry", 770, false);
        table.add("Bill", 564, true);
        table.add("Janet", 875, false);

        // Query the table
        TableView view = table.where().beginsWith(0, "Jan").findAll();

        // @@EndShow@@
        // @@EndExample@@
    }

    public void endsWith() {
        // @@Example: ex_java_dyn_query_endsWith @@
        // @@Show@@
        // Create a table
        Table table = new Table();

        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");

        // Insert some values
        table.add("Arnold", 420, false);
        table.add("Jane", 770, false);
        table.add("Erik", 327, false);
        table.add("Henry", 770, false);
        table.add("Bill", 564, true);
        table.add("Janet", 875, false);

        // Query the table
        TableView view = table.where().endsWith(0, "ik").findAll();

        // @@EndShow@@
        // @@EndExample@@
    }

    public void contains() {
        // @@Example: ex_java_dyn_query_contains @@
        // @@Show@@
        // Create a table
        Table table = new Table();

        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");

        // Insert some values
        table.add("Arnold", 420, false);
        table.add("Jane", 770, false);
        table.add("Erik", 327, false);
        table.add("Henry", 770, false);
        table.add("Bill", 564, true);
        table.add("Janet", 875, false);

        // Query the table
        TableView view = table.where().contains(0, "n").findAll();

        // @@EndShow@@
        // @@EndExample@@
    }

    public void group() {
        // @@Example: ex_java_dyn_query_group @@
        // @@Show@@
        // Create a table
        Table table = new Table();

        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");

        // Insert some values
        table.add("Arnold", 420, false);
        table.add("Jane", 770, false);
        table.add("Erik", 327, false);
        table.add("Henry", 770, false);
        table.add("Bill", 564, true);
        table.add("Janet", 875, false);

        // Query the table
        TableView view = table.where().group().equalTo(0, "Erik").or().equalTo(1,770).endGroup().findAll();

        // @@EndShow@@
        // @@EndExample@@
    }

    public void subtable() {
        // @@Example: ex_java_dyn_query_subtable @@
        // @@Show@@
        // Create a table
        Table table = new Table();

        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.TABLE, "tasks");

        TableSchema tasks = table.getSubTableSchema(1);
        tasks.addColumn(ColumnType.STRING, "name");
        tasks.addColumn(ColumnType.INTEGER, "score");
        tasks.addColumn(ColumnType.BOOLEAN, "completed");

        // Insert some values
        table.add("Arnold", new Object[][] {{"task1", 120, false}, {"task2", 321, true}, {"task3", 78, false}});
        table.add("Jane", new Object[][] {{"task2", 400, true}, {"task3", 375, true}});
        table.add("Erik", new Object[][] {{"task1", 562, true}, {"task3", 14, false}});


        // Query the table
        TableView view = table.where().subTable(1).equalTo(2, true).endSubTable().findAll();

        System.out.println(view);

        // @@EndShow@@
        // @@EndExample@@
    }

    public void findAll() {
        // @@Example: ex_java_dyn_query_findAll @@
        // @@Show@@
        // Create a table
        Table table = new Table();

        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");

        // Insert some values
        table.add("Arnold", 420, false);
        table.add("Jane", 770, false);
        table.add("Erik", 327, false);
        table.add("Henry", 770, false);
        table.add("Bill", 564, true);
        table.add("Janet", 875, false);

        // Query the table with an empty filter, and return a view
        TableView view = table.where().findAll();

        // @@EndShow@@
        // @@EndExample@@
    }

    public void findNext() {
        // @@Example: ex_java_dyn_query_findNext @@
        // @@Show@@
        // Create a table
        Table table = new Table();

        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");

        // Insert some values
        table.add("Arnold", 420, false);
        table.add("Jane", 770, false);
        table.add("Erik", 327, false);
        table.add("Henry", 770, false);
        table.add("Bill", 564, true);
        table.add("Janet", 875, false);

        // Iterates through the table and sets all tasks with a score > 600 to completed
        TableQuery query = table.where().greaterThan(1, 600);

        long index = query.find();
        do {
            table.setBoolean(2, index, true);
        } while((index = query.find(index+1)) != -1);
        System.out.println(table);
        // @@EndShow@@
        // @@EndExample@@
    }

    public void count() {
        // @@Example: ex_java_dyn_query_count @@
        // @@Show@@
        // Create a table
        Table table = new Table();

        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");

        // Insert some values
        table.add("Arnold", 420, false);
        table.add("Jane", 770, false);
        table.add("Erik", 327, false);
        table.add("Henry", 770, false);
        table.add("Bill", 564, true);
        table.add("Janet", 875, false);

        // Query the table
        long amount = table.where().greaterThan(1, 450).count();

        // @@EndShow@@
        // @@EndExample@@
    }

    public void sum() {
        // @@Example: ex_java_dyn_query_sum @@
        // @@Show@@
        // Create a table
        Table table = new Table();

        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");

        // Insert some values
        table.add("Arnold", 420, false);
        table.add("Jane", 770, false);
        table.add("Erik", 327, false);
        table.add("Henry", 770, false);
        table.add("Bill", 564, true);
        table.add("Janet", 875, false);

        // Query the table
        long sum = table.where().greaterThan(1, 450).sumInt(1);

        // @@EndShow@@
        // @@EndExample@@
    }

    public void average() {
        // @@Example: ex_java_dyn_query_average @@
        // @@Show@@
        // Create a table
        Table table = new Table();

        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");

        // Insert some values
        table.add("Arnold", 420, false);
        table.add("Jane", 770, false);
        table.add("Erik", 327, false);
        table.add("Henry", 770, false);
        table.add("Bill", 564, true);
        table.add("Janet", 875, false);

        // Query the table
        double avg = table.where().greaterThan(1, 450).averageInt(1);

        // @@EndShow@@
        // @@EndExample@@
    }

    public void maximum() {
        // @@Example: ex_java_dyn_query_maximum @@
        // @@Show@@
        // Create a table
        Table table = new Table();

        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");

        // Insert some values
        table.add("Arnold", 420, false);
        table.add("Jane", 770, false);
        table.add("Erik", 327, false);
        table.add("Henry", 770, false);
        table.add("Bill", 564, true);
        table.add("Janet", 875, false);

        // Query the table
        long max = table.where().greaterThan(1, 450).maximumInt(1);

        // @@EndShow@@
        // @@EndExample@@
    }

    public void minimum() {
        // @@Example: ex_java_dyn_query_minimum @@
        // @@Show@@
        // Create a table
        Table table = new Table();

        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");

        // Insert some values
        table.add("Arnold", 420, false);
        table.add("Jane", 770, false);
        table.add("Erik", 327, false);
        table.add("Henry", 770, false);
        table.add("Bill", 564, true);
        table.add("Janet", 875, false);

        // Query the table
        long min = table.where().greaterThan(1, 450).minimumInt(1);

        // @@EndShow@@
        // @@EndExample@@
    }

    public void remove() {
        // @@Example: ex_java_dyn_query_remove @@
        // @@Show@@
        // Create a table
        Table table = new Table();

        table.addColumn(ColumnType.STRING, "username");
        table.addColumn(ColumnType.INTEGER, "score");
        table.addColumn(ColumnType.BOOLEAN, "completed");

        // Insert some values
        table.add("Arnold", 420, false);
        table.add("Jane", 770, false);
        table.add("Erik", 327, false);
        table.add("Henry", 770, false);
        table.add("Bill", 564, true);
        table.add("Janet", 875, false);

        // Query the table with and remove the resulting rows
        long rowsRemoved = table.where().greaterThan(1, 450).remove();

        // @@EndShow@@
        // @@EndExample@@
    }

}
