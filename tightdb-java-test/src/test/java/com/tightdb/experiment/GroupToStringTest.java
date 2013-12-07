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
        table.addColumn(ColumnType.BOOLEAN, "boolean");
        table.add(true);
        Table table2 = group.getTable("another-table");
        table2.addColumn(ColumnType.BOOLEAN, "boolean");
        table2.add(true);

        assertEquals("     tables        rows  \n" +
                     "   0 testTable     1     \n" +
                     "   1 another-table 1     \n", group.toString());
    }

    @Test
    public void groupToJson() {

        Group group = new Group();

        Table table = group.getTable("testTable");

        table.addColumn(ColumnType.BINARY, "binary");     // 0
        table.addColumn(ColumnType.BOOLEAN, "boolean");   // 1
        table.addColumn(ColumnType.DATE, "date");         // 2
        table.addColumn(ColumnType.INTEGER, "long");      // 3
        table.addColumn(ColumnType.MIXED, "mixed");       // 4
        table.addColumn(ColumnType.STRING, "string");     // 5
        table.addColumn(ColumnType.TABLE, "table");       // 6

        table.add(new byte[] {0,2,3}, true, new Date(0), 123, new Mixed(123), "TestString", null);

        assertEquals("{\"testTable\":[{\"binary\":\"000203\",\"boolean\":true,\"date\":\"1970-01-01 00:00:00\",\"long\":123,\"mixed\":123,\"string\":\"TestString\",\"table\":[]}]}", group.toJson());
    }



}
