package com.tightdb.experiment;

import java.util.Date;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tightdb.ColumnType;
import com.tightdb.Table;
import com.tightdb.TableQuery;
import com.tightdb.exceptions.IllegalTypeException;

public class ColumnTypeQueryTest {
    static final boolean ENABLED = false;
    private Table t;
    private TableQuery q;
    
    @BeforeMethod
    public void init() {
        t  = new Table();
        t.addColumn(ColumnType.ColumnTypeDate, "Date");
        t.addColumn(ColumnType.ColumnTypeString, "String");
        t.addColumn(ColumnType.ColumnTypeInt, "Integer");
        
        t.add(new Date(), "I'm a String", 33);
        t.add(new Date(), "Second String", 458);
        
        q = t.where();
    }
    
    @Test(enabled = ENABLED, expectedExceptions=IllegalTypeException.class)
    public void filterLongOnStringColumn() {
        q.equal(1, 23).findAll();
    }
    
    @Test(enabled = ENABLED, expectedExceptions=IllegalTypeException.class)
    public void filterStringOnLongColumn() {
        q.equal(3, "I'm a String").findAll();
    }
    
    @Test(enabled = ENABLED, expectedExceptions=IllegalTypeException.class)
    public void filterStringOnDateColumn() {
        q.equal(1, "I'm a String").findAll();
    }
    
    
}
