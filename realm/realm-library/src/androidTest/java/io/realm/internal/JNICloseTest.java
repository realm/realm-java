/*
 * Copyright 2015 Realm Inc.
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

package io.realm.internal;

import android.test.AndroidTestCase;

import io.realm.RealmFieldType;
import io.realm.TestHelper;

public class JNICloseTest extends AndroidTestCase {

    /**
     * Make sure, that it's possible to use the query on a closed table
     */
    public void testQueryAccessibleAfterTableClose() throws Throwable{
        Table table = TestHelper.getTableWithAllColumnTypes();
        table.addEmptyRows(10);
        for (long i=0; i<table.size(); i++)
            table.setLong(5, i, i);
        TableQuery query = table.where();
        // Closes the table, it _should_ be allowed to access the query thereafter
        Table.nativeClose(table.nativePtr);
        Table table2 = TestHelper.getTableWithAllColumnTypes();
        table2.addEmptyRows(10);
        for (int i=0; i<table2.size(); i++)
            table2.setLong(5, i, 117+i);

        TableView tv = query.findAll();
        assertEquals(10, tv.size());

        // TODO: add a lot of methods
    }

    public void testAccessingViewMethodsAfterTableClose() {
        Table table = TestHelper.getTableWithAllColumnTypes();
        table.addEmptyRows(10);
        TableQuery query = table.where();
        TableView view = query.findAll();
        //Closes the table, it should be allowed to access the view thereafter (table is ref-counted)
        Table.nativeClose(table.nativePtr);

        // Accessing methods should be ok.
        view.size();
        view.getBinaryByteArray(0, 0);
        view.getBoolean(1, 0);
        view.getDate(2, 0);
        view.getDouble(3, 0);
        view.getFloat(4, 0);
        view.getLong(5, 0);

        // TODO - add all methods from view
    }
}
