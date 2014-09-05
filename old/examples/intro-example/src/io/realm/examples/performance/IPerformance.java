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

public abstract interface IPerformance {

    public long usedNativeMemory();
    public void buildTable(int rows);

    public void begin_findSmallInt(long value);
    public boolean findSmallInt(long value);
    public void end_findSmallInt();

    public void begin_findByteInt(long value);
    public boolean findByteInt(long value);
    public void end_findByteInt();

    public void begin_findLongInt(long value);
    public boolean findLongInt(long value);
    public void end_findLongInt();

    public void begin_findString(String value);
    public boolean findString(String value);
    public void end_findString();

    public boolean addIndex();

    public void begin_findIntWithIndex();
    public long findIntWithIndex(long value);
    public void end_findIntWithIndex();

    public void closeTable();
}

