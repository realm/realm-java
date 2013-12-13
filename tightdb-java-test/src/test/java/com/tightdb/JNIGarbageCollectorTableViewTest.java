package com.tightdb;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

// Tables get detached

public class JNIGarbageCollectorTableViewTest {

    private Table t;


    public void test1(long count){
        List<TableView> views = new ArrayList<TableView>();

        for (long i=0;i<count;i++){
            t.addEmptyRow();
            Table sub = t.getSubtable(0, i);
            views.add(sub.where().findAll());
            sub.close();
        }
    }

    public void test2(long count){
        for (long i=0;i<count;i++){
            Table sub = t.getSubtable(0, i);
            TableView view = sub.where().findAll();
            sub.close();
            view.size();
        }
    }

    public void test3(long count){
        for (long i=0;i<count;i++){
            Table sub = t.getSubtable(0, i);
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
        long loop = 100;

        for (int i=0;i<loop;i++){
            test1(count);
            test2(count);
            test3(count);
        }
    }
}
