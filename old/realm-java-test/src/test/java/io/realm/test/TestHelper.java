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

package io.realm.test;

import java.util.Date;

import io.realm.ColumnType;
import io.realm.Table;


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
            return ColumnType.INTEGER;
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


    /**
     * Creates an empty table with 1 column of all our supported column types, currently 9 columns
     * @return
     */
    public static Table getTableWithAllColumnTypes(){

        Table t = new Table();

        t.addColumn(ColumnType.BINARY, "binary");
        t.addColumn(ColumnType.BOOLEAN, "boolean");
        t.addColumn(ColumnType.DATE, "date");
        t.addColumn(ColumnType.DOUBLE, "double");
        t.addColumn(ColumnType.FLOAT, "float");
        t.addColumn(ColumnType.INTEGER, "long");
        t.addColumn(ColumnType.MIXED, "mixed");
        t.addColumn(ColumnType.STRING, "string");
        t.addColumn(ColumnType.TABLE, "table");

        return t;
    }
}
