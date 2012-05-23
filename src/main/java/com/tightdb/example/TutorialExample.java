//
// This example is used in the short introduction "Tightdb Java Interface"
//

package com.tightdb.example;

import java.io.IOException;

import com.tightdb.Group;
import com.tightdb.generated.People;
import com.tightdb.generated.PeopleQuery;
import com.tightdb.generated.PeopleTable;
import com.tightdb.lib.Table;

// @@Example: create_table @@
public class TutorialExample {
    @Table
    class people {
    	String name;
    	int age;
    	boolean hired;
    }

    public static void ShowShortExample() {
        PeopleTable peopletable = new PeopleTable();
        // ...
// @@EndExample@@

// Excuse the lacking of indentation below which is due to automatic Tutorial documentation purposes.  
        
/****************************** BASIC OPERATIONS *****************************/

// @@Example: insert_rows @@
peopletable.add("John", 20, true);
peopletable.add("Mary", 21, false);
peopletable.add("Lars", 32, true);
peopletable.add("Phil", 43, false);
peopletable.add("Anni", 53, true);
// @@EndExample@@
// @@Example: insert_at_index @@
peopletable.insert(2, "Frank", 34, true);
// @@EndExample@@

/****************************** GETTERS AND SETTERS *****************************/

// @@Example: accessing_rows @@
// 2 ways to get the value
String name = peopletable.at(2).getName(); // name => "Mary"
// or
String name2 = peopletable.at(2).name.get();

// 2 ways to set the value
peopletable.at(2).name.set("NewName");
// or
peopletable.at(2).setName("NewName"); 
// @@EndExample@@

System.out.println("at(2).getName -> " + name + " or " + name2);
System.out.println("at(2).setName('NewName') -> " + peopletable.at(2).getName());
		
// @@Example: number_of_rows @@
if (!peopletable.isEmpty()) {
    long s = peopletable.size(); // s => 6
}
// @@EndExample@@

System.out.println("Size = " + peopletable.size() + "\n");

/****************************** ITERATION OF ALL RECORDS *****************************/

// lazy iteration over the table

// @@Example: iteration @@
for (People people : peopletable) {
	System.out.println(people.getName() + " is " + people.getAge() + " years old.");
}
// @@EndExample@@

/****************************** SIMPLE QUERY ******************************/
// @@Example: simple_seach @@
People p = peopletable.name.is("John").findFirst();
// @@EndExample@@

System.out.println("\nFind 'John': " + p );

/****************************** COMPLEX QUERY *****************************/

System.out.println("\nAll with age between 20 and 30:");
// @@Example: advanced_search @@
PeopleQuery query = peopletable.age.between(20, 30);
// System.out.println(query.count());	// count and average is not yet implemented
// System.out.println(query.average());
for (People people : query.findAll()) {
    System.out.println(people.getName() + " is " + people.getAge() + " years old");
}        
// @@EndExample

/****************************** DATA REMOVAL *****************************/
// @@Example: deleting_row @@
peopletable.remove(2);
// @@EndExample@@

System.out.println("\nRemoved row 2. Down to " + peopletable.size() + " rows.");

/****************************** SERIALIZE ***************************************/

// @@Example: serialisation @@
// Create Table in Group
Group group = new Group();
PeopleTable people = new PeopleTable(group);

people.add("John", 20, true);
people.add("Mary", 21, false);
        
// Write to disk
try {
    group.writeToFile("people.tightdb");
} catch (IOException e) {
    e.printStackTrace();
}
        
// Load a group from disk (and print contents)
Group fromDisk = new Group("people.tightdb");
PeopleTable people2 = new PeopleTable(fromDisk);
       
for (People ppl : people2) {
    System.out.println(ppl.getName() + " is " + ppl.getAge() + " years old");
}

// Write same group to memory buffer
byte[] buffer = group.writeToMem();
        
// Load a group from memory (and print contents)
Group fromMem = new Group(buffer);
PeopleTable people3 = new PeopleTable(fromMem);

for (People ppl : people3) {
    System.out.println(ppl.getName() + " is " + ppl.getAge() + " years old");
}
// @@EndExample@@
}

}
