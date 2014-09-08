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


import io.realm.TableQuery;
import io.realm.TableView;
import org.testng.annotations.Test;
import org.testng.AssertJUnit;
import io.realm.ColumnType;
import io.realm.Table;

// Execute this test with Memory leak detector enabled.

public class MemoryLeakTest {

    @Test
    public void testMemoryManagement() throws Throwable {

        //System.out.println("Begin mem test");

        for (int i = 0; i < 10000; i++) {

            Table table = new Table();
            table.addColumn(ColumnType.INTEGER, "myint");
            table.add(i);

            TableQuery query = table.where();

            TableView view = query.notEqualTo(0, 2).findAll();
            AssertJUnit.assertEquals(i, table.getLong(0,0) );
            view.close();

            query.close();

            table.close();
        }
        // System.out.println("End mem test");
    }
}
