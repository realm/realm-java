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
import io.realm.DefineTable;
import io.realm.Table;

public class InsertVsAddPerformance {


    private static long ROWS = 10000000;


    @DefineTable(table = "PeopleTable")
    class people {
        String  name1;
        long     age1;
        boolean hired1;
        String  name2;
        long     age2;
        boolean hired2;
        String  name3;
        long     age3;
        boolean hired3;
        String  name4;
        long     age4;
        boolean hired5;
    }


    public static void main(String[] args) {

        PeopleTable pt = new PeopleTable();

        Long typedTimer = System.currentTimeMillis();
        System.out.println("Performance testing TYPED interface on " + ROWS + " rows");

        for (long r=0;r<ROWS;r++){
           /* if (r % 1000000 == 0 && r > 0){
                System.out.println(r + " split time: " +  (System.currentTimeMillis() - typedTimer));
            }*/
            pt.add("name"+r, r, true, "name"+r, r, true, "name"+r, r, true, "name"+r, r, true);
        }

        Long totalTimeTyped = System.currentTimeMillis() - typedTimer;
        System.out.println("Time for TYPED interface: " + totalTimeTyped);

        Table t = new Table();

        t.addColumn(ColumnType.STRING, "String");
        t.addColumn(ColumnType.INTEGER, "Long");
        t.addColumn(ColumnType.BOOLEAN, "Boolean");
        t.addColumn(ColumnType.STRING, "String");
        t.addColumn(ColumnType.INTEGER, "Long");
        t.addColumn(ColumnType.BOOLEAN, "Boolean");
        t.addColumn(ColumnType.STRING, "String");
        t.addColumn(ColumnType.INTEGER, "Long");
        t.addColumn(ColumnType.BOOLEAN, "Boolean");
        t.addColumn(ColumnType.STRING, "String");
        t.addColumn(ColumnType.INTEGER, "Long");
        t.addColumn(ColumnType.BOOLEAN, "Boolean");

        Long dynTimer = System.currentTimeMillis();
        System.out.println("Performance testing DYNAMIC interface on " + ROWS + " rows");

        for (long r=0;r<ROWS;r++){
            /*if (r % 1000000 == 0 && r > 0){
                System.out.println(r + " split time: " +  (System.currentTimeMillis() - dynTimer));
            }*/
            t.add("name"+r, r, true, "name"+r, r, true, "name"+r, r, true, "name"+r, r, true);
        }

        Long totalTimeDyn = System.currentTimeMillis() - dynTimer;

        System.out.println("Summery for performance tests on " + ROWS + " rows:");
        System.out.println("TYPED: " + totalTimeTyped);
        System.out.println("DYNAMIC: " + totalTimeDyn);
    }
}
