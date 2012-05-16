//
// This example is used in the short introduction "Tightdb Java Interface"
//

package com.tightdb.example;

import com.tightdb.generated.People;
import com.tightdb.generated.PeopleTable;
import com.tightdb.generated.PeopleView;
import com.tightdb.generated.PeopleQuery;

import com.tightdb.lib.Table;
import com.tightdb.lib.TightDB;

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

/* NOT YET FUNCTIUONAL!!!    

// @@Example: serialisation @@
// Create Table in Group
Group group = new Group();
TableBase t = group.getTable("people");
        
// Add some rows by low-level interface - similar to highlevel and typesafe "add()"
t.insertString(0, 0, "John");
t.insertLong(1, 0, 20);
t.insertBoolean(2, 0, true);
t.insertDone();
      
t.insertString(0, 1, "Mary");
t.insertLong(1, 1, 21);
t.insertBoolean(2, 1, false);
t.insertDone();
        
t.insertString(0, 2, "Lars");
t.insertLong(1, 2, 21);
t.insertBoolean(2, 2, true);
t.insertDone();
        
t.insertString(0, 3, "Phil");
t.insertLong(1, 3, 43);
t.insertBoolean(2, 3, false);
t.insertDone();
        
// Write to disk
try {
    group.writeToFile("people.tightdb");
} catch (IOException e) {
    e.printStackTrace();
}
        
// Load a group from disk (and print contents)
Group fromDisk = new Group();
fromDisk.load("people.tightdb");
       
TableBase diskTable = fromDisk.getTable("people");
for (int i = 0; i < diskTable.getCount(); i++)
    System.out.println(i + ": " + diskTable.getString(0, i) );     // print names
        
// Write same group to memory buffer
byte[] buffer = group.writeToBuffer();
        
// Load a group from memory (and print contents)
Group fromMem = new Group();
fromMem.loadData(buffer);    // method will be renamed to "loadMem"
TableBase memTable = fromMem.getTable("people");
for (int i = 0; i < memTable.getCount(); i++)
    System.out.println(i + ": " + memTable.getString(0, i) );     // print names
// @@EndExample@@

*/
	}

}
