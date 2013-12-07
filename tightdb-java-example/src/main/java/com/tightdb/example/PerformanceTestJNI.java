package com.tightdb.example;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.tightdb.*;

public class PerformanceTestJNI {
    final static int ROWS = 100000;
    final static int LOOPS = 20;
    
    public static void main(String[] args) {
        long dur;
        
        Table tbl = setup_getLong(ROWS);
        for (int i=0; i<LOOPS; i++) {
            dur = time_getLong(tbl, ROWS);
            System.out.printf("Time for %d tbl.getLong():  %d us.\n", ROWS, dur);
        }
        
        long[] tbl2 = setup_arrayGet(ROWS);
        for (int i=0; i<LOOPS; i++) {
            dur = time_arrayGet(tbl2, ROWS);
            System.out.printf("Time for %d Array.get():    %d us.\n", ROWS, dur);
        }
        
        ArrayList<Long> tbl3 = setup_arrayListGet(ROWS);
        for (int i=0; i<LOOPS; i++) {
            dur = time_arrayListGet(tbl3, ROWS);
            System.out.printf("Time for %d ArrayList.get(): %d us.\n", ROWS, dur);
        }
        
        //test_getName();
    }


    static Table setup_getLong(int rows) {
        // Create table with 1 Long column and a number of rows
        Table tbl = new Table();
        tbl.addColumn(ColumnType.INTEGER, "myInt");
        tbl.addEmptyRows(rows);
        for (long idx=0; idx<rows; idx++) {
            tbl.setLong(0, idx, 2); 
        }
        return tbl;
    }
    static long time_getLong(Table tbl, int rows) {
        // Loop through the table
        Timer time = new Timer();
        for (long idx = 0; idx < rows; idx++) {
            if (tbl.getLong(0, idx) == -1) {
                break;
            }
        }
        long dur = time.GetTimeInMicroSec();
        return dur;
    }

    
    static long[] setup_arrayGet(int rows) {
        // Create table with 1 Long column and a number of rows
        long[] tbl = new long[rows];
        for (int idx=0; idx<rows; idx++) {
            tbl[idx] = idx;
        }
        return tbl;
    }
    static long time_arrayGet(long[] tbl, int rows) {   
        // Loop through the table
        Timer time = new Timer();
        for (int idx = 0; idx < rows; idx++) {
            if (tbl[idx] == -1) {
                break;
            }
        }
        long dur = time.GetTimeInMicroSec();
        return dur;
    }

    static ArrayList<Long> setup_arrayListGet(int rows) {   
        // Create table with 1 Long column and a number of rows
        ArrayList<Long> tbl = new ArrayList<Long>(rows);
        for (Long idx = (long)0; idx < rows; idx++) {
            tbl.add(idx);
        }
        return tbl;
    }
    
    static long time_arrayListGet(ArrayList<Long> tbl, int rows) {      
        // Loop through the table
        Timer time = new Timer();
        for (int idx = 0; idx < rows; idx++) {
            if (tbl.get(idx) == -1) {
                break;
            }
        }
        long dur = time.GetTimeInMicroSec();
        return dur;
    }
    

    static void test_getName() {
        final int COLUMNS = 1000;
        final int ROWS = 1000;
        final int LOOPS = 100000;
        
        Table tbl = new Table();
        Map<String, Long> map = new HashMap<String, Long>();

        for (long i=0; i<COLUMNS; i++) {
            String name = "column"+i;
            tbl.addColumn(ColumnType.BOOLEAN, name);
            map.put(name, i);
        }
        tbl.addEmptyRows(ROWS);
        
        long columns = tbl.getColumnCount();
        boolean bool = false;
        Object col = (Object)0L;
        for (int j=0; j<LOOPS; j++) {
            for (long i=0; i<columns; i++) {
                String name = tbl.getColumnName((Long)col);
                bool = tbl.getBoolean(i, 5);
                col = map.get(name);
            }
        }
        System.out.println("Done." + bool + col);
    }

}


class Timer {
    static long startTime;
    
    public Timer() {
        Start();
    }
    
    public void Start() {
        startTime = System.nanoTime();
    }

    public long GetTimeInMicroSec() {
        long stopTime = System.nanoTime();
        return (stopTime - startTime) / 1000;
    }
}
