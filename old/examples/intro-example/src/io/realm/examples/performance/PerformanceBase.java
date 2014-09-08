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

public abstract class PerformanceBase implements IPerformance {

    @Override
    public long usedNativeMemory() {
        return 0;
    }

    public abstract void buildTable(int rows);

    @Override
    public void begin_findSmallInt(long value) {
    }

    @Override
    public abstract boolean findSmallInt(long value);

    @Override
    public void end_findSmallInt() {
    }

    @Override
    public void begin_findByteInt(long value) {
    }

    @Override
    public abstract boolean findByteInt(long value);

    @Override
    public void end_findByteInt() {
    }

    @Override
    public void begin_findLongInt(long value) {
    }

    @Override
    public abstract boolean findLongInt(long value);

    @Override
    public void end_findLongInt() {
    }

    @Override
    public void begin_findString(String value) {
    }

    @Override
    public abstract boolean findString(String value);

    @Override
    public void end_findString() {
    }

    @Override
    public boolean addIndex() {
        return false;
    }

    @Override
    public void begin_findIntWithIndex() {
    }

    @Override
    public long findIntWithIndex(long value) {
        return -1;
    }

    @Override
    public void end_findIntWithIndex() {
    }

    @Override
    public void closeTable() {
    }

}
