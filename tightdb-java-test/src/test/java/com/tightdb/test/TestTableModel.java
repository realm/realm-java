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
    
    @DefineTable
    class AllTypes {
        boolean  boolCol;
        java.util.Date dateCol;
        double doubleCol;
        int intCol;
        long longCol;
        float floatCol;
        String stringCol;
        byte[] byteCol;
        Car carTableCol; // Subtable of the type Car defined as a typed table
        Object mixedCol; // Can hold any of the types
    } 
    
    @DefineTable
    class Car {
        String  brand;
        String  model;
        int year;
    }
}
