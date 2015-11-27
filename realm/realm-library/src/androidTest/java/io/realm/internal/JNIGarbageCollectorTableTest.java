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

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmFieldType;


// Tables get detached

public class JNIGarbageCollectorTableTest extends TestCase {

    private Table t;


    public void t1(long count){
        t = new Table();

        t.addColumn(RealmFieldType.UNSUPPORTED_TABLE, "table");
        t.addEmptyRow();

        List<Table> tables = new ArrayList<Table>();

        for (long i=0;i<count;i++){
            t.addEmptyRow();
            tables.add(t.getSubtable(0, i));
        }
        
        t.close();
    }

    public void t2(long count){
        t = new Table();

        t.addColumn(RealmFieldType.UNSUPPORTED_TABLE, "table");
        t.addEmptyRow();

        for (long i=0;i<count;i++){
            t.addEmptyRow();

            Table sub = t.getSubtable(0, i);
            sub.size();
        }
        
        t.close();
    }

    public void t3(long count){
        t = new Table();

        t.addColumn(RealmFieldType.UNSUPPORTED_TABLE, "table");
        t.addEmptyRow();

        for (long i=0;i<count;i++){
            t.addEmptyRow();

            Table sub = t.getSubtable(0, i);
            sub.size();
            sub.close();
        }
        
        t.close();
    }

    public void testGetSubtable(){

        long count = 10; //1000;
        long loop = 100;

        for (int i=0;i<loop;i++){
            t1(count);
            t2(count);
            t3(count);
        }
    }
}
