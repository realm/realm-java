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
		for (People person : people) {
			System.out.println(person.getName() + " is " + person.getAge() + " years old.");
		}
		// @@EndExample@@
		
		/****************************** SIMPLE QUERY *****************************/
		// @@Example: simple_seach @@
		People p = people.name.is("John").findFirst();
		// @@EndExample@@
		
		System.out.println("\nFind 'John': " + p + "\n");
		
		/****************************** COMPLEX QUERY ****************************/
		
		// @@Example: advanced_search @@
		PeopleQuery query = people.age.between(20, 30);
		System.out.println(query.count() + "people are between 20 and 30.");	
		
		for (People person : query.findAll()) {
		    System.out.println(person.getName() + " is " + person.getAge() + " years old");
		}        
		// @@EndExample

		//System.out.println("Average age is " + query.age.average() + " years.");

		/****************************** DATA REMOVAL *****************************/
		// @@Example: deleting_row @@
		people.remove(2);
		// @@EndExample@@
		
		System.out.println("\nRemoved row 2. Down to " + people.size() + " rows.\n");
		
		/****************************** SERIALIZE ********************************/
		
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
		       
		for (People person : people2) {
		    System.out.println(person.getName() + " is " + person.getAge() + " years old");
		}
		
		// Write same group to memory buffer
		byte[] buffer = group.writeToMem();
		        
		// Load a group from memory (and print contents)
		Group fromMem = new Group(buffer);
		PeopleTable people3 = new PeopleTable(fromMem);
		
		for (People person : people3) {
		    System.out.println(person.getName() + " is " + person.getAge() + " years old");
		}
		// @@EndExample@@
	}
}
