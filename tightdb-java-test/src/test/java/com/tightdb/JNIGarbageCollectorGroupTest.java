/*package com.tightdb;

import static org.testng.AssertJUnit.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.tightdb.test.TestHelper;

// Tables get detached

public class JNIGarbageCollectorGroupTest {

    private Group g;


    public void test1(long count){

        List<Table> tables = new ArrayList<Table>();

        for (long i=0;i<count;i++){
            Table t = g.getTable("" + i);
            t.addColumn(ColumnType.STRING, "");
            tables.add(t);
            t.size();
        }

        System.out.println("Test1. Size : " + tables.size());
    }

    public void test2(long count){


        for (long i=0;i<count;i++){
            Table t = g.getTable("" + i);
            t.addColumn(ColumnType.STRING, "");
            t.size();
        }

        System.out.println("Test2. Done");
    }

    public void test3(long count){

        for (long i=0;i<count;i++){
            Table t = g.getTable("" + i);
            t.addColumn(ColumnType.STRING, "");
            t.size();
            t.close();
        }

        System.out.println("Test3. Done");
    }


    @Test
    public void testGetSubtable(){

        g = new Group();


        long count = 10000;

        long loop = 0;

        while (true){
            System.out.println("Loop: " + loop);
            test1(count);
            test2(count);
            test3(count);
            loop++;
            
            if(loop % 10 == 0){
                System.gc();
                System.out.println("GC called");
            }
        }
    }
}
*/