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

package io.realm.examples.performance;

import io.realm.DefineTable;
import io.realm.internal.Util;

public class Tightdb extends PerformanceBase implements IPerformance {


    @DefineTable(row="TestRow")
    class test
    {
        int     indexInt;
        String  second;
        int     byteInt;
        int     smallInt;
        long    longInt;
    }

    private TestTable table = null;

    public Tightdb() {
        table = new TestTable();
    }

    public long usedNativeMemory() {
        return Util.getNativeMemUsage();
    }

    public void buildTable(int rows) {
        for (int i = 0; i < rows; ++i) {
            int n = ExampleHelper.getRandNumber();
            table.add(n, ExampleHelper.getNumberString(n), Performance.BYTE_TEST_VAL, Performance.SMALL_TEST_VAL, Performance.LONG_TEST_VAL);
        }
    }

    //--------------- small Int

    public void begin_findSmallInt(long value) {
        //TestQuery q = table.smallInt.equalTo(value);
    }

    public boolean findSmallInt(long value) {
        //Test res = q.findFirst();
        TestRow res = table.smallInt.findFirst(value);
        return (res != null);
    }

    //--------------- byte Int

    public boolean findByteInt(long value) {
        TestRow res = table.byteInt.findFirst(value);
        return (res != null);
    }

    //--------------- long Int

    public boolean findLongInt(long value) {
        TestRow res = table.longInt.findFirst(value);
        return (res != null);
    }

    //---------------- string

    public boolean findString(String value) {
        TestRow res = table.second.equalTo(value).findFirst();
        return (res != null);
    }

    //---------------- int with index

    public boolean addIndex() {
        return false;
    }

    public long findIntWithIndex(long value)
    {
        TestRow res = table.indexInt.findFirst(value);
        return (res != null) ? (int)res.getPosition() : -1;
    }
}
