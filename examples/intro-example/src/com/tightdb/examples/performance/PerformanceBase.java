package com.realm.examples.performance;

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
