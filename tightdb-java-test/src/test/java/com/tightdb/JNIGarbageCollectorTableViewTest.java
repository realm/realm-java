package com.tightdb;

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
            Table sub = t.getSubTable(0, i);
            views.add(sub.where().findAll());
            sub.close();
        }
    }

    public void test2(long count){


        for (long i=0;i<count;i++){
            Table sub = t.getSubTable(0, i);
            TableView view = sub.where().findAll();
            sub.close();
            view.size();
        }
    }

    public void test3(long count){

        for (long i=0;i<count;i++){
            Table sub = t.getSubTable(0, i);
            TableView view = sub.where().findAll();
            sub.close();
            view.size();
            view.close();
        }
    }


    @Test(enabled=true)
    public void testGetSubtableView(){

        t = new Table();

        t.addColumn(ColumnType.TABLE, "table");

        long count = 1000;

        long loop = 0;

        for (int i=0;i<100;i++){
            //System.out.println("Loop: " + loop);
            test1(count);
            test2(count);
            test3(count);
            loop++;
        }
    }
}
