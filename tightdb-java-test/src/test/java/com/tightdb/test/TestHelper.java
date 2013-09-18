package com.tightdb.test;

import java.util.Date;

import com.tightdb.ColumnType;
import com.tightdb.Table;


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
    
    
    /**
     * Creates an empty table with 1 column of all our supported column types, currently 9 columns
     * @return
     */
    public static Table getTableWithAllColumnTypes(){
        
        Table t = new Table();
        
        t.addColumn(ColumnType.ColumnTypeBinary, "binary");
        t.addColumn(ColumnType.ColumnTypeBool, "boolean");
        t.addColumn(ColumnType.ColumnTypeDate, "date");
        t.addColumn(ColumnType.ColumnTypeDouble, "double");
        t.addColumn(ColumnType.ColumnTypeFloat, "float");
        t.addColumn(ColumnType.ColumnTypeInt, "long");
        t.addColumn(ColumnType.ColumnTypeMixed, "mixed");
        t.addColumn(ColumnType.ColumnTypeString, "string");
        t.addColumn(ColumnType.ColumnTypeTable, "table");
        
        return t;
    }
}
