package com.tightdb.test;

import java.util.Date;

import com.tightdb.ColumnType;


/**
 * Class holds helper methods for the test cases
 *
 */
public class TestHelper {

    
    /**
     * Returns the corresponding column type for an object.
     * @param o
     * @return
     */
    public static ColumnType getColumnType(Object o){
        
        if (o instanceof Boolean)
            return ColumnType.ColumnTypeBool;
        if (o instanceof String)
            return ColumnType.ColumnTypeString;
        if (o instanceof Long)
            return ColumnType.ColumnTypeInt;
        if (o instanceof Float)
            return ColumnType.ColumnTypeFloat;
        if (o instanceof Double)
            return ColumnType.ColumnTypeDouble;
        if (o instanceof Date)
            return ColumnType.ColumnTypeDate;
        if (o instanceof byte[])
            return ColumnType.ColumnTypeBinary;
        
        return ColumnType.ColumnTypeMixed;
    }
}
