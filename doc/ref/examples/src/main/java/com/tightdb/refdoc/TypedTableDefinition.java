package com.tightdb.refdoc;

import com.tightdb.*;

public class TypedTableDefinition {

    // @@Example: ex_java_typed_table_definition @@

    // Define a table with columns "brand", "model" and "year"
    // The following classes are generated CarTable, CarQuery, CarView and CarRow 
    @DefineTable
    class Car {
        String  brand;
        String  model;
        int year;
    }

    // It is possible to directly specify the names of one or more of the generated classes 
    @DefineTable(table="Trucks", query="TruckFilter", view="TruckResults", row="Truck")
    class Truck {
        String  brand;
        String  model;
        int year;
    }

    // The following column types are supported
    @DefineTable
    class AllTypes {
        boolean  boolCol;
        java.util.Date dateCol;
        double doubleCol;
        int intCol;
        float floatCol;
        String stringCol;
        byte[] byteCol;
        Car carTableCol; // Subtable of the type Car defined as a typed table
        Object mixedCol; // Can hold any of the types

    } 
    //@@EndExample@@
}
