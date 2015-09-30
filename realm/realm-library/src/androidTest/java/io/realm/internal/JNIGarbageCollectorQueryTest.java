package io.realm.internal;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

// Tables get detached

public class JNIGarbageCollectorQueryTest extends TestCase {

    private Table t;


    public void t1(long count){
        List<TableQuery> views = new ArrayList<TableQuery>();

        for (long i=0;i<count;i++){
            t.addEmptyRow();
            Table sub = t.getSubtable(0, i);
            views.add(sub.where());
            sub.close();
        }
    }

    public void t2(long count){
        for (long i=0;i<count;i++){
            Table sub = t.getSubtable(0, i);
            TableQuery query = sub.where();
            sub.close();
            query.count();
        }
    }

    public void t3(long count){
        for (long i=0;i<count;i++){
            Table sub = t.getSubtable(0, i);
            TableQuery query = sub.where();
            sub.close();
            query.count();
            query.close();
        }
    }

    public void testGetSubtable(){

        t = new Table();
        t.addColumn(ColumnType.TABLE, "table");

        long count = 100;
        long loop = 100;

        for (int i=0;i<loop;i++){
            t1(count);
            t2(count);
            t3(count);
        }
    }
}
