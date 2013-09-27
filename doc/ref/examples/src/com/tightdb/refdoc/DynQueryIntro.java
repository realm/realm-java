// @@Example: ex_java_dyn_query_intro @@
package com.tightdb.refdoc;

import com.tightdb.ColumnType;
import com.tightdb.Table;
import com.tightdb.TableQuery;
import com.tightdb.TableView;
import java.util.Date;

public class DynQueryIntro {

public static void main(String[] args) {

// @@Show@@
Table table = new Table();

// Specify the column types and names
table.addColumn(ColumnType.STRING, "firstName");
table.addColumn(ColumnType.STRING, "lastName");
table.addColumn(ColumnType.INTEGER, "salary");

// Add data to the table
table.add("John", "Lee", 10000);
table.add("Jane", "Lee", 15000);
table.add("John", "Anderson", 20000);
table.add("Erik", "Lee", 30000);
table.add("Henry", "Anderson", 10000);

// Create a query object from the table.
TableQuery query = table.where();

TableView view;

// Find all employees with a first name of John.
view = table.where().equalTo(0, "John").findAll();

// Find the average salary of all employees with the last name Anderson.
double avgSalary = table.where().equalTo(1, "Anderson").average(2);

// Find the total salary of people named Jane and Erik.
double salary = table.where().group().equalTo(0, "Jane").or().equalTo(0, "Erik").endGroup().sum(2);

// Find all employees with a last name of Lee and a salary less than 25000.
view = table.where().equalTo(1, "Lee").lessThan(2, 25000).findAll();

// Querying on a view
view = table.where().tableview(view).equalTo(0, "Jane").findAll();
// @@EndShow@@

}

}
//@@EndExample@@
