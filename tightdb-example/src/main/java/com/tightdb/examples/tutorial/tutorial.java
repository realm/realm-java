//
//This example is used in the short introduction "Tightdb Java Interface"
//The @@ comments below are used for automatic extraction to the documentation
//

package com.tightdb.examples.tutorial;

import java.io.IOException;

import com.tightdb.*;


//@@Example: create_table @@
public class tutorial {
	// Define the TighDB table with columns "name", "age" and "hired"
	@Table
	class people {
		String 	name;
	 	int 	age;
	 	boolean hired;
	}
	 
	public static void main(String[] args) {
	 PeopleTable peopletable = new PeopleTable();
	//@@EndExample@@
			        
	 /****************************** BASIC OPERATIONS *************************/
			
	 // @@Example: insert_rows @@
	 peopletable.add("John", 20, true);
	 peopletable.add("Mary", 21, false);
	 peopletable.add("Lars", 32, true);
	 peopletable.add("Phil", 43, false);
	 peopletable.add("Anni", 54, true);
	 // @@EndExample@@
			
	 // @@Example: insert_at_index @@
	 peopletable.insert(2, "Frank", 34, true);
	 // @@EndExample@@
	
	 // @@Example: number_of_rows @@
	 if (!peopletable.isEmpty()) {
	     long s = peopletable.size(); // s => 6
	 }
	 // @@EndExample@@
			
	 System.out.println("Size = " + peopletable.size() + "\n");
					
	 /****************************** GETTERS AND SETTERS **********************/
			
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
	
	 /****************************** DATA REMOVAL *****************************/
	 // @@Example: deleting_row @@
	 peopletable.remove(2);
	 // @@EndExample@@
	
	 System.out.println("\nRemoved row 2. Down to " + peopletable.size() + " rows.\n");
	
	 /****************************** ITERATION OF ALL RECORDS *****************/
	
	 // lazy iteration over the table
			
	 // @@Example: iteration @@
	 for (PeopleRow person : peopletable) {
	     System.out.println(person.getName() + " is " + person.getAge() + " years old.");
	 }
	 // @@EndExample@@
			
	 /****************************** SIMPLE QUERY *****************************/
	
	 System.out.println("\nFound: ");
	 // @@Example: simple_seach @@
	 PeopleRow p = peopletable.name.equal("John").findFirst();
	 System.out.println( p );	
	 // prints: "People {name=John, age=20, hired=true}"
	 //	 @@EndExample@@
	 
	 /****************************** COMPLEX QUERY ****************************/
	
	 // @@Example: advanced_search @@
	 // Define the query
	 PeopleQuery query = peopletable
			 .age.between(20, 35)		// Implicit AND with below
			 .name.contains("a")		// Implicit AND with below
			 .group()					// "("
	         	.hired.equal(true)
	         	.or()					// or
	         	.name.endsWith("y")
	         .endGroup();				// ")"
	 // Count matches
	 PeopleView match = query.findAll();
	 System.out.println(match.size() + " person(s) match query.");
	
	 // Take the average age of the matches    
	 System.out.println(match.age.sum() + " years is the summed age.");
	
	 // Perform query and use the result
	 for (PeopleRow person : match) {
	     // ... do something with matching 'person'
	 }        
	 // @@EndExample
	 System.out.println("");
	 
	 /****************************** SERIALIZE ********************************/
	
	 System.out.println("Serialize to file:");
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
