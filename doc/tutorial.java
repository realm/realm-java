package com.tightdb.example;

import java.util.Arrays;
import java.util.Date;

import com.tightdb.generated.Employee;
import com.tightdb.generated.EmployeeTable;
import com.tightdb.generated.EmployeeView;
import com.tightdb.generated.PhoneTable;
import com.tightdb.lib.AbstractColumn;
import com.tightdb.lib.NestedTable;
import com.tightdb.lib.Table;
import com.tightdb.lib.TightDB;

// @@Example: create_table @@
public class Example {
    @Table
    class people {
	String name;
	int age;
	boolean hired;
    }

    public static void main(String[] args) {
        PeopleTable peopletable = new PeopleTable();
        // ...
// @@EndExample@@

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

		TightDB.print("Employees", employees);

		TightDB.print("Johny", johny);

		System.out.println("first record: " + john);
		System.out.println("second record: " + nikolche);
		System.out.println("some column: " + john.firstName);

		/****************************** GETTERS AND SETTERS *****************************/

// @@Example: accessing_rows @@
// 2 ways to get the value
String name = peopletable.at(2).getName(); // name => "Mary"
// or
String name = peopletable.at(2).name.get();

// 2 ways to set the value
peopletable.at(2).name.set("NewName");
// or
peopletable.at(2).setName("NewName"); 
// @@EndExample@@


// @@Example: number_of_rows @@
if (!peopletable.isEmpty()) {
    long s = peopletable.size(); // s => 6
}
// @@EndExample@@

		Employee niko = employees.firstName.startsWith("Nik").findUnique();
		System.out.println("Unique Niko: " + niko);

		/****************************** MANIPULATION OF ALL RECORDS *****************************/

		// using explicit OR
		TightDB.print("Search example", employees.firstName.is("Johnny").or().lastName.is("Mihajlovski").findFirst());

		// using implicit AND
		TightDB.print("Search example 2", employees.firstName.is("Johnny").lastName.startsWith("B").findLast());

		employees.firstName.is("John").findLast().salary.set(30000);

		/****************************** ITERATION OF ALL RECORDS *****************************/

		// lazy iteration over the table
// @@Example: iteration @@
for (People people : peopletable) {
	System.out.println(people.getName() + " is " + people.getAge() + " years old.");
}
// @@EndExample@@

		/****************************** AGGREGATION *****************************/

		// aggregation of the salary
		System.out.println("max salary: " + employees.salary.max());
		System.out.println("min salary: " + employees.salary.min());
		System.out.println("salary sum: " + employees.salary.sum());

                /****************************** SIMPLE QUERY ******************************/
// @@Example: simple_seach @@
People p = peopletable.name.is("John").findFirst();
// @@EndExample@@
		/****************************** COMPLEX QUERY *****************************/
		

// @@Example: advanced_search @@
PeopleQuery query = peopletable.age.between(20, 30);
System.out.println(query.count());
System.out.println(query.average());
for (People people : query.findAll()) {
    System.out.println(people.getName() + " is " + people.getAge() + " years old");
}        
// @@EndExample

        TightDB.print("Query 1", employees.firstName.startsWith("Nik").lastName.contains("vski").or().firstName.is("John").findAll());

		TightDB.print("Query 2a", employees.firstName.startsWith("Nik").startGroup().lastName.contains("vski").or().firstName.is("John").endGroup()
				.findAll());

		TightDB.print("Query 2b",
				employees.query().startGroup().lastName.contains("vski").or().firstName.is("John").endGroup().firstName.startsWith("Nik").findAll());

		/****************************** MANIPULATION OF ALL RECORDS *****************************/

		System.out.println("- First names: " + Arrays.toString(employees.firstName.getAll()));

		employees.salary.setAll(100000);
		employees.firstName.contains("o").findAll().firstName.setAll("Bill");

		TightDB.print(employees);

		/****************************** COLUMN RETRIEVAL *****************************/

		System.out.print("- Columns:");
		for (AbstractColumn<?, ?, ?> column : john.columns()) {
			System.out.print(column.getName() + "=" + column.getReadableValue());
		}
		System.out.println();

		/****************************** SUBTABLES *****************************/

		PhoneTable subtable = john.phones.get();
		subtable.add("mobile", "111");
		
		john.getPhones().add("mobile", "111");
		john.getPhones().add("home", "222");

		johny.getPhones().add("mobile", "333");

		nikolche.getPhones().add("mobile", "444");
		nikolche.getPhones().add("work", "555");

		for (PhoneTable phoneTable : employees.phones.getAll()) {
			TightDB.print(phoneTable);
		}

		/****************************** DATA REMOVAL *****************************/
// @@Example: deleting_row @@
peopletable.remove(2);
// @@EndExample@@

		/****************************** NOT IMPLEMENTED YET *****************************/

		try {
			// from 2nd to 4th row
			EmployeeView view = employees.range(2, 4);

			// cursor navigation
			Employee p1 = employees.at(4).next(); // 5nd row
			Employee p2 = employees.last().previous(); // 2nd-last row
			Employee p3 = employees.first().after(3); // 4th row
			Employee p4 = employees.last().before(2); // 3rd-last row
		} catch (Exception e) {
		}
	}

}
