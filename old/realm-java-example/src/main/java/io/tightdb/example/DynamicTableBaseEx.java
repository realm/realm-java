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
import io.realm.TableView;
// import io.realm.internal.util;

public class DynamicTableBaseEx {

    public static void main(String[] args) {
        // System.out.println("--Memusage: " + util.getNativeMemUsage());

        Table base = new Table();
        System.out.println("created table");

        base.addColumn(ColumnType.STRING, "name");
        base.addColumn(ColumnType.INTEGER, "salary");
        base.addColumn(ColumnType.MIXED, "Whatever");
        System.out.println("specified structure");

        base.add("John", 24000, new Mixed(1));
        System.out.println("inserted data");

        System.out.println(base.getColumnName(0));
        System.out.println(base.getColumnName(1));

        System.out.println(base.size());
        System.out.println(base.getString(0, 0));
        System.out.println(base.getLong(1, 0));

        TableView results = base.findAllLong(1, 24000);
        System.out.println("Results size: " + results.size());

        long rowIndex = base.findFirstString(0, "John");
        System.out.println("First result index: " + rowIndex);

        //System.out.println("--Memusage: " + util.getNativeMemUsage());

        base.remove(0);
        base.clear();
    }

}
