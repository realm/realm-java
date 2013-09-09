package com.tightdb.experiment;

import com.tightdb.ColumnType;
import com.tightdb.Group;
import com.tightdb.Mixed;
import com.tightdb.Table;
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

        table.add(new byte[] {0,2,3}, true, new Date(), 123D, 123F, 123, new Mixed(123), "TestString", null);

        System.out.println(group.toString());

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

        table.add(new byte[] {0,2,3}, true, new Date(), 123D, 123F, 123, new Mixed(123), "TestString", null);

        System.out.println(group.toJson());

    }



}
