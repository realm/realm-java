//
// This example is used in the short introduction "Tightdb Java Interface"
// The @@ comments below are used for automatic extraction to the documentation
//

package com.tightdb.example;

import java.io.IOException;

import com.tightdb.Group;
import com.tightdb.lib.Table;

// @@Example: create_table @@
public class TutorialExample {
	
    @Table
    class people {
    	String name;
    	int age;
    	boolean hired;
    }
    
    public static void main(String[] args) {
        PeopleTable people = new PeopleTable();
        // ...
		// @@EndExample@@
		        
		/****************************** BASIC OPERATIONS *************************/
		
		// @@Example: insert_rows @@
		people.add("John", 20, true);
		people.add("Mary", 21, false);
		people.add("Lars", 32, true);
		people.add("Phil", 43, false);
		people.add("Anni", 53, true);
		// @@EndExample@@
		
		// @@Example: insert_at_index @@
		people.insert(2, "Frank", 34, true);
		// @@EndExample@@
		
		/****************************** GETTERS AND SETTERS **********************/
		
		// @@Example: accessing_rows @@
		// 2 ways to get the value
		String name = people.at(2).getName(); // name => "Mary"
		// or
		String name2 = people.at(2).name.get();
		
		// 2 ways to set the value
		people.at(2).name.set("NewName");
		// or
		people.at(2).setName("NewName"); 
		// @@EndExample@@
		
		System.out.println("at(2).getName -> " + name + " or " + name2);
		System.out.println("at(2).setName('NewName') -> " + people.at(2).getName());
				
		// @@Example: number_of_rows @@
		if (!people.isEmpty()) {
		    long s = people.size(); // s => 6
		}
		// @@EndExample@@
		
		System.out.println("Size = " + people.size() + "\n");
		
		/****************************** ITERATION OF ALL RECORDS *****************/
		
		// lazy iteration over the table
		
		// @@Example: iteration @@
		for (PeopleRow person : people) {
			System.out.println(person.getName() + " is " + person.getAge() + " years old.");
		}
		// @@EndExample@@
		
		/****************************** SIMPLE QUERY *****************************/
		
		// @@Example: simple_seach @@
		PeopleRow p = people.name.equal("John").findFirst();
		// @@EndExample@@
		
		System.out.println("\nFind 'John': " + p + "\n");
		
		/****************************** COMPLEX QUERY ****************************/
		
		// @@Example: advanced_search @@
		// Define the query
		PeopleQuery query = people.name.contains("a")
								.group()
									.hired.equal(false)
									.or()
									.name.endsWith("y")
								.endGroup()
								.age.between(20, 30);
		// Count matches
		System.out.println(query.count() + " person match query.");
		
		// Perform query and use the result
		for (PeopleRow person : query.findAll()) {
		    // ... do something with matching 'person'
		}        
		// @@EndExample
		
		/****************************** DATA REMOVAL *****************************/
		// @@Example: deleting_row @@
		people.remove(2);
		// @@EndExample@@
		
		System.out.println("\nRemoved row 2. Down to " + people.size() + " rows.\n");
		
		/****************************** SERIALIZE ********************************/
		
		// @@Example: serialisation @@
		// Create Table in Group
		Group group = new Group();
		PeopleTable people1 = new PeopleTable(group);
		
		people1.add("John", 20, true);
		people1.add("Mary", 21, false);
		        
		// Write to disk
		try {
		    group.writeToFile("people.tightdb");
		} catch (IOException e) {
		    e.printStackTrace();
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
		// @@EndExample@@
	}
}
