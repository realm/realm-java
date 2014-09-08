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

import java.util.Date;

import io.realm.TableQuery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.realm.ColumnType;
import io.realm.Table;

public class ColumnTypeQueryTest {
    private Table t;
    private TableQuery q;

    @BeforeMethod
    public void init() {
        t  = new Table();
        t.addColumn(ColumnType.DATE, "Date");
        t.addColumn(ColumnType.STRING, "String");
        t.addColumn(ColumnType.INTEGER, "Long");

        t.add(new Date(), "I'm a String", 33);
        t.add(new Date(), "Second String", 458);

        q = t.where();
    }

    @Test(expectedExceptions=IllegalArgumentException.class)

    public void filterStringOnDateColumn() {
        q.equalTo(0, "I'm a String").findAll();
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void filterLongOnStringColumn() {
        q.equalTo(1, 23).findAll();
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void filterStringOnIntColumn() {
        q.equalTo(2, "I'm a String").findAll();
    }


}
