package com.tightdb.test;

import java.util.Date;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tightdb.ColumnType;
import com.tightdb.Table;
import com.tightdb.exceptions.IlegalTypeException;

public class ColumnTypeInsertTest {
    
    private Table t;
    
    @BeforeMethod
    public void init() {
        t  = new Table();
        t.addColumn(ColumnType.ColumnTypeDate, "date");
        t.addColumn(ColumnType.ColumnTypeString, "name");
    }
    
    
    @Test(expectedExceptions=IlegalTypeException.class)
    public void testStringInsertInDateColumn() {
      t.add("I'm an String", "I'm also a String");
    }
    
    
    @Test(expectedExceptions=IlegalTypeException.class)
    public void testDoubleInsertInDateColumn() {
      t.add(34.65, "I'm also a String");
    }
    
    @Test(expectedExceptions=IlegalTypeException.class)
    public void testIntegerInsertInStringColumn() {
      t.add(new Date(), 400);
    }
}