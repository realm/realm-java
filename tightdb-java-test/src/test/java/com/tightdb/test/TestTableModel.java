package com.tightdb.test;

import java.util.Date;

import com.tightdb.DefineTable;

/**
 * This model is used to generate classes that are used only for the tests.
 */
public class TestTableModel {

    @DefineTable
    class TestEmployee {
        String firstName;
        String lastName;
        int salary;
        boolean driver;
        byte[] photo;
        Date birthdate;
        Object extra;
        TestPhone phones;
    }

    @DefineTable
    class TestPhone {
        String type;
        String number;
    }

    @DefineTable
    class TestNumbers {
        long longNum;
        float floatNum;
        double doubleNum;
    }
    
    
    @DefineTable
    class TestQueryTable {
        long longNum;
        float floatNum;
        double doubleNum;
        String stringVal;
    }
}
