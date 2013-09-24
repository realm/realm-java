package com.tightdb;
   
import static org.testng.AssertJUnit.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tightdb.ColumnType;
import com.tightdb.Table;

    
public class JNITableTest {
	Table t = new Table();
	
    @BeforeMethod
	void init() {		
        t.addColumn(ColumnType.ColumnTypeBinary, "binary"); // 0
        t.addColumn(ColumnType.ColumnTypeBool, "boolean");  // 1
        t.addColumn(ColumnType.ColumnTypeDate, "date");     // 2
        t.addColumn(ColumnType.ColumnTypeDouble, "double"); // 3
        t.addColumn(ColumnType.ColumnTypeFloat, "float");   // 4
        t.addColumn(ColumnType.ColumnTypeInt, "long");      // 5
        t.addColumn(ColumnType.ColumnTypeMixed, "mixed");   // 6
        t.addColumn(ColumnType.ColumnTypeString, "string"); // 7
        t.addColumn(ColumnType.ColumnTypeTable, "table");   // 8
	}

    // Check all other column types than String throws exception when using setIndex()/hasIndex()

    @Test()
    public void shouldThrowWhenSetIndexOnWrongColumnType() {
        for (long colIndex = 0; colIndex < t.getColumnCount(); colIndex++) {
            boolean exceptionExpected = (t.getColumnType(colIndex) != ColumnType.ColumnTypeString);

            // Try to setIndex()
            try {
                t.setIndex(colIndex);
                if (exceptionExpected)
                    fail("expected exception for colIndex " + colIndex);
            } catch (IllegalArgumentException e) {
            }

            // Try to hasIndex() for all columnTypes
            t.hasIndex(colIndex);
        }
    }

}
