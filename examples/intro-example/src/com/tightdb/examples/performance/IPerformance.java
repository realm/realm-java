package com.tightdb.examples.performance;

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

