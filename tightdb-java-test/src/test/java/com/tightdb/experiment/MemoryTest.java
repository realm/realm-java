package com.tightdb.experiment;


import com.tightdb.Table;
import com.tightdb.TableQuery;
import com.tightdb.TableView;
import org.testng.annotations.Test;

public class MemoryTest {

    @Test
    public void testMemoryManagement() {

        // With debug messages enabled this will show that finalizers is actually called when the garbage collector runs. And there by disposing the Table and TableQuery objects

        System.out.println("Begin mem test");

        for(int i = 0; i < 10000; i++) {

            Table table = new Table();

            TableQuery query = table.where();

            TableView view = query.findAll();

        }

        System.out.println("End mem test");
    }

}

