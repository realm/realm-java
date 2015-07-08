package io.realm.internal;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

// Tables get detached

public class JNIGarbageCollectorTableTest extends TestCase {

    private Table t;

    public void t1(long count) {
        t = new Table();

        t.addColumn(ColumnType.TABLE, "table");
        t.addEmptyRow();

        List<Table> tables = new ArrayList<Table>();

        for (long i = 0; i < count; i++) {
            t.addEmptyRow();
            tables.add(t.getSubtable(0, i));
        }

        t.close();
    }

    public void t2(long count) {
        t = new Table();

        t.addColumn(ColumnType.TABLE, "table");
        t.addEmptyRow();

        for (long i = 0; i < count; i++) {
            t.addEmptyRow();

            Table sub = t.getSubtable(0, i);
            sub.size();
        }

        t.close();
    }

    public void t3(long count) {
        t = new Table();

        t.addColumn(ColumnType.TABLE, "table");
        t.addEmptyRow();

        for (long i = 0; i < count; i++) {
            t.addEmptyRow();

            Table sub = t.getSubtable(0, i);
            sub.size();
            sub.close();
        }

        t.close();
    }

    public void testGetSubtable() {

        long count = 10; //1000;
        long loop = 100;

        for (int i = 0; i < loop; i++) {
            t1(count);
            t2(count);
            t3(count);
        }
    }
}
