package com.tightdb.experiment;

import java.util.Date;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tightdb.ColumnType;
import com.tightdb.Table;
import com.tightdb.TableQuery;

public class ColumnTypeQueryTest {
    
    private Table t;
    private TableQuery q;
    
    @BeforeMethod
    public void init() {
        t  = new Table();
        t.addColumn(ColumnType.DATE, "Date");
        t.addColumn(ColumnType.STRING, "String");
        t.addColumn(ColumnType.LONG, "Long");
        
        t.add(new Date(), "I'm a String", 33);
        t.add(new Date(), "Second String", 458);
        
        q = t.where();
    }
    
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void filterLongOnStringColumn() {
        q.equal(1, 23).findAll();
    }
    
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void filterStringOnLongColumn() {
        q.equal(3, "I'm a String").findAll();
    }
    
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void filterStringOnDateColumn() {
        q.equal(1, "I'm a String").findAll();
    }
    
    
}
