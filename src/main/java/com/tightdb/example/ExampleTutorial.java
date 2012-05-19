package com.tightdb.example;

import com.tightdb.generated.People;
import com.tightdb.generated.PeopleQuery;
import com.tightdb.generated.PeopleTable;
import com.tightdb.lib.NestedTable;
import com.tightdb.lib.Table;

public class ExampleTutorial {
	
	@Table
	class people {
		String name;
		int age;
		boolean hired;
	}

	@NestedTable
	class phone {
		String type;
		String number;
	}

	public static void main(String[] args) {
// @@Example: create_table @@
PeopleTable peopletable = new PeopleTable();
// @@EndExample@@

		/****************************** BASIC OPERATIONS *****************************/

// @@Example: insert_rows @@
People john = peopletable.add("John", 20, true);
People mary = peopletable.add("Mary", 21, false);
People lars = peopletable.add("Lars", 32, true);
People phil = peopletable.add("Phil", 43, false);
People anni = peopletable.add("Anni", 53, true);
// @@EndExample@@
// @@Example: insert_at_index @@
People frank = peopletable.insert(2, "Frank", 34, true);
// @@EndExample@@

/*
		TightDB.print("Employees", employees);

		TightDB.print("Johny", john);

		System.out.println("first record: " + john);
		System.out.println("some column: " + john.firstName);
*/
		/****************************** GETTERS AND SETTERS *****************************/

// @@Example: accessing_rows @@
// 2 ways to get the value
System.out.println("name1: " + john.name.get());
System.out.println("name2: " + john.getName());

// 2 ways to set the value
peopletable.at(2).name.set("NewName");
peopletable.at(2).setName("NewName");
// @@EndExample@@

//		Employee niko = employees.firstName.startsWith("Nik").findUnique();
//		System.out.println("Unique Niko: " + niko);

		/****************************** MANIPULATION OF ALL RECORDS *****************************/

		// using explicit OR
//		TightDB.print("Search example", employees.firstName.is("Johnny").or().lastName.is("Mihajlovski").findFirst());

		// using implicit AND
//		TightDB.print("Search example 2", employees.firstName.is("Johnny").lastName.startsWith("B").findLast());

//		employees.firstName.is("John").findLast().salary.set(30000);

		/****************************** ITERATION OF ALL RECORDS *****************************/

		// lazy iteration over the table
// @@Example: iteration @@
for (People people : peopletable) {
	System.out.println(people.name.get() + " is " + people.age.get() + " years old.");
}
// @@EndExample@@

		//****************************** AGGREGATION *****************************
/*
		// aggregation of the salary
		System.out.println("max salary: " + employees.salary.max());
		System.out.println("min salary: " + employees.salary.min());
		System.out.println("salary sum: " + employees.salary.sum());
*/
		//****************************** COMPLEX QUERY *****************************
		
// @@Example: simple_search @@
// To be implemented
// @@EndExample

// @@Example: advanced_search @@
PeopleQuery query = peopletable.age.between(20, 30);
//System.out.println(query.count());
//System.out.println(query.average());
for (People people : query.findAll()) {
    System.out.println(people.name.get() + " is " + people.age.get() + " years old");
}        
// @@EndExample

		//****************************** DATA REMOVAL *****************************
// @@Example: deleting_row @@
peopletable.remove(2);
System.out.println(peopletable.size());
// @@EndExample@@

		/****************************** Serialization *****************************/
		/*	
Convert from C++:	
		// Create Table in Group
		Group group;
		BasicTableRef<MyTable> t = group.get_table<MyTable>("employees");
		
		// Add some rows
		t->add("John", 20, true);
		t->add("Mary", 21, false);
		t->add("Lars", 21, true);
		t->add("Phil", 43, false);
		t->add("Anni", 54, true);
		
		// Write to disk
		group.write("employees.tightdb");
		
		// Load a group from disk (and print contents)
		Group fromDisk("employees.tightdb");
		BasicTableRef<MyTable> diskTable = fromDisk.get_table<MyTable>("employees");
		for (size_t i = 0; i < diskTable->size(); i++)
		    cout << i << ": " << diskTable[i].name << endl;
		
		// Write same group to memory buffer
		size_t len;
		const char* const buffer = group.write_to_mem(len);
		
		// Load a group from memory (and print contents)
		Group fromMem(buffer, len);
		BasicTableRef<MyTable> memTable = fromMem.get_table<MyTable>("employees");
		for (size_t i = 0; i < memTable->size(); i++)
		    cout << i << ": " << memTable[i].name << endl; 
		*/	

		//****************************** NOT IMPLEMENTED YET *****************************
	}
}