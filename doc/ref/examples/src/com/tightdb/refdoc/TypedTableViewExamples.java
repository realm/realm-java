

package com.tightdb.refdoc;

import com.tightdb.ColumnType;



public class TypedTableViewExamples {

    public static void main(String[] args)  {
        
        // Table methods:
        sizeExample();
        
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
    
    
    
    
    
    
    

    

    
    

    
    
    
    
    static void Assert(boolean check) {
        if (!check) {
            throw new RuntimeException();
        }
    }
} 
