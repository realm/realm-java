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
import io.realm.Table;

public class Experiment {
    public static void main(String[] args) {

        System.out.println("Start experiment");
        test3();
    }

    public static Table getTable() {
        Group g = new Group();
        Table t = g.getTable("testTable");
        t.addColumn(ColumnType.STRING, "test");
        g.close();

        return t;
    }

    public static void test3() {
        Table t = getTable();
        //g.close();
        t.add("hej");

        System.out.println(t);
    }

    public static void test1() {
        insert(new Object[] {1, "txt"});
        insert("hmm", 2, "hej");

        Object[] sub2 = new Object[] {2, "str2", 22};
        Object[] subtable = new Object[] {1, "str1", sub2, 11};
        insert("hmm", subtable, 1);

        Object[][] arrOfArr = new Object[][] { {1}, {0} };
        insert("lll", arrOfArr );
    }

    public static void insert(Object... objects) {
        if (objects == null) return;
        System.out.print("\ninsert: ");
        for (Object obj : objects) {
            System.out.print(obj + ", ");
            if (obj instanceof Object[]) {
                System.out.print("...");
                insert((Object[])obj);
            }
        }
    }
}
