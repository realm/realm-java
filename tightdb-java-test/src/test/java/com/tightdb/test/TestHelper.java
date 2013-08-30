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
            return ColumnType.BOOLEAN;
        if (o instanceof String)
            return ColumnType.STRING;
        if (o instanceof Long)
            return ColumnType.LONG;
        if (o instanceof Float)
            return ColumnType.FLOAT;
        if (o instanceof Double)
            return ColumnType.DOUBLE;
        if (o instanceof Date)
            return ColumnType.DATE;
        if (o instanceof byte[])
            return ColumnType.BINARY;
        
        return ColumnType.MIXED;
    }
}
