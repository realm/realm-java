package com.tightdb.experiment;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.tightdb.ColumnType;
import com.tightdb.Table;
import com.tightdb.TableView;
import com.tightdb.exceptions.IllegalTypeException;
import com.tightdb.test.DataProviderUtil;

public class ColumnTypeViewTest {
    private Table t;
    private TableView v;
    
    @BeforeMethod
    public void init() {
        t  = new Table();
        t.addColumn(ColumnType.ColumnTypeDate, "Date");
        t.addColumn(ColumnType.ColumnTypeString, "String");
        t.addColumn(ColumnType.ColumnTypeInt, "Integer");
        
        t.add(new Date(), "I'm a String", 33);
        
        v = t.where().findAll();
    }
    
/* TODO: Enable   
    //On date Column________________________________
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void getStringOnDateColumn() {
        v.getString(0, 0);
    }
    
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void getLongOnDateColumn() {
      v.getLong(0, 0);
    }
    
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void getMixedOnDateColumn() {
      v.getMixed(0, 0);
    }
    
    //On String Column________________________________
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void getDateOnStringColumn() {
        v.getDate(1, 0);
    }
    
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void getLongOnStringColumn() {
      v.getLong(1, 0);
    }
    
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void getMixedOnStringColumn() {
      v.getMixed(1, 0);
    }
    
    //On Integer Column________________________________
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void getDateOnIntegerColumn() {
        v.getDate(2, 0);
    }
    
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void getLongOnIntegerColumn() {
      v.getLong(2, 0);
    }
    
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void getMixedOnIntegerColumn() {
      v.getMixed(2, 0);
    }
  */  
}
