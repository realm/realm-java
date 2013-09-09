package com.tightdb.experiment;

import com.tightdb.ColumnType;
import com.tightdb.Group;
import com.tightdb.Mixed;
import com.tightdb.Table;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

import java.util.Date;

public class GroupToStringTest {

    @Test
    public void groupToString() {

        Group group = new Group();

        Table table = group.getTable("testTable");

        table.addColumn(ColumnType.ColumnTypeBinary, "binary");     // 0
        table.addColumn(ColumnType.ColumnTypeBool, "boolean");      // 1
        table.addColumn(ColumnType.ColumnTypeDate, "date");         // 2
        table.addColumn(ColumnType.ColumnTypeDouble, "double");     // 3
        table.addColumn(ColumnType.ColumnTypeFloat, "float");       // 4
        table.addColumn(ColumnType.ColumnTypeInt, "long");          // 5
        table.addColumn(ColumnType.ColumnTypeMixed, "mixed");       // 6
        table.addColumn(ColumnType.ColumnTypeString, "string");     // 7
        table.addColumn(ColumnType.ColumnTypeTable, "table");       // 8

        table.add(new byte[] {0,2,3}, true, new Date(0), 123D, 123F, 123, new Mixed(123), "TestString", null);

        assertEquals("     tables     rows  \n" +
                     "   0 testTable  1     \n", group.toString());
    }

    @Test
    public void groupToJson() {

        Group group = new Group();

        Table table = group.getTable("testTable");

        table.addColumn(ColumnType.ColumnTypeBinary, "binary");     // 0
        table.addColumn(ColumnType.ColumnTypeBool, "boolean");      // 1
        table.addColumn(ColumnType.ColumnTypeDate, "date");         // 2
        table.addColumn(ColumnType.ColumnTypeDouble, "double");     // 3
        table.addColumn(ColumnType.ColumnTypeFloat, "float");       // 4
        table.addColumn(ColumnType.ColumnTypeInt, "long");          // 5
        table.addColumn(ColumnType.ColumnTypeMixed, "mixed");       // 6
        table.addColumn(ColumnType.ColumnTypeString, "string");     // 7
        table.addColumn(ColumnType.ColumnTypeTable, "table");       // 8

        table.add(new byte[] {0,2,3}, true, new Date(0), 123D, 123F, 123, new Mixed(123), "TestString", null);

        assertEquals("{\"testTable\":[{\"binary\":\"000203\",\"boolean\":true,\"date\":\"1970-01-01 00:00:00\",\"double\":1.2300000000000000e+02,\"float\":1.2300000e+02,\"long\":123,\"mixed\":123,\"string\":\"TestString\",\"table\":[]}]}", group.toJson());

    }



}
