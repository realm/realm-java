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

package io.tightdb.example;

import io.realm.ColumnType;
import io.realm.Mixed;
import io.realm.Table;
import io.realm.TableSpec;

public class MixedTest {

    public static void main(String[] args) {
        Table table = new Table();

        TableSpec tableSpec = new TableSpec();
        tableSpec.addColumn(ColumnType.INTEGER, "num");
        tableSpec.addColumn(ColumnType.MIXED, "mix");
        TableSpec subspec = tableSpec.addSubtableColumn("subtable");
        subspec.addColumn(ColumnType.INTEGER, "num");
        table.updateFromSpec(tableSpec);

        try {
            // Shouldn't work: no Mixed stored yet
            @SuppressWarnings("unused")
            Mixed m = table.getMixed(1, 0);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println( "Got exception - as expected!");
        }

        table.addEmptyRow();
        table.setMixed(1, 0, new Mixed(ColumnType.TABLE));
        Mixed m = table.getMixed(1, 0);

        ColumnType mt = table.getMixedType(1,0);
        System.out.println("m = " + m + " type: " + mt);

    }

}
