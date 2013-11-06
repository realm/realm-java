// @@Example: ex_java_typed_table_intro_subtables @@
package com.tightdb.refdoc;
import java.io.File;
import java.io.IOException;

import com.tightdb.*;

public class TypedTableIntroSubtables {


    @DefineTable
    class Employees {
        String  name;
        int     age;
        boolean hired;
        PhoneNumbers phones;
    }
    
    @DefineTable
    class PhoneNumbers {
        String  desc;
        String  number;
    }

    public static void main(String[] args) {
        // @@Show@@

        //Create table instance from the generated class
        EmployeesTable employeesTable = new EmployeesTable();

        // Multiple approaches to add data to subtables
        
        // First approach:
        // Add a row with values but insert null in the subtables column
        EmployeesRow row = employeesTable.add("John", 20, true, null);
        
        // Then retrieve the subtable from  EmployeeRow and add data
        PhoneNumbersTable phonesTable = row.getPhones();
        phonesTable.add("mobile", "121-121-121");
        phonesTable.add("work", "232-232-232");
        
        // Second approach:
        // Insert subtable values as an Object[][]
        Object[][] phones = new Object[][] {{"mobile", "343-343-343"},
                                            {"work", "454-454-454"} };
        
        employeesTable.add("Mary", 21, false, phones);
        
        // Compact version
        employeesTable.add("Lars", 32, true, new Object[][] {{"mobile", "565-565-565"},
                                                             {"work", "676-676-676"},
                                                             {"home", "787-787-787"}});
        
        /****************************** GETTERS AND SETTERS **********************/

        // Get phone number from row 0 in subtable in row 2 in parent table
        String number = employeesTable.get(2).getPhones().get(0).getNumber(); // name => "565-565-565"
        
        // Set the number to new number
        employeesTable.get(2).getPhones().get(0).setNumber("555-555-555");

        String lastPhoneDescription = employeesTable.get(2).getPhones().last().getDesc();  // desc => "home"

        // Replace entire home number with beach house number
        employeesTable.get(2).getPhones().last().set("beach hourse", "999-999-999");

        /****************************** DATA REMOVAL *****************************/
        
        // Remove beach house row 
        employeesTable.get(2).getPhones().removeLast();        
        // @@EndShow@@
    } 
}
//@@EndExample@@
