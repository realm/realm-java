/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.experiment;

import io.realm.ColumnType;
import io.realm.Group;
import io.realm.Mixed;
import io.realm.Table;
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
