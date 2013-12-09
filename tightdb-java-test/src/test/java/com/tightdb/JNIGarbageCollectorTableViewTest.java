/*package com.tightdb;

import static org.testng.AssertJUnit.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.tightdb.test.TestHelper;

// Tables get detached

public class JNIGarbageCollectorTableViewTest {

    private Table t;


    public void test1(long count){

        List<TableView> views = new ArrayList<TableView>();

        for (long i=0;i<count;i++){
            t.addEmptyRow();
            views.add(t.getSortedView(0));
        }

        System.out.println("Test1. Size : " + views.size());
    }

    public void test2(long count){


        for (long i=0;i<count;i++){
            TableView view = t.getSortedView(0);
            view.size();
        }

        System.out.println("Test2. Done");
    }

    public void test3(long count){

        for (long i=0;i<count;i++){
            TableView view = t.getSortedView(0);
            view.size();
            view.close();
        }

        System.out.println("Test3. Done");
    }


    @Test
    public void testGetSubtable(){

        t = new Table();

        t.addColumn(ColumnType.INTEGER, "int");
        t.addEmptyRow();


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
*/