package com.tightdb.examples.performance;

import java.util.ArrayList;

public class JavaArrayList extends PerformanceBase implements IPerformance {

    public static class Table
    {
        int     indexInt;
        String  second;
        int     byteInt;
        int     smallInt;
        long    longInt;

        public Table(int indexInt, String second, int byteInt, int smallInt, long longInt) {
            this.indexInt = indexInt;
            this.second = second;
            this.byteInt = byteInt;
            this.smallInt = smallInt;
            this.longInt = longInt;
        }
    }

    private ArrayList<Table> table = null;
    private int Rows = 0;

    public JavaArrayList() {
        table = new ArrayList<Table>();
    }

    public long usedNativeMemory() {
        return 0;
    }

    public void buildTable(int rows) {
        for (int i = 0; i < rows; ++i) {
            int n = ExampleHelper.getRandNumber();
            table.add(new Table(n, ExampleHelper.getNumberString(n), Performance.BYTE_TEST_VAL, Performance.SMALL_TEST_VAL, Performance.LONG_TEST_VAL) );
        }
        this.Rows = rows;
    }

  //--------------- small Int

    public boolean findSmallInt(long value) {
        int index;
        for (index = 0; index < Rows; index++) {
            if (table.get(index).smallInt == value) {
                break;
            }
        }
        return (index != Rows);
    }

    //--------------- byte Int

    public boolean findByteInt(long value) {
        int index;
        for (index = 0; index < Rows; index++) {
            if (table.get(index).byteInt == value) {
                break;
            }
        }
        return (index != Rows);
    }

  //--------------- long Int

    public boolean findLongInt(long value) {
        int index;
        for (index = 0; index < Rows; index++) {
            if (table.get(index).longInt == value) {
                break;
            }
        }
        return (index != Rows);
    }

    //---------------- string

    public boolean findString(String value) {
        int index;
        for (index = 0; index < Rows; index++) {
            if (table.get(index).second.equalsIgnoreCase(value)) {
                break;
            }
        }
        return (index != Rows);
    }

    //---------------- int with index

    public boolean addIndex() {
        return false;
    }

    public long findIntWithIndex(long value)
    {
        return -1;
    }
}
