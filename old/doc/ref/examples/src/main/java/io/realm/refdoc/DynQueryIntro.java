/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// @@Example: ex_java_dyn_query_intro @@
package io.realm.refdoc;

import io.realm.ColumnType;
import io.realm.Table;
import io.realm.TableQuery;
import io.realm.TableView;

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

// Find all employees with a first name of John.
TableView view = table.where().equalTo(0, "John").findAll();

// Find the average salary of all employees with the last name Anderson.
double avgSalary = table.where().equalTo(1, "Anderson").averageInt(2);

// Find the total salary of people named Jane and Erik.
double salary = table.where().group()
                                .equalTo(0, "Jane")
                                .or()
                                .equalTo(0, "Erik")
                             .endGroup()
                             .sumInt(2);

// Find all employees with a last name of Lee and a salary less than 25000.
view = table.where().equalTo(1, "Lee").lessThan(2, 25000).findAll();

// Querying on a view
view = table.where().tableview(view).equalTo(0, "Jane").findAll();
// @@EndShow@@

}

}
//@@EndExample@@
