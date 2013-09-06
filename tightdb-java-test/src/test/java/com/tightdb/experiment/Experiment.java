package com.tightdb.experiment;

import com.tightdb.Table;
import com.tightdb.TableQuery;
import com.tightdb.TableView;

public class Experiment {
    public static void main(String[] args) {
    
    	System.out.println("Start experiment");
    	test2();
    }
    
    public static void test2() {
    	Table t = new Table();
    	
    	TableQuery q = t.where();
    	
    	TableView v = q.findAll();
    	//v.close();
    	v=null;

    	//q.close();
    	q=null;

    	t.close();
    	t = null;
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
