// @@Example: ex_java_typed_table_intro @@
package com.tightdb.refdoc;
import java.io.File;
import java.io.IOException;

import com.tightdb.*;

public class TypedTableIntro {


    // Define the TighDB table with columns "name", "age" and "hired"
    @DefineTable(table = "PeopleTable")
    class people {
        String  name;
        int     age;
        boolean hired;
    }

    public static void main(String[] args) {

        //Create table instance from the generated class
        PeopleTable peopleTable = new PeopleTable();

        // Add data to table
        peopleTable.add("John", 20, true);
        peopleTable.add("Mary", 21, false);
        peopleTable.add("Lars", 32, true);
        peopleTable.add("Phil", 43, false);
        peopleTable.add("Anni", 54, true); 
        
        peopleTable.get(0).getAge();  
        
        // Insert data at row index 2
        peopleTable.insert(2, "Frank", 34, true);

        if (!peopleTable.isEmpty()) {
            long s = peopleTable.size(); // s => 6
        }

        System.out.println("Size = " + peopleTable.size() + "\n");

        /****************************** GETTERS AND SETTERS **********************/

        // Get value from row 2 column Name
        String name = peopleTable.get(2).getName(); // name => "Mary"
        
        // Set the value from in row 2 column Name
        peopleTable.get(2).setName("NewName"); 

        String lastRowName = peopleTable.last().getName();  // retrieve name for last row

        // Replace entire row 4 with new values
        peopleTable.get(4).set("Eric", 50, true);

        /****************************** DATA REMOVAL *****************************/
        peopleTable.remove(2);

        System.out.println("\nRemoved row 2. Down to " + peopleTable.size() + " rows.\n");

        /****************************** ITERATION OF ALL RECORDS *****************/

        // lazy iteration over the table

        for (PeopleRow person : peopleTable) {
            System.out.println(person.getName() + " is " + person.getAge() + " years old.");
        }

        /****************************** SIMPLE QUERY *****************************/

        System.out.println("\nFound: ");
        PeopleRow p = peopleTable.name.equal("John").findFirst();
        System.out.println( p );
        // prints: "Employee {name=John, age=20, hired=true}"

        /****************************** COMPLEX QUERY ****************************/

        // Define the query
        PeopleQuery query = peopleTable
                .age.between(20, 35)    // Implicit AND with below
                .name.contains("a")     // Implicit AND with below
                .group()                // "("
                .hired.equal(true)
                .or()               // or
                .name.endsWith("y")
                .endGroup();            // ")"
        // Count matches
        PeopleView match = query.findAll();
        System.out.println(match.size() + " employee(s) match query.");

        // Take the average age of the matches
        System.out.println(match.age.sum() + " years is the sum of ages.");

        // Perform query and use the result
        for (PeopleRow person : match) {
            // ... do something with matching 'person'
        }
        System.out.println("");

        /****************************** SERIALIZE ********************************/

        System.out.println("Serialize to file:");
        new File("people.tightdb").delete(); // overwrites file if it already exists

        // Create Table in Group
        Group group = new Group();
        PeopleTable person1 = new PeopleTable(group);

        person1.add("John", 20, true);
        person1.add("Mary", 21, false);

        // Write to disk
        try {
            group.writeToFile("people.tightdb");
        } catch (IOException e) {
            // unable to write - handle...
            System.exit(1);
        }

        // Load a group from disk (and print contents)
        Group fromDisk = new Group("people.tightdb");
        PeopleTable people2 = new PeopleTable(fromDisk);

        for (PeopleRow person : people2) {
            System.out.println(person.getName() + " is " + person.getAge() + " years old");
        }

        // Write same group to memory buffer
        byte[] buffer = group.writeToMem();

        // Load a group from memory (and print contents)
        Group fromMem = new Group(buffer);
        PeopleTable people3 = new PeopleTable(fromMem);

        for (PeopleRow person : people3) {
            System.out.println(person.getName() + " is " + person.getAge() + " years old");
        }

        /****************************** TRANSACTIONS ********************************/

        System.out.println("\nTransactions:");

        // Open a shared group
        SharedGroup db = new SharedGroup("people.tightdb");

        // Write transaction:
        WriteTransaction wrtTrans = db.beginWrite();    // Start transaction
        try {
            PeopleTable person = new PeopleTable(wrtTrans);
            // Add row to table
            person.add("Bill", 53, true);
            wrtTrans.commit();                          // End transaction
        } catch (Throwable e) {
            wrtTrans.rollback();                        // or Rollback
        }

        // Read transaction:
        ReadTransaction rdTrans = db.beginRead();       // Start transaction
        try{
            PeopleTable people = new PeopleTable(rdTrans);
            for (PeopleRow person2 : people) {
                System.out.println(person2.getName() + " is " +
                        person2.getAge() + " years old");
            }
        } finally {
            rdTrans.endRead();                          // End transaction 
        }
    } 
}
//@@EndExample@@
