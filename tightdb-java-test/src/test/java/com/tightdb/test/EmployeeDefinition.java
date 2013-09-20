package com.tightdb.test;

import com.tightdb.DefineTable;

import java.nio.ByteBuffer;
import java.util.Date;

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

public class EmployeeDefinition {
}
