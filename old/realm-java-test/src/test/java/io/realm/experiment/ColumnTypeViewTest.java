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

import io.realm.TableView;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.realm.ColumnType;
import io.realm.Table;

public class ColumnTypeViewTest {
    private Table t;
    private TableView v;

    @BeforeMethod
    public void init() {
        t  = new Table();
        t.addColumn(ColumnType.DATE, "Date");
        t.addColumn(ColumnType.STRING, "String");
        t.addColumn(ColumnType.INTEGER , "Long");

        t.add(new Date(), "I'm a String", 33);

        v = t.where().findAll();
    }

    @AfterMethod
    public void after() throws Throwable {
        t.close();
        t = null;
        v = null;
    }

    //On date Column________________________________
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void getStringOnDateColumn() {
        v.getString(0, 0);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void getLongOnDateColumn() {
        v.getLong(0, 0);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void getMixedOnDateColumn() {
        v.getMixed(0, 0);
    }

    //On String Column________________________________
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void getDateOnStringColumn() {
        v.getDate(1, 0);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void getLongOnStringColumn() {
        v.getLong(1, 0);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void getMixedOnStringColumn() {
        v.getMixed(1, 0);
    }

    //On Integer Column________________________________
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void getDateOnIntegerColumn() {
        v.getDate(2, 0);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void getStringOnIntegerColumn() {
        v.getString(2, 0);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void getMixedOnIntegerColumn() {
        v.getMixed(2, 0);
    }

}
