package com.tightdb;

import static org.testng.AssertJUnit.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.tightdb.test.TestHelper;

// Tables get detached

public class JNIGarbageCollectorQueryTest {

    private Table t;


    public void test1(long count){

        List<TableQuery> views = new ArrayList<TableQuery>();

        for (long i=0;i<count;i++){
            t.addEmptyRow();
            Table sub = t.getSubTable(0, i);
            views.add(sub.where());
            sub.close();
        }

        System.out.println("Test1. Size : " + views.size());
    }

    public void test2(long count){


        for (long i=0;i<count;i++){
            Table sub = t.getSubTable(0, i);
            TableQuery query = sub.where();
            sub.close();
            query.count();
        }

        System.out.println("Test2. Done");
    }

    public void test3(long count){

        for (long i=0;i<count;i++){
            Table sub = t.getSubTable(0, i);
            TableQuery quer = sub.where();
            sub.close();
            quer.count();
            quer.close();
        }

        System.out.println("Test3. Done");
    }


    @Test
    public void testGetSubtable(){

        t = new Table();

        t.addColumn(ColumnType.TABLE, "table");

        long count = 10000;

        long loop = 0;

        while (true){
            System.out.println("Loop: " + loop);
            test1(count);
            test2(count);
            test3(count);
            loop++;
        }
    }
}
