package com.tightdb.refdoc;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import com.tightdb.Group;
import com.tightdb.SharedGroup;
import com.tightdb.WriteTransaction;

public class TypedTableExamples {

    public static void main(String[] args) throws FileNotFoundException  {

        // Constructor methods
        constructorPlainExample();
        constructorGroupExample();
        constructorGroupNameExample();

        // Table methods:
        isValidExample();
        sizeExample();
        isEmptyExample();
        clearExample();
        optimizeExample();
        setIndexExample();
        hasIndexExample();

        // Column methods:
        /* getColumnCountExample();
        getColumnNameExample();
        getColumnIndexExample();
        getColumnTypeExample();*/


        // Row methods:
        getExample();
        addExample();
        removeExample();
        removeLastExample();
        addEmptyRowExample();

        // Searching methods
        whereExample();

        // Dump methods:
        toJSONExample();
    }


    // ******************************************
    // Constructor methods
    // ******************************************


    public static void constructorPlainExample(){
        // @@Example: ex_java_table_constructor_plain @@
        // @@Show@@
        // Create PeopleTable as a standalone table
        PeopleTable people = new PeopleTable();

        // Table operations
        people.add("Peter", 32, true);
        // @@EndShow@@
        // @@EndExample@@
    }


    public static void constructorGroupExample(){
        // @@Example: ex_java_table_constructor_group @@
        // @@Show@@
        Group group = new Group();

        // Create table in the group. 
        // The name of the table in group will be PeopleTable.class.getSimpleName
        PeopleTable people = new PeopleTable(group);
        
        Assert(group.hasTable(PeopleTable.class.getSimpleName()));
        // @@EndShow@@
        // @@EndExample@@
    }


    public static void constructorGroupNameExample(){
        // @@Example: ex_java_table_constructor_group_name @@
        // @@Show@@
        Group group = new Group();

        // Create 2 tables of same typed with different names by specifying it in constructor
        PeopleTable americans = new PeopleTable(group, "Americans");
        PeopleTable canadians = new PeopleTable(group, "Canadians");
        
        // Table operations
        americans.add("Joe", 40, true);
        // @@EndShow@@
        // @@EndExample@@
    }


    // ******************************************
    // Table methods
    // ******************************************


    public static void isValidExample(){
        // @@Example: ex_java_typed_table_is_valid @@
        // @@Show@@
        // Open a group from file
        Group fromFile = new Group( /* filepath.tightdb */);

        // Get PeopleTable from group
        PeopleTable people = new PeopleTable(fromFile);

        // Group is closed
        fromFile.close();

        if( people.isValid()) {
            long size = people.size();
        } else {
            System.out.println("Group has been closed, table is no longer valid");
        }
        // @@EndShow@@
        // @@EndExample@@
    }


