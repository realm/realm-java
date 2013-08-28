package com.tightdb.dynamic;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.tightdb.ColumnType;
import com.tightdb.Table;
import com.tightdb.test.DataProviderUtil;

public class ColumnTypeInsertTest {




    @Test(expectedExceptions=IllegalArgumentException.class, dataProvider = "columnTypesProvider")
    public void testGenericInsert(Object colTypeObject, Object o) {
        
        Table t  = new Table();
        
        //If the objects matches it will not fail, therefore we throw an exception as it should not be tested
        if(o.getClass().equals(colTypeObject.getClass())){
            throw new IllegalArgumentException();
        }
        
        t.addColumn(getColumnType(colTypeObject), colTypeObject.getClass().getSimpleName());
        
        System.out.println(o.getClass().getSimpleName());
        t.add(o);
    }

    
    /**
     * Returns the corresponding column type for an object.
     * @param o
     * @return
     */
    public ColumnType getColumnType(Object o){
        
        if(o instanceof Boolean)
            return ColumnType.ColumnTypeBool;
        if(o instanceof String)
            return ColumnType.ColumnTypeString;
        if(o instanceof Long)
            return ColumnType.ColumnTypeInt;
        if(o instanceof Float)
            return ColumnType.ColumnTypeFloat;
        if(o instanceof Double)
            return ColumnType.ColumnTypeDouble;
        if(o instanceof Date)
            return ColumnType.ColumnTypeDate;
        if(o instanceof byte[])
            return ColumnType.ColumnTypeBinary;
        
        return ColumnType.ColumnTypeMixed;
    }
    
    
    //Generates a list of different objects to be passed as parameter to the insert() on table
    @DataProvider(name = "columnTypesProvider")
    public Iterator<Object[]> mixedValuesProvider() {
        Object[] values = {
                true, 
                "abc", 
                123L,
                987.123f, 
                1234567.898d, 
                new Date(645342), 
                new byte[] { 1, 2, 3, 4, 5 }
        };

        List<?> mixedValues = Arrays.asList(values);
        return DataProviderUtil.allCombinations(mixedValues,mixedValues);
    }
}
