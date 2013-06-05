package com.tightdb.test;

import java.util.Date;

public class EmployeesFixture {
    
    public static final PhoneData[][] PHONES = { 
        { new PhoneData("home", "123") }, 
        { new PhoneData("mobile", "456") }, 
        { new PhoneData("work", "789"), new PhoneData("mobile", "012") } 
    };
    
    public static final EmployeeData[] EMPLOYEES = { new EmployeeData("John", "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(111111), "extra", PHONES[0]),
            new EmployeeData("Nikolche", "Mihajlovski", 30000, false, new byte[] { 4, 5 }, new Date(2222), 1234, PHONES[1]),
            new EmployeeData("Johny", "B. Good", 10000, true, new byte[] { 1, 2, 3 }, new Date(333343333), true, PHONES[2]) };

    public static Object[] getAll(int index) {
        Object[] values = new Object[EMPLOYEES.length];

        for (int i = 0; i < values.length; i++) {
            values[i] = EMPLOYEES[i].get(index);
        }

        return values;
    }

}
