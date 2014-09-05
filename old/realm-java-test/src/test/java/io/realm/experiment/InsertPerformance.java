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

import io.realm.ColumnType;
import io.realm.Table;

public class InsertPerformance {

    public static void main(String[] args) {

        Table t = new Table();

        t.addColumn(ColumnType.STRING, "String");
        t.addColumn(ColumnType.BOOLEAN, "Bool");
        t.addColumn(ColumnType.INTEGER, "Long");
        t.addColumn(ColumnType.DATE, "Date");

        Long timer = System.currentTimeMillis();

        System.out.println("Performance test for inserting values in table:");

        for (int i=0;i<50000000;i++){

            t.add("String", false, 4000L, new Date());

            if (i % 1000000 == 0 && i > 0){
                System.out.println(i + " split time: " +  (System.currentTimeMillis() - timer));
            }
        }

        System.out.println("Total time in miliseconds: " + (System.currentTimeMillis() - timer));
    }
}
