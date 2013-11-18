package com.tightdb.refdoc;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import com.tightdb.DefineTable;

public class TypedTableViewExamples {

    public static void main(String[] args) throws FileNotFoundException  {


        // TableView methods:
        sizeExample();
        isEmptyExample();
        clearExample();
        
        // Column methods:
        setAllExample();
        columnSumExample();
        columnAverageExample();
        columnMinimumExample();
        columnMaximumExample();

        // Row methods:
        getRowExample();
        removeExample();
        removeLastExample();
        
        // Cell methods
        getValueExample();
        setValueExample();

        // Searching methods
        whereExample();
        findAllExample();
        findFirstExample();
        lookupExample();
        equalToExample();
        containsExample();
        endsWithExample();

        // Dump methods:
        toJSONExample();
    }



    // ******************************************
    // Table methods
    // ******************************************


    public static void sizeExample(){
        // @@Example: ex_java_typed_table_view_size @@
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
        // @@Example: ex_java_typed_table_view_is_empty @@
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
        // @@Example: ex_java_typed_table_view_clear @@
        // @@Show@@
        // Create table and add 3 rows of data
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 

        PeopleView view = people.age.equalTo(2).findAll();
        view.clear();
        // @@EndShow@@
        // @@EndExample@@
    }


    // ******************************************
    // Column methods
    // ******************************************
    
    
    public static void setAllExample(){
        // @@Example: ex_java_typed_table_view_column_set_all @@
        // @@Show@@
        // Create table and add 3 rows of data
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 
        people.add("Laura", 31, false); 
        people.add("Eddie", 29, true); 

        // Make sure all people in the table are hired
        people.hired.setAll(true);
        
        // Make all people the same young age
        people.age.setAll(22);
        
        Assert(people.get(3).getHired() == true && people.get(3).getAge() == 22);
        // @@EndShow@@
        // @@EndExample@@
    }

    
    public static void columnSumExample(){
        // @@Example: ex_java_typed_table_view_column_sum @@
        // @@Show@@
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 
        people.add("Laura", 31, false); 
        people.add("Eddie", 29, true); 
        
        PeopleView hiredPeople = people.hired.equalTo(true).findAll();
        
        Assert(hiredPeople.age.sum() == 95);
        // @@EndShow@@
        // @@EndExample@@
    }
    
    public static void columnAverageExample(){
        // @@Example: ex_java_typed_table_view_column_average @@
        // @@Show@@
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 
        people.add("Laura", 31, false); 
        people.add("Eddie", 29, true); 
        
        PeopleView hiredPeople = people.hired.equalTo(true).findAll();
        
        Assert(hiredPeople.age.average() == 95d / 3);
        // @@EndShow@@
        // @@EndExample@@
    }
    
    public static void columnMinimumExample(){
        // @@Example: ex_java_typed_table_view_column_minimum @@
        // @@Show@@
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 
        people.add("Laura", 31, false); 
        people.add("Eddie", 29, true); 
        
        PeopleView hiredPeople = people.hired.equalTo(true).findAll();
        
        Assert(hiredPeople.age.minimum() == 26);
        // @@EndShow@@
        // @@EndExample@@
    }
    
    
    public static void columnMaximumExample(){
        // @@Example: ex_java_typed_table_view_column_maximum @@
        // @@Show@@
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false); 
        people.add("Greg", 26, true); 
        people.add("Laura", 31, false); 
        people.add("Eddie", 29, true); 
        
        PeopleView hiredPeople = people.hired.equalTo(true).findAll();
        
        Assert(hiredPeople.age.maximum() == 40);
        // @@EndShow@@
        // @@EndExample@@
    }



    // ******************************************
    // Row methods
    // ******************************************

    public static void getRowExample(){
        // @@Example: ex_java_typed_table_view_get_row @@
        // @@Show@@
        // Create table and add 2 rows of data
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false);
        people.add("Greg", 26, true);
        people.add("Laura", 31, false);
        people.add("Eddie", 29, true);
        
        PeopleView hiredPeople = people.hired.equalTo(true).findAll();

        // Use get method to access rows
        PeopleRow susan = hiredPeople.get(1);
        Assert(susan.getName().equals("Greg"));
        // @@EndShow@@
        // @@EndExample@@
    }


    public static void removeExample(){
        // @@Example: ex_java_typed_table_view_remove @@
        // @@Show@@
        // Create table and add 3 rows of data
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false);
        people.add("Greg", 26, true);
        people.add("Laura", 31, false);
        people.add("Eddie", 29, true);
        
        PeopleView hiredPeople = people.hired.equalTo(true).findAll();

        //Remove row at index 1
        hiredPeople.remove(1);

        Assert(hiredPeople.size() == 2);
        // @@EndShow@@
        // @@EndExample@@
    }


    public static void removeLastExample(){
        // @@Example: ex_java_typed_table_view_remove_last_row @@
        // @@Show@@
        // Create table and add 3 rows of data
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false);
        people.add("Greg", 26, true);
        people.add("Laura", 31, false);
        people.add("Eddie", 29, true);
        
        PeopleView hiredPeople = people.hired.equalTo(true).findAll();

        //Remove last row
        hiredPeople.removeLast();

        Assert(hiredPeople.size() == 2);
        // @@EndShow@@
        // @@EndExample@@
    }
    

    // ******************************************
    // Cell methods
    // ******************************************
    
    public static void getValueExample(){
        // @@Example: ex_java_typed_table_view_column_get_value @@
        // @@Show@@
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false);
        people.add("Greg", 26, true);
        people.add("Laura", 31, false);
        people.add("Eddie", 29, true);
        
        PeopleView hiredPeople = people.hired.equalTo(true).findAll();

        // Get the Name from row 2
        Assert(hiredPeople.get(2).getName().equals("Eddie"));
        
        // A row can also be extracted
        PeopleRow row0 = hiredPeople.get(1);
        Assert(row0.getName().equals("Greg"));
        // @@EndShow@@
        // @@EndExample@@
    }
    
    
    public static void setValueExample(){
        // @@Example: ex_java_typed_table_view_column_set_value @@
        // @@Show@@
        PeopleTable people = new PeopleTable();
        people.add("John", 40, true);
        people.add("Susan", 50, false);
        people.add("Greg", 26, true);
        people.add("Laura", 31, false);
        people.add("Eddie", 29, true);
        
        PeopleView hiredPeople = people.hired.equalTo(true).findAll();

        // Set the Name from row 2
        hiredPeople.get(2).setName("Peter");
        Assert(hiredPeople.get(2).getName().equals("Peter"));
        
        // A row can also be extracted
        PeopleRow row0 = hiredPeople.get(0);
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
