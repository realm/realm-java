package com.tightdb;

import static org.testng.AssertJUnit.*;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;


// Tables get detached

public class JNIGarbageCollectorTableTest {

    private Table t;


    public void test1(long count){
        
        t = new Table();

        t.addColumn(ColumnType.TABLE, "table");
        t.addEmptyRow();

        List<Table> tables = new ArrayList<Table>();

        for (long i=0;i<count;i++){
            t.addEmptyRow();
            tables.add(t.getSubTable(0, i));
        }
        
        t.close();
    }

    public void test2(long count){

        t = new Table();

        t.addColumn(ColumnType.TABLE, "table");
        t.addEmptyRow();

        for (long i=0;i<count;i++){
            t.addEmptyRow();

            Table sub = t.getSubTable(0, i);
            sub.size();

        }
        
        t.close();
    }

    public void test3(long count){
        
        t = new Table();

        t.addColumn(ColumnType.TABLE, "table");
        t.addEmptyRow();

        for (long i=0;i<count;i++){
            t.addEmptyRow();

            Table sub = t.getSubTable(0, i);
            sub.size();
            sub.close();
        }
        
        t.close();
    }


    @Test
    public void testGetSubtable(){

        


        long count = 1000;

        long loop = 0;

        for (int i=0;i<100;i++){
           // System.out.println("Loop: " + loop);
            test1(count);
            test2(count);
            test3(count);
            loop++;
        }
    }
}
