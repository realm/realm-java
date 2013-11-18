package com.tightdb.refdoc;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import com.tightdb.DefineTable;
import com.tightdb.Group;

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
        

        // Column methods:
        setAllExample();
        setIndexExample();
        hasIndexExample();
        columnSumExample();
        columnAverageExample();
        columnMinimumExample();
        columnMaximumExample();
        

        // Row methods:
        getRowExample();
        addExample();
        removeExample();
        removeLastExample();
        addEmptyRowExample();
        
        // Cell methods
        getValueExample();
        setValueExample();

        // Searching methods
        whereExample();
        findAllExample();
        findFirstExample();
        lookupExample();
        equalToExample();
        notEqualToExample();
        greaterThanExample();
        lessThanExample();
        betweenExample();
        containsExample();
        endsWithExample();
        
        // Sort
        //getSortedViewExample();

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
            // Group has been closed, table is no longer valid
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




    // ******************************************
    // Column methods
    // ******************************************
    
    
    public static void setAllExample(){
        // @@Example: ex_java_typed_table_column_set_all @@
        // @@Show@@
        // Create table and add 3 rows of data
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 
        people.add("Laura", 31, false); 

        // Make sure all people in the table are hired
        people.hired.setAll(true);
        
        // Make all people the same young age
        people.age.setAll(22);
        
        Assert(people.get(3).getHired() == true && people.get(3).getAge() == 22);
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
    
    public static void columnSumExample(){
        // @@Example: ex_java_typed_table_column_sum @@
        // @@Show@@
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 30, false); 
        people.add("Greg", 20, true); 
        
        Assert(people.age.sum() == 90);
        // @@EndShow@@
        // @@EndExample@@
    }
    
    public static void columnAverageExample(){
        // @@Example: ex_java_typed_table_column_average @@
        // @@Show@@
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 30, false); 
        people.add("Greg", 20, true); 
        
        Assert(people.age.average() == 30.0d);
        // @@EndShow@@
        // @@EndExample@@
    }
    
    public static void columnMinimumExample(){
        // @@Example: ex_java_typed_table_column_minimum @@
        // @@Show@@
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 30, false); 
        people.add("Greg", 20, true); 
        
        Assert(people.age.minimum() == 20);
        // @@EndShow@@
        // @@EndExample@@
    }
    
    
    public static void columnMaximumExample(){
        // @@Example: ex_java_typed_table_column_maximum @@
        // @@Show@@
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 30, false); 
        people.add("Greg", 20, true); 
        
        Assert(people.age.maximum() == 40);
        // @@EndShow@@
        // @@EndExample@@
    }



    // ******************************************
    // Row methods
    // ******************************************

    public static void getRowExample(){
        // @@Example: ex_java_typed_table_get_row @@
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
    // Cell methods
    // ******************************************
    
    public static void getValueExample(){
        // @@Example: ex_java_typed_table_column_get_value @@
        // @@Show@@
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 

        // Get the Name from row 2
        Assert(people.get(2).getName().equals("Greg"));
        
        // A row can also be extracted
        PeopleRow row0 = people.get(0);
        Assert(row0.getHired() == true);
        // @@EndShow@@
        // @@EndExample@@
    }
    
    
    public static void setValueExample(){
        // @@Example: ex_java_typed_table_column_set_value @@
        // @@Show@@
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 

        // Set the Name from row 2
        people.get(2).setName("Peter");
        Assert(people.get(2).getName().equals("Peter"));
        
        // A row can also be extracted
        PeopleRow row0 = people.get(0);
        row0.setHired(false);
        Assert(row0.getHired() == false);
        // @@EndShow@@
        // @@EndExample@@
    }


    // ******************************************
    // Searching methods
    // ******************************************

    public static void whereExample(){
        // @@Example: ex_java_typed_table_where @@
        // @@Show@@
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 

        // Get a typed query from the table
        PeopleQuery query = people.where();
        // @@EndShow@@
        // @@EndExample@@
    }
    
    
    
    public static void findAllExample(){
        // @@Example: ex_java_typed_table_find_all @@
        // @@Show@@
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 
        people.add("Laura", 31, false); 
        people.add("Eddie", 29, true); 
        
        // find all returns a view with the matching results
        PeopleView notHired = people.hired.findAll(false);

        Assert(notHired.size() == 2);
        // @@EndShow@@
        // @@EndExample@@
    }
    
    
    public static void findFirstExample(){
        // @@Example: ex_java_typed_table_find_first @@
        // @@Show@@
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 
        people.add("Laura", 31, false); 
        people.add("Eddie", 29, true); 
        
        // Returns a row with the first matching result
        PeopleRow firstInLine = people.hired.findFirst(false);

        Assert(firstInLine.getName().equals("Susan"));
        // @@EndShow@@
        // @@EndExample@@
    }
    
    // @@Example: ex_java_typed_table_lookup @@
    // @@Show@@
    // Define a key-value table
    @DefineTable(table="KeyValStore")
    class KeyVal {
      String  key;
      long value;
    }
    
    public static void lookupExample(){
        // Create instance of KeyValStore
        KeyValStore kvs = new KeyValStore();
        // Put some values into the store
        for (long i=0; i<10000;i++){
            String key = "key" + i;
            long value = i*1000;
            kvs.add(key, value);
        }
        
        // Lookup row index for key49
        long rowIndex = kvs.key.lookup("key49");
        
        // Get the row and retrieve the value
        Assert(kvs.get(rowIndex).getValue() == 49000);
    }
    // @@EndShow@@
    // @@EndExample@@
    
    public static void equalToExample(){
        // @@Example: ex_java_typed_table_equal_to @@
        // @@Show@@
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 
        people.add("Laura", 31, false); 
        people.add("Eddie", 29, true); 
        
        // Returns a PeopleQuery
        PeopleQuery query = people.age.equalTo(26);
        PeopleRow result = query.findFirst();

        Assert(result.getName().equals("Greg"));
        // @@EndShow@@
        // @@EndExample@@
    }
    
    public static void notEqualToExample(){
        // @@Example: ex_java_typed_table_not_equal_to @@
        // @@Show@@
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 
        people.add("Laura", 31, false); 
        people.add("Eddie", 29, true); 
        
        // Returns a PeopleQuery
        PeopleQuery query = people.age.notEqualTo(26);
        PeopleView result = query.findAll();

        Assert(result.size() == 4);
        // @@EndShow@@
        // @@EndExample@@
    }
    
    public static void greaterThanExample(){
        // @@Example: ex_java_typed_table_greater_than @@
        // @@Show@@
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 
        people.add("Laura", 31, false); 
        people.add("Eddie", 29, true); 
        
        // Returns a PeopleQuery
        PeopleQuery query = people.age.greaterThan(30);
        PeopleView result = query.findAll();

        Assert(result.size() == 3);
        // @@EndShow@@
        // @@EndExample@@
    }
    
    public static void lessThanExample(){
        // @@Example: ex_java_typed_table_less_than @@
        // @@Show@@
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 
        people.add("Laura", 31, false); 
        people.add("Eddie", 29, true); 
        
        // Returns a PeopleQuery
        PeopleQuery query = people.age.lessThan(40);
        PeopleView result = query.findAll();

        Assert(result.size() == 3);
        // @@EndShow@@
        // @@EndExample@@
    }
    
    public static void betweenExample(){
        // @@Example: ex_java_typed_table_between @@
        // @@Show@@
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 
        people.add("Laura", 31, false); 
        people.add("Eddie", 29, true); 
        
        // Returns a PeopleQuery
        PeopleQuery query = people.age.between(30, 40);
        PeopleView result = query.findAll();

        Assert(result.size() == 2);
        // @@EndShow@@
        // @@EndExample@@
    }
    
    public static void containsExample(){
        // @@Example: ex_java_typed_table_contains @@
        // @@Show@@
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 
        people.add("Laura", 31, false); 
        people.add("Eddie", 29, true); 
        
        // Returns a PeopleQuery
        PeopleQuery query = people.name.contains("a");
        PeopleView results = query.findAll();

        Assert(results.size() == 2);
        // @@EndShow@@
        // @@EndExample@@
    }
    
    
    public static void endsWithExample(){
        // @@Example: ex_java_typed_table_ends_with @@
        // @@Show@@
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 
        people.add("Laura", 31, false); 
        people.add("Eddie", 29, true); 
        
        // Returns a PeopleQuery
        PeopleQuery query = people.name.endsWith("n");
        PeopleView results = query.findAll();

        Assert(results.size() == 2);
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