    public static void sizeExample(){
        // @@Example: ex_java_typed_table_size @@
        // @@Show@@
        // Create table and add 3 rows of data
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 

        // Size is 3
        Assert(people.size() == 3);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void isEmptyExample(){
        // @@Example: ex_java_typed_table_is_empty @@
        // @@Show@@
        // Create table 
        PeopleTable people = new PeopleTable();

        // No data has been added, table is empty
        Assert(people.isEmpty());

        // Add 3 rows of data
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 

        // Table is not empty
        Assert(people.isEmpty() == false);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void clearExample(){
        // @@Example: ex_java_typed_table_clear @@
        // @@Show@@
        // Create table and add 3 rows of data
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 

        // Clear table
        people.clear();

        // Table is empty
        Assert(people.isEmpty());
        // @@EndShow@@
        // @@EndExample@@
    }


    public static void optimizeExample(){
        // @@Example: ex_java_typed_table_optimize @@
        // @@Show@@
        // Create table and add 3 rows of data
        PeopleTable people = new PeopleTable();

        for (long row=0; row<100000;row++){

            // After 1000 rows of added data, the table
            // has enough info to update the internal data structure
            if(row == 1000)
                people.optimize();

            people.add("John", 40, true);
            people.add("Susan", 50, false); 
            people.add("Greg", 26, true); 
        }
        // @@EndShow@@
        // @@EndExample@@
    }


    public static void setIndexExample(){
        // @@Example: ex_java_typed_table_set_index @@
        // @@Show@@
        // Create table and add 3 rows of data
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 

        // Set index on Name column (Only String columns are currently supported)
        people.name.setIndex();

        // Check if column has index
        Assert(people.name.hasIndex());
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void hasIndexExample(){
        // @@Example: ex_java_typed_table_has_index @@
        // @@Show@@
        // Create table and add 3 rows of data
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 

        // Set index on Name column (Only String columns are currently supported)
        people.name.setIndex();

        // Check if column has index
        Assert(people.name.hasIndex());
        // @@EndShow@@
        // @@EndExample@@
    }


    // ******************************************
    // Column methods
    // ******************************************

    /*  public static void getColumnCountExample(){
        // @@Example: ex_java_typed_table_get_column_count @@
        // @@Show@@
        PeopleTable people = new PeopleTable();

        // Get column count. This example uses the table from Table (typed) intro
        //Assert(people.getColumnCount() == 3);
        // @@EndShow@@
        // @@EndExample@@
    }


    public static void getColumnNameExample(){
        // @@Example: ex_java_typed_table_get_column_name @@
        // @@Show@@
        PeopleTable people = new PeopleTable();

        // Get column name. This example uses the table from Table (typed) intro
        //Assert(people.getColumnName(1).equals("age"));
        // @@EndShow@@
        // @@EndExample@@
    }


    public static void getColumnIndexExample(){
        // @@Example: ex_java_typed_table_get_column_index @@
        // @@Show@@
        PeopleTable people = new PeopleTable();

        // Get column index. This example uses the table from Table (typed) intro
        //Assert(people.getColumnIndex("age") == 1);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void getColumnTypeExample(){
        // @@Example: ex_java_typed_table_get_column_type @@
        // @@Show@@
        PeopleTable people = new PeopleTable();

        // Get column index. This example uses the table from Table (typed) intro
        //Assert(people.getColumnType(1).equals(ColumnType.INTEGER));
        // @@EndShow@@
        // @@EndExample@@
    }*/


    // ******************************************
    // Row methods
    // ******************************************

    public static void getExample(){
        // @@Example: ex_java_typed_table_get @@
        // @@Show@@
        // Create table and add 2 rows of data
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 

        // Use get method to access rows
        PeopleRow susan = people.get(1);
        Assert(susan.getName().equals("Susan"));

        // Can be chained to access column values directly
        Assert(people.get(0).getHired() == true);
        Assert(people.get(1).getHired() == false);
        // @@EndShow@@
        // @@EndExample@@
    }


    public static void addExample(){
        // @@Example: ex_java_typed_table_add @@
        // @@Show@@
        // Create table
        PeopleTable people = new PeopleTable();

        // Add 2 rows of data
        people.add("John", 40, true);
        people.add("Susan", 50, false); 

        Assert(people.size() == 2);
        // @@EndShow@@
        // @@EndExample@@
    }

    public static void addEmptyRowExample(){
        // @@Example: ex_java_typed_table_add_empty_row @@
        // @@Show@@
        // Create table
        PeopleTable people = new PeopleTable();

        // Add a row with default values
        people.addEmptyRow();
        Assert(people.size() == 1);

        // Add a row and set some values
        PeopleRow row = people.addEmptyRow();
        row.setName("John");
        row.setHired(true);

        // @@EndShow@@
        // @@EndExample@@
    }


    public static void removeExample(){
        // @@Example: ex_java_typed_table_remove @@
        // @@Show@@
        // Create table and add 3 rows of data
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 

        //Remove 2nd row
        people.remove(1);

        Assert(people.size() == 2);
        // @@EndShow@@
        // @@EndExample@@
    }


    public static void removeLastExample(){
        // @@Example: ex_java_typed_table_remove_last_row @@
        // @@Show@@
        // Create table and add 3 rows of data
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 

        //Remove last row
        people.removeLast();

        Assert(people.size() == 2);
        // @@EndShow@@
        // @@EndExample@@
    }


    // ******************************************
    // Searching methods
    // ******************************************


    public static void whereExample(){
        // @@Example: ex_java_typed_table_where @@
        // @@Show@@
        // Create table and add 3 rows of data
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 

        // Do a query on table
        PeopleRow susan = people.where().name.equalTo("Susan").findFirst();
        Assert(susan != null);
        Assert(susan.getAge() == 50);

        // See Query class for possible operations
        // with the Query object
        // @@EndShow@@
        // @@EndExample@@
    }


    // ******************************************
    // Dump methods
    // ******************************************

    public static void toJSONExample() throws FileNotFoundException{
        // @@Example: ex_java_typed_table_to_json @@
        // @@Show@@
        // Create table and add 2 rows of data
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 

        // Generate json output
        String json = people.toJson();

        // Print json e.g. using a printbwriter
        PrintWriter out = new PrintWriter("fromServlet");
        out.print(json);
        out.close();

        // The json should match the following:
        Assert(json.equals("[{\"name\":\"John\",\"age\":40,\"hired\":true},{\"name\":\"Susan\",\"age\":50,\"hired\":false}]"));
        // @@EndShow@@
        // @@EndExample@@
    }


    static void Assert(boolean check) {
        if (!check) {
            throw new RuntimeException();
        }
    }
} 
