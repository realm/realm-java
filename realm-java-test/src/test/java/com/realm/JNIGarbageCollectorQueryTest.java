package com.realm;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

// Tables get detached

public class JNIGarbageCollectorQueryTest {

    private Table t;


    public void test1(long count){
        List<TableQuery> views = new ArrayList<TableQuery>();

        for (long i=0;i<count;i++){
            t.addEmptyRow();
            Table sub = t.getSubtable(0, i);
            views.add(sub.where());
            sub.close();
        }
    }

    public void test2(long count){
        for (long i=0;i<count;i++){
            Table sub = t.getSubtable(0, i);
            TableQuery query = sub.where();
            sub.close();
            query.count();
        }
    }

    public void test3(long count){
        for (long i=0;i<count;i++){
            Table sub = t.getSubtable(0, i);
            TableQuery query = sub.where();
            sub.close();
            query.count();
            query.close();
        }
    }

    @Test(enabled=true)
    public void testGetSubtable(){

        t = new Table();
        t.addColumn(ColumnType.TABLE, "table");

        long count = 100;
        long loop = 100;

        for (int i=0;i<loop;i++){
            test1(count);
            test2(count);
            test3(count);
        }
    }
}
