//
// This example is used in the short introduction "TightDB Java Interface"
// The @@ comments below are used for automatic extraction of the code
// to the documentation.
//

package io.realm.examples.tutorial;

import io.realm.*;

import java.io.IOException;
import java.io.File;

@SuppressWarnings("unused")

public class Tutorial {
    public static void main(String[] args) {
        tutorial();
    }

    public static void tutorial() {

        //@@Example: create_table @@
        // Define the TighDB table with columns "name", "age" and "hired"
        Table peopleTable = new Table();
        final long NAME = peopleTable.addColumn(ColumnType.STRING, "name");
        final long AGE = peopleTable.addColumn(ColumnType.INTEGER, "age");
        final long HIRED = peopleTable.addColumn(ColumnType.BOOLEAN, "hired");
        //@@EndExample@@

        Assert(peopleTable.getColumnCount() == 3);

        /****************************** BASIC OPERATIONS *************************/

        // @@Example: insert_rows @@
        peopleTable.add("John", 20, true);
        peopleTable.add("Mary", 21, false);
        peopleTable.add("Lars", 32, true);
        peopleTable.add("Phil", 43, false);
        peopleTable.add("Anni", 54, true);
        // @@EndExample@@

        Assert(peopleTable.size() == 5);

        // @@Example: insert_at_index @@
        peopleTable.addAt(2, "Frank", 34, true);
        // @@EndExample@@

        Assert(peopleTable.size() == 6);
        Assert(peopleTable.getString(NAME, 2).equals("Frank"));

        // @@Example: number_of_rows @@
        if (!peopleTable.isEmpty()) {
            long s = peopleTable.size();
        }
        // @@EndExample@@

        System.out.println("Size = " + peopleTable.size() + "\n");

        /****************************** GETTERS AND SETTERS **********************/

        // @@Example: accessing_rows @@
        // Get value
        String name = peopleTable.getString(NAME, 1);

        // Set value
        peopleTable.setString(NAME, 1, "NewName");
        // @@EndExample@@

        Assert(name.equals("Mary"));
        Assert(peopleTable.getString(NAME, 1).equals("NewName"));

        // @@Example: updating_entire_row @@
        peopleTable.set(4, "Eric", 50, true);
        // @@EndExample@@

        Assert(peopleTable.getString(NAME, 4).equals("Eric"));
        Assert(peopleTable.getLong(AGE, 4) == 50);
        Assert(peopleTable.getBoolean(HIRED, 4) == true);

        /****************************** DATA REMOVAL *****************************/
        // @@Example: deleting_row @@
        peopleTable.remove(2);
        // @@EndExample@@

        Assert(peopleTable.size() == 5);

        /****************************** ITERATION OF ALL RECORDS *****************/

        // lazy iteration over the table

        // @@Example: iteration @@
        for (long index = 0; index < peopleTable.size(); index++) {
            System.out.println(peopleTable.getString(NAME, index) +
                    " is " + peopleTable.getLong(AGE, index) + " years old.");
        }
        // @@EndExample@@

        /****************************** SIMPLE QUERY *****************************/

        // @@Example: simple_seach @@
        long johnIndex = peopleTable.findFirstString(NAME, "John");
        System.out.println("Name: " + peopleTable.getString(NAME, johnIndex) +
                ", Age: " + peopleTable.getLong(AGE, johnIndex) +
                ", Hired: " + peopleTable.getBoolean(HIRED, johnIndex));
        // prints: "Name: John, Age: 20, Hired: true"
        // @@EndExample@@

        Assert(johnIndex == 0);

        /****************************** COMPLEX QUERY ****************************/

        // @@Example: advanced_search @@
        // Define the query
        TableQuery query = peopleTable.where()
                               .between(AGE, 20, 35)
                               .contains(NAME, "a")
                               .group()
                                   .equalTo(HIRED, true)
                                   .or()
                                   .endsWith(NAME, "y")
                               .endGroup();
        // Count matches
        TableView match = query.findAll();
        System.out.println(match.size() + " employee(s) match query.");

        // Take the average age of the matches
        System.out.println(match.averageLong(AGE) + " years is the average age.");

        // Perform query and use the result
        for (long index = 0; index < match.size(); index++) {
            System.out.println(match.getString(NAME, index) +
                    " is " + match.getLong(AGE, index) + " years old.");
        }
        // @@EndExample

        Assert(match.size() == 1);

        Assert(match.averageLong(AGE) == 32D);

        /****************************** SERIALIZE ********************************/

        System.out.println("Serialize to file:");
        // remove file if there - can't write to the same file
        new File("people.realm").delete();

        // @@Example: serialisation @@
        // Create Table in Group
        Group group = new Group();
        Table people1 = group.getTable("people");
        people1.addColumn(ColumnType.STRING, "name");
        people1.addColumn(ColumnType.INTEGER, "age");
        people1.addColumn(ColumnType.BOOLEAN, "hired");

        people1.add("John", 20, true);
        people1.add("Mary", 21, false);

        Assert(people1.getColumnCount() == 3);
        Assert(people1.size() == 2);

        // Write to disk
        try {
            group.writeToFile("people.realm");
        } catch (IOException e) {
            // unable to write - handle...
            System.exit(1);
        }

        // Load a group from disk (and print contents)
        Group fromDisk = new Group("people.realm");
        Table people2 = fromDisk.getTable("people");

        Assert(people2.getColumnCount() == 3);
        Assert(people2.size() == 2);

        for (long index = 0; index < people2.size(); index++) {
            System.out.println(people2.getString(NAME, index) +
                    " is " + people2.getLong(AGE, index) + " years old.");
        }

        // Write same group to memory buffer
        byte[] buffer = group.writeToMem();

        // Load a group from memory (and print contents)
        Group fromMem = new Group(buffer);
        Table people3 = fromMem.getTable("people");

        for (long index = 0; index < people3.size(); index++) {
            System.out.println(people3.getString(NAME, index) +
                    " is " + people3.getLong(AGE, index) + " years old.");
        }
        // @@EndExample@@

        /****************************** TRANSACTIONS ********************************/

        System.out.println("\nTransactions:");
        // @@Example: transaction @@

        // Open a shared group
        SharedGroup sharedGroup = new SharedGroup("people.realm");

        // Write transaction:
        WriteTransaction writeTransaction = sharedGroup.beginWrite();
        try {
            Table person = writeTransaction.getTable("people");
            // Add row to table
            person.add("Bill", 53, true);
            writeTransaction.commit();
        } catch (Throwable e) {
            writeTransaction.rollback();
        }

        // Read transaction:
        ReadTransaction readTransaction = sharedGroup.beginRead();
        try {
            Table people = readTransaction.getTable("people");
            for (long index = 0; index < people.size(); index++) {
                System.out.println(people.getString(NAME, index) +
                        " is " + people.getLong(AGE, index) + " years old.");
            }
        } finally {
            readTransaction.endRead();
        }
        // @@EndExample@@

    } // main

    static void Assert(boolean check) {
        if (!check) {
            throw new RuntimeException();
        }
    }
} // class
