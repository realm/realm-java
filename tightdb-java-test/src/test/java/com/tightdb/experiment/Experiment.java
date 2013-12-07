package com.tightdb.experiment;

import com.tightdb.ColumnType;
import com.tightdb.Group;
import com.tightdb.Table;

public class Experiment {
    public static void main(String[] args) {

        System.out.println("Start experiment");
        test3();
    }

    public static Table getTable() {
        Group g = new Group();
        Table t = g.getTable("testTable");
        t.addColumn(ColumnType.STRING, "test");
        g.close();

        return t;
    }

    public static void test3() {
        Table t = getTable();
        //g.close();
        t.add("hej");

        System.out.println(t);
    }

    public static void test1() {
        insert(new Object[] {1, "txt"});
        insert("hmm", 2, "hej");

        Object[] sub2 = new Object[] {2, "str2", 22};
        Object[] subtable = new Object[] {1, "str1", sub2, 11};
        insert("hmm", subtable, 1);

        Object[][] arrOfArr = new Object[][] { {1}, {0} };
        insert("lll", arrOfArr );
    }

    public static void insert(Object... objects) {
        if (objects == null) return;
        System.out.print("\ninsert: ");
        for (Object obj : objects) {
            System.out.print(obj + ", ");
            if (obj instanceof Object[]) {
                System.out.print("...");
                insert((Object[])obj);
            }
        }
    }
}
